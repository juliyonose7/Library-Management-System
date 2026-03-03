package model;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackendSyncService {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\\\"accessToken\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern NUMBER_FIELD_PATTERN_TEMPLATE = Pattern.compile("__KEY__");

    private final boolean enabled;
    private final String apiBase;
    private final String username;
    private final String password;
    private final HttpClient httpClient;

    private BackendSyncService(boolean enabled, String apiBase, String username, String password) {
        this.enabled = enabled;
        this.apiBase = apiBase;
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static BackendSyncService createFromEnvironment() {
        boolean enabled = Boolean.parseBoolean(readConfig("SGILIB_DESKTOP_API_SYNC", "true"));
        String base = readConfig("SGILIB_API_BASE", "http://localhost:8080/api/v1");
        String user = readConfig("SGILIB_API_USER", "admin");
        String pass = readConfig("SGILIB_API_PASSWORD", "");
        return new BackendSyncService(enabled, base, user, pass);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Library loadLibrarySnapshot() {
        if (!enabled) {
            return null;
        }

        try {
            Library library = new Library();
            library.setAutoSave(false);

            Map<Long, Author> authorsById = new HashMap<>();
            String authorsResponse = request("GET", "/authors?size=1000", null);
            for (String object : parseContentObjects(authorsResponse)) {
                Long id = getLongField(object, "id");
                String name = getStringField(object, "name");
                String nationality = getStringField(object, "nationality");
                if (id == null || name == null || name.trim().isEmpty()) {
                    continue;
                }

                Author author = new Author(name, nationality == null ? "" : nationality);
                authorsById.put(id, author);
                library.getAuthors().add(author);
            }

            String booksResponse = request("GET", "/books?size=1000", null);
            for (String object : parseContentObjects(booksResponse)) {
                String title = getStringField(object, "title");
                String isbn = getStringField(object, "isbn");
                Integer year = getIntegerField(object, "publicationYear");
                Integer stock = getIntegerField(object, "stock");
                Long authorId = getLongField(object, "authorId");
                String category = getStringField(object, "category");
                String coverUrl = getStringField(object, "coverUrl");

                if (title == null || isbn == null || year == null || stock == null) {
                    continue;
                }

                Book book = new Novel(title, isbn, 0.0, year, stock, category == null || category.isBlank() ? "General" : category);
                if (coverUrl != null && !coverUrl.isBlank()) {
                    book.setCustomImagePath(coverUrl);
                }

                Author author = authorId == null ? null : authorsById.get(authorId);
                if (author != null) {
                    book.addAuthor(author);
                    author.addBook(book);
                }

                library.getCatalog().add(book);
            }

            String clientsResponse = request("GET", "/clients?size=1000", null);
            for (String object : parseContentObjects(clientsResponse)) {
                Long id = getLongField(object, "id");
                String firstName = getStringField(object, "firstName");
                String lastName = getStringField(object, "lastName");

                if (id == null) {
                    continue;
                }

                String safeFirstName = firstName == null ? "Cliente" : firstName.trim();
                String safeLastName = lastName == null ? "" : lastName.trim();
                String fullName = (safeFirstName + " " + safeLastName).trim();
                if (fullName.isEmpty()) {
                    fullName = "Cliente " + id;
                }

                library.getClients().add(new Client(fullName, String.valueOf(id)));
            }

            return library;
        } catch (Exception ex) {
            System.err.println("[API-SYNC] no se pudo cargar snapshot desde backend: " + ex.getMessage());
            return null;
        }
    }

    public void syncAuthorUpsert(Author author) {
        if (!enabled || author == null) {
            return;
        }

        try {
            Long authorId = findAuthorIdByName(author.getName());
            String payload = "{" +
                    "\"name\":\"" + jsonEscape(author.getName()) + "\"," +
                    "\"nationality\":\"" + jsonEscape(author.getNationality()) + "\"" +
                    "}";

            if (authorId == null) {
                request("POST", "/authors", payload);
            } else {
                request("PUT", "/authors/" + authorId, payload);
            }
        } catch (Exception ex) {
            System.err.println("[API-SYNC] error sincronizando autor: " + ex.getMessage());
        }
    }

    public void syncAuthorDelete(Author author) {
        if (!enabled || author == null) {
            return;
        }

        try {
            Long authorId = findAuthorIdByName(author.getName());
            if (authorId != null) {
                request("DELETE", "/authors/" + authorId, null);
            }
        } catch (Exception ex) {
            System.err.println("[API-SYNC] error eliminando autor: " + ex.getMessage());
        }
    }

    public void syncClientUpsert(Client client) {
        if (!enabled || client == null) {
            return;
        }

        try {
            String email = buildDesktopEmail(client.getId());
            Long clientId = findClientIdByEmail(email);
            String[] parts = splitName(client.getName());

            String payload = "{" +
                    "\"firstName\":\"" + jsonEscape(parts[0]) + "\"," +
                    "\"lastName\":\"" + jsonEscape(parts[1]) + "\"," +
                    "\"email\":\"" + jsonEscape(email) + "\"" +
                    "}";

            if (clientId == null) {
                request("POST", "/clients", payload);
            } else {
                request("PUT", "/clients/" + clientId, payload);
            }
        } catch (Exception ex) {
            System.err.println("[API-SYNC] error sincronizando cliente: " + ex.getMessage());
        }
    }

    public void syncClientDelete(Client client) {
        if (!enabled || client == null) {
            return;
        }

        try {
            Long clientId = findClientIdByEmail(buildDesktopEmail(client.getId()));
            if (clientId != null) {
                request("DELETE", "/clients/" + clientId, null);
            }
        } catch (Exception ex) {
            System.err.println("[API-SYNC] error eliminando cliente: " + ex.getMessage());
        }
    }

    public void syncBookUpsert(Book book) {
        if (!enabled || book == null || book.getAuthors().isEmpty()) {
            return;
        }

        try {
            Author primaryAuthor = book.getAuthors().get(0);
            syncAuthorUpsert(primaryAuthor);
            Long authorId = findAuthorIdByName(primaryAuthor.getName());
            if (authorId == null) {
                return;
            }

            Long bookId = findBookIdByIsbn(book.getIsbn());
            String payload = "{" +
                    "\"title\":\"" + jsonEscape(book.getTitle()) + "\"," +
                    "\"isbn\":\"" + jsonEscape(book.getIsbn()) + "\"," +
                    "\"publicationYear\":" + book.getYear() + "," +
                    "\"stock\":" + book.getStock() + "," +
                    "\"authorId\":" + authorId +
                    "}";

            if (bookId == null) {
                request("POST", "/books", payload);
            } else {
                request("PUT", "/books/" + bookId, payload);
            }
        } catch (Exception ex) {
            System.err.println("[API-SYNC] error sincronizando libro: " + ex.getMessage());
        }
    }

    public void syncBookDelete(Book book) {
        if (!enabled || book == null) {
            return;
        }

        try {
            Long bookId = findBookIdByIsbn(book.getIsbn());
            if (bookId != null) {
                request("DELETE", "/books/" + bookId, null);
            }
        } catch (Exception ex) {
            System.err.println("[API-SYNC] error eliminando libro: " + ex.getMessage());
        }
    }

    public void syncSale(String isbn, String clientLocalId) {
        if (!enabled || isbn == null || clientLocalId == null) {
            return;
        }

        try {
            Long bookId = findBookIdByIsbn(isbn);
            Long clientId = findClientIdByLocalId(clientLocalId);
            if (bookId == null || clientId == null) {
                return;
            }

            String payload = "{" +
                    "\"clientId\":" + clientId + "," +
                    "\"bookId\":" + bookId + "," +
                    "\"quantity\":1" +
                    "}";

            request("POST", "/sales", payload);
        } catch (Exception ex) {
            System.err.println("[API-SYNC] error registrando venta: " + ex.getMessage());
        }
    }

    private Long findClientIdByLocalId(String clientLocalId) throws IOException, InterruptedException {
        Long clientId = findClientIdByEmail(buildDesktopEmail(clientLocalId));
        if (clientId != null) {
            return clientId;
        }

        if (clientLocalId != null && clientLocalId.matches("\\d+")) {
            long numericId = Long.parseLong(clientLocalId);
            String response = request("GET", "/clients?size=1000", null);
            for (String object : parseContentObjects(response)) {
                Long id = getLongField(object, "id");
                if (id != null && id == numericId) {
                    return id;
                }
            }
        }

        return null;
    }

    private Long findAuthorIdByName(String name) throws IOException, InterruptedException {
        String response = request("GET", "/authors?size=500", null);
        for (String object : parseContentObjects(response)) {
            String currentName = getStringField(object, "name");
            if (currentName != null && currentName.equalsIgnoreCase(name)) {
                return getLongField(object, "id");
            }
        }
        return null;
    }

    private Long findClientIdByEmail(String email) throws IOException, InterruptedException {
        String response = request("GET", "/clients?size=500", null);
        for (String object : parseContentObjects(response)) {
            String currentEmail = getStringField(object, "email");
            if (currentEmail != null && currentEmail.equalsIgnoreCase(email)) {
                return getLongField(object, "id");
            }
        }
        return null;
    }

    private Long findBookIdByIsbn(String isbn) throws IOException, InterruptedException {
        String response = request("GET", "/books?size=500", null);
        for (String object : parseContentObjects(response)) {
            String currentIsbn = getStringField(object, "isbn");
            if (currentIsbn != null && currentIsbn.equalsIgnoreCase(isbn)) {
                return getLongField(object, "id");
            }
        }
        return null;
    }

    private String request(String method, String path, String body) throws IOException, InterruptedException {
        String token = loginAndGetToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiBase + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json");

        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return response.body();
        }
        throw new IOException("status=" + status + " body=" + response.body());
    }

    private String loginAndGetToken() throws IOException, InterruptedException {
        String payload = "{" +
                "\"username\":\"" + jsonEscape(username) + "\"," +
                "\"password\":\"" + jsonEscape(password) + "\"" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBase + "/auth/login"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("login failed status=" + response.statusCode());
        }

        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(response.body());
        if (!matcher.find()) {
            throw new IOException("login token not found");
        }
        return matcher.group(1);
    }

    private static List<String> parseContentObjects(String json) {
        List<String> objects = new ArrayList<>();
        if (json == null) {
            return objects;
        }

        int contentKey = json.indexOf("\"content\"");
        if (contentKey < 0) {
            return objects;
        }

        int arrayStart = json.indexOf('[', contentKey);
        if (arrayStart < 0) {
            return objects;
        }

        int depth = 0;
        int arrayEnd = -1;
        for (int i = arrayStart; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '[') {
                depth++;
            } else if (ch == ']') {
                depth--;
                if (depth == 0) {
                    arrayEnd = i;
                    break;
                }
            }
        }

        if (arrayEnd < 0) {
            return objects;
        }

        String content = json.substring(arrayStart + 1, arrayEnd).trim();
        if (content.isEmpty()) {
            return objects;
        }

        int objDepth = 0;
        int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch == '{') {
                if (objDepth == 0) {
                    start = i;
                }
                objDepth++;
            } else if (ch == '}') {
                objDepth--;
                if (objDepth == 0 && start >= 0) {
                    objects.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        return objects;
    }

    private static String getStringField(String objectJson, String field) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
        Matcher matcher = pattern.matcher(objectJson);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n");
    }

    private static Long getLongField(String objectJson, String field) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(objectJson);
        if (!matcher.find()) {
            return null;
        }
        return Long.parseLong(matcher.group(1));
    }

    private static Integer getIntegerField(String objectJson, String field) {
        Long value = getLongField(objectJson, field);
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    private static String buildDesktopEmail(String localId) {
        String base = localId == null ? "client" : localId.toLowerCase().replaceAll("[^a-z0-9]+", ".");
        base = base.replaceAll("^\\.+|\\.+$", "");
        if (base.isEmpty()) {
            base = "client";
        }
        return base + "@desktop.local";
    }

    private static String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"Cliente", "Desktop"};
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], "Desktop"};
        }
        return parts;
    }

    private static String readConfig(String key, String defaultValue) {
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.trim().isEmpty()) {
            return fromProperty.trim();
        }
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        return defaultValue;
    }

    private static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String encoded(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
