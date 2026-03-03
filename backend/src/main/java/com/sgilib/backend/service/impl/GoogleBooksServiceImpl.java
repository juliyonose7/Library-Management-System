package com.sgilib.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.sgilib.backend.api.dto.GoogleBookMetadataResponse;
import com.sgilib.backend.service.GoogleBooksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class GoogleBooksServiceImpl implements GoogleBooksService {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksServiceImpl.class);

    private final RestClient googleRestClient;
    private final RestClient openLibraryRestClient;
    private final boolean enabled;

    public GoogleBooksServiceImpl(RestClient.Builder restClientBuilder,
                                  @Value("${integration.google-books.enabled:true}") boolean enabled) {
        this.googleRestClient = restClientBuilder.baseUrl("https://www.googleapis.com/books/v1").build();
        this.openLibraryRestClient = restClientBuilder.baseUrl("https://openlibrary.org").build();
        this.enabled = enabled;
    }

    @Override
    public Optional<GoogleBookMetadataResponse> findByIsbn(String isbn) {
        if (!enabled || isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedIsbn = isbn.replaceAll("[^0-9Xx]", "");
        if (normalizedIsbn.isEmpty()) {
            return Optional.empty();
        }

        Optional<GoogleBookMetadataResponse> googleMetadata = findFromGoogle(normalizedIsbn);
        if (googleMetadata.isPresent()) {
            return googleMetadata;
        }

        return findFromOpenLibrary(normalizedIsbn);
    }

    private Optional<GoogleBookMetadataResponse> findFromGoogle(String normalizedIsbn) {
        try {
            JsonNode root = googleRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", "isbn:" + normalizedIsbn)
                            .queryParam("maxResults", 1)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null || !root.has("items") || !root.get("items").isArray() || root.get("items").isEmpty()) {
                return Optional.empty();
            }

            JsonNode volumeInfo = root.get("items").get(0).path("volumeInfo");
            GoogleBookMetadataResponse response = new GoogleBookMetadataResponse();
            response.setTitle(textOrNull(volumeInfo, "title"));
            response.setSubtitle(textOrNull(volumeInfo, "subtitle"));
            response.setDescription(textOrNull(volumeInfo, "description"));
            response.setPublisher(textOrNull(volumeInfo, "publisher"));
            response.setCategory(firstArrayText(volumeInfo, "categories"));
            response.setAuthorName(firstArrayText(volumeInfo, "authors"));
            response.setCoverUrl(extractGoogleCover(volumeInfo));
            response.setPublicationYear(extractYear(textOrNull(volumeInfo, "publishedDate")));
            response.setPageCount(intOrNull(volumeInfo, "pageCount"));
            return Optional.of(response);
        } catch (Exception ex) {
            log.warn("Google Books lookup failed for ISBN {}: {}", normalizedIsbn, ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<GoogleBookMetadataResponse> findFromOpenLibrary(String normalizedIsbn) {
        try {
            JsonNode root = openLibraryRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/books")
                            .queryParam("bibkeys", "ISBN:" + normalizedIsbn)
                            .queryParam("jscmd", "data")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null || root.isEmpty()) {
                return Optional.empty();
            }

            String key = "ISBN:" + normalizedIsbn;
            JsonNode volumeInfo = root.path(key);
            if (volumeInfo.isMissingNode() || volumeInfo.isNull() || volumeInfo.isEmpty()) {
                return Optional.empty();
            }

            GoogleBookMetadataResponse response = new GoogleBookMetadataResponse();
            response.setTitle(textOrNull(volumeInfo, "title"));
            response.setSubtitle(textOrNull(volumeInfo, "subtitle"));
            response.setDescription(textOrNull(volumeInfo, "notes"));
            response.setPublisher(firstArrayObjectText(volumeInfo, "publishers", "name"));
            response.setCategory(firstArrayObjectText(volumeInfo, "subjects", "name"));
            response.setAuthorName(firstArrayObjectText(volumeInfo, "authors", "name"));
            response.setCoverUrl(extractOpenLibraryCover(volumeInfo));
            response.setPublicationYear(extractYear(textOrNull(volumeInfo, "publish_date")));
            response.setPageCount(intOrNull(volumeInfo, "number_of_pages"));
            return Optional.of(response);
        } catch (Exception ex) {
            log.warn("Open Library fallback failed for ISBN {}: {}", normalizedIsbn, ex.getMessage());
            return Optional.empty();
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static Integer intOrNull(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        return node.get(field).asInt();
    }

    private static Integer extractYear(String publishedDate) {
        if (publishedDate == null || publishedDate.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(publishedDate.substring(0, 4));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String firstArrayText(JsonNode node, String field) {
        if (node == null || !node.has(field) || !node.get(field).isArray() || node.get(field).isEmpty()) {
            return null;
        }
        String value = node.get(field).get(0).asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static String firstArrayObjectText(JsonNode node, String field, String subField) {
        if (node == null || !node.has(field) || !node.get(field).isArray() || node.get(field).isEmpty()) {
            return null;
        }
        JsonNode entry = node.get(field).get(0);
        if (entry == null || entry.isMissingNode()) {
            return null;
        }
        if (entry.isTextual()) {
            String value = entry.asText();
            return value == null || value.isBlank() ? null : value;
        }
        if (!entry.hasNonNull(subField)) {
            return null;
        }
        String value = entry.get(subField).asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static String extractGoogleCover(JsonNode volumeInfo) {
        JsonNode imageLinks = volumeInfo.path("imageLinks");
        if (imageLinks.isMissingNode()) {
            return null;
        }
        String cover = textOrNull(imageLinks, "thumbnail");
        if (cover == null) {
            cover = textOrNull(imageLinks, "smallThumbnail");
        }
        if (cover == null) {
            return null;
        }
        return cover.replace("http://", "https://");
    }

    private static String extractOpenLibraryCover(JsonNode volumeInfo) {
        JsonNode coverNode = volumeInfo.path("cover");
        if (coverNode.isMissingNode()) {
            return null;
        }
        String cover = textOrNull(coverNode, "large");
        if (cover == null) {
            cover = textOrNull(coverNode, "medium");
        }
        if (cover == null) {
            cover = textOrNull(coverNode, "small");
        }
        if (cover == null) {
            return null;
        }
        return cover.replace("http://", "https://");
    }
}
