package com.sgilib.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.sgilib.backend.api.dto.GoogleBookMetadataResponse;
import com.sgilib.backend.service.GoogleBooksService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class GoogleBooksServiceImpl implements GoogleBooksService {

    private final RestClient restClient;
    private final boolean enabled;

    public GoogleBooksServiceImpl(RestClient.Builder restClientBuilder,
                                  @Value("${integration.google-books.enabled:true}") boolean enabled) {
        this.restClient = restClientBuilder.baseUrl("https://www.googleapis.com/books/v1").build();
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

        try {
            JsonNode root = restClient.get()
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
            response.setCoverUrl(extractCover(volumeInfo));
            response.setPublicationYear(extractYear(textOrNull(volumeInfo, "publishedDate")));
            response.setPageCount(intOrNull(volumeInfo, "pageCount"));
            return Optional.of(response);
        } catch (Exception ex) {
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

    private static String extractCover(JsonNode volumeInfo) {
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
}
