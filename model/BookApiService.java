package model;

// importaciones necesarias para conectar con la api de google books
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * servicio para conectar con la api de google books y obtener informacion de libros
 * automaticamente incluyendo imagenes de portada, metadatos, autores, etc.
 * 
 * este servicio es completamente autonomo y no requiere dependencias externas,
 * incluye su propio parseador de json para evitar conflictos de dependencias
 */
public class BookApiService {
    // url base de la api de google books para buscar volumenes de libros
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes";
    
    // directorio local donde se guardaran las imagenes descargadas
    private static final String IMAGES_DIR = "book_images";
    
    // bloque estatico que se ejecuta al cargar la clase por primera vez
    static {
        // crear el directorio de imagenes si no existe ya
        // esto asegura que siempre tengamos un lugar donde guardar las portadas
        try {
            Files.createDirectories(Paths.get(IMAGES_DIR));
        } catch (IOException e) {
            System.err.println("error creando directorio de imagenes: " + e.getMessage());
        }
    }
    
    /**
     * clase interna para almacenar los resultados de busqueda de la api
     * contiene toda la informacion que se puede extraer de google books
     */
    public static class BookApiResult {
        // informacion basica del libro
        public String title;           // titulo del libro
        public String isbn;            // codigo isbn internacional
        public String imageUrl;        // url de la imagen de portada
        
        // informacion de autores (pueden ser multiples)
        public List<String> authors = new ArrayList<>();
        public List<String> authorNationalities = new ArrayList<>();
        
        // metadatos adicionales
        public String description;     // descripcion o resumen del libro
        public String genre;          // genero literario
        public Integer publishedYear; // ano de publicacion
        public String publisher;      // editorial
        
        /**
         * verifica si el resultado incluye una imagen de portada
         * @return true si hay url de imagen valida
         */
        public boolean hasImage() {
            return imageUrl != null && !imageUrl.isEmpty();
        }
        
        /**
         * verifica si el resultado tiene metadatos utiles
         * @return true si tiene autores, descripcion o genero
         */
        public boolean hasMetadata() {
            return !authors.isEmpty() || description != null || genre != null;
        }
    }
    
    /**
     * busca informacion de un libro usando titulo y autor como parametros
     * esta es la busqueda mas flexible pero menos precisa que por isbn
     * 
     * @param title titulo del libro a buscar
     * @param author nombre del autor (opcional, puede ser null)
     * @return resultado con la informacion encontrada o null si no hay resultados
     */
    public static BookApiResult searchBook(String title, String author) {
        try {
            // construir la consulta de busqueda
            String query = title;
            
            // si se proporciono autor, agregarlo a la busqueda para mayor precision
            if (author != null && !author.trim().isEmpty()) {
                query += " author:" + author.trim();
            }
            
            // codificar la consulta para url (espacios, caracteres especiales, etc)
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            
            // construir la url completa limitando a 1 resultado para eficiencia
            String urlString = GOOGLE_BOOKS_API + "?q=" + encodedQuery + "&maxResults=1";
            
            // ejecutar la peticion http y procesar respuesta
            return makeApiRequest(urlString);
            
        } catch (Exception e) {
            System.err.println("error buscando libro por titulo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * busca informacion de un libro usando su codigo isbn
     * este es el metodo mas preciso ya que isbn es unico por libro
     * 
     * @param isbn codigo isbn del libro (con o sin guiones)
     * @return resultado con la informacion encontrada o null si no hay resultados
     */
    public static BookApiResult searchByIsbn(String isbn) {
        try {
            // validar que se proporciono un isbn
            if (isbn == null || isbn.trim().isEmpty()) {
                return null;
            }
            
            // limpiar el isbn removiendo guiones y espacios, dejando solo numeros y X
            String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
            
            // construir url de busqueda especifica por isbn
            String urlString = GOOGLE_BOOKS_API + "?q=isbn:" + cleanIsbn;
            
            // ejecutar la peticion http y procesar respuesta
            return makeApiRequest(urlString);
            
        } catch (Exception e) {
            System.err.println("error buscando libro por isbn: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * ejecuta una peticion http get a la url especificada y procesa la respuesta
     * este metodo maneja toda la comunicacion con la api de google books
     * 
     * @param urlString url completa para hacer la peticion
     * @return resultado parseado o null si hay error
     */
    private static BookApiResult makeApiRequest(String urlString) {
        try {
            // crear objeto url y abrir conexion http
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // configurar la peticion como get
            conn.setRequestMethod("GET");
            
            // establecer user-agent para identificar nuestra aplicacion
            conn.setRequestProperty("User-Agent", "SGI-LIB/1.0");
            
            // verificar el codigo de respuesta http
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("codigo de respuesta de api: " + responseCode);
                return null;
            }
            
            // leer la respuesta json linea por linea
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            // construir la respuesta completa
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            // parsear el json recibido y retornar el resultado
            return parseApiResponse(response.toString());
            
        } catch (Exception e) {
            System.err.println("error en peticion de api: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * parsea la respuesta json de google books api usando metodos nativos de java
     * no requiere librerias externas para evitar conflictos de dependencias
     * 
     * @param jsonResponse respuesta json cruda de la api
     * @return objeto con los datos extraidos o null si no hay resultados
     */
    private static BookApiResult parseApiResponse(String jsonResponse) {
        try {
            // verificar si la respuesta contiene resultados
            // la api retorna un campo "items" cuando encuentra libros
            if (!jsonResponse.contains("\"items\"")) {
                return null; // no hay resultados en la busqueda
            }
            
            // crear objeto para almacenar los resultados extraidos
            BookApiResult result = new BookApiResult();
            
            // extraer la seccion de items que contiene los libros encontrados
            String itemsSection = extractJsonValue(jsonResponse, "items");
            if (itemsSection == null || !itemsSection.startsWith("[")) {
                return null; // formato invalido de respuesta
            }
            
            // buscar el primer volumeinfo dentro del primer item
            // volumeinfo contiene toda la informacion del libro
            int volumeInfoStart = itemsSection.indexOf("\"volumeInfo\"");
            if (volumeInfoStart == -1) {
                return null; // no se encontro informacion del volumen
            }
            
            // extraer el objeto volumeinfo completo
            String volumeInfoSection = extractJsonObject(itemsSection, volumeInfoStart);
            
            // extraer el titulo del libro
            result.title = extractJsonString(volumeInfoSection, "title");
            
            // extraer los autores (pueden ser multiples)
            String authorsSection = extractJsonValue(volumeInfoSection, "authors");
            if (authorsSection != null && authorsSection.startsWith("[")) {
                String[] authors = extractJsonArray(authorsSection);
                for (String author : authors) {
                    if (!author.trim().isEmpty()) {
                        // limpiar comillas y agregar a la lista
                        result.authors.add(author.trim().replaceAll("\"", ""));
                        // google books no proporciona nacionalidad, usar valor por defecto
                        result.authorNationalities.add("no especificada");
                    }
                }
            }
            
            // extraer el isbn del libro
            String identifiersSection = extractJsonValue(volumeInfoSection, "industryIdentifiers");
            if (identifiersSection != null && identifiersSection.startsWith("[")) {
                result.isbn = extractIsbnFromIdentifiers(identifiersSection);
            }
            
            // extraer la url de la imagen de portada
            String imageLinksSection = extractJsonValue(volumeInfoSection, "imageLinks");
            if (imageLinksSection != null) {
                // preferir thumbnail, si no esta disponible usar smallthumbnail
                String imageUrl = extractJsonString(imageLinksSection, "thumbnail");
                if (imageUrl == null) {
                    imageUrl = extractJsonString(imageLinksSection, "smallThumbnail");
                }
                result.imageUrl = imageUrl;
            }
            
            // extraer la descripcion del libro
            result.description = extractJsonString(volumeInfoSection, "description");
            
            // extraer las categorias para determinar el genero
            String categoriesSection = extractJsonValue(volumeInfoSection, "categories");
            if (categoriesSection != null && categoriesSection.startsWith("[")) {
                String[] categories = extractJsonArray(categoriesSection);
                if (categories.length > 0) {
                    // usar la primera categoria como genero principal
                    result.genre = categories[0].trim().replaceAll("\"", "");
                }
            }
            
            // extraer el ano de publicacion
            String publishedDate = extractJsonString(volumeInfoSection, "publishedDate");
            if (publishedDate != null && publishedDate.length() >= 4) {
                try {
                    // tomar solo los primeros 4 caracteres para el ano
                    result.publishedYear = Integer.parseInt(publishedDate.substring(0, 4));
                } catch (Exception e) {
                    // ignorar errores de parsing de fecha
                }
            }
            
            // extraer la editorial
            result.publisher = extractJsonString(volumeInfoSection, "publisher");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("error parseando respuesta de api: " + e.getMessage());
            return null;
        }
    }
    
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }
        
        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) {
            return null;
        }
        
        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // String value
            return extractJsonString(json, key);
        } else if (firstChar == '[') {
            // Array value
            return extractJsonArray(json, valueStart);
        } else if (firstChar == '{') {
            // Object value
            return extractJsonObject(json, valueStart);
        } else {
            // Number or boolean
            int valueEnd = valueStart;
            while (valueEnd < json.length() && 
                   json.charAt(valueEnd) != ',' && 
                   json.charAt(valueEnd) != '}' && 
                   json.charAt(valueEnd) != ']') {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }
    
    private static String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }
        
        int valueStart = json.indexOf('"', keyIndex + searchKey.length());
        if (valueStart == -1) {
            return null;
        }
        
        valueStart++; // Skip opening quote
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            if (json.charAt(valueEnd) == '"' && json.charAt(valueEnd - 1) != '\\') {
                break;
            }
            valueEnd++;
        }
        
        if (valueEnd >= json.length()) {
            return null;
        }
        
        return json.substring(valueStart, valueEnd);
    }
    
    private static String extractJsonArray(String json, int startIndex) {
        int braceCount = 0;
        int i = startIndex;
        
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                braceCount++;
            } else if (c == ']') {
                braceCount--;
                if (braceCount == 0) {
                    break;
                }
            }
        }
        
        return json.substring(startIndex, i + 1);
    }
    
    private static String extractJsonObject(String json, int startIndex) {
        int braceCount = 0;
        int i = startIndex;
        
        // Find the opening brace
        while (i < json.length() && json.charAt(i) != '{') {
            i++;
        }
        
        if (i >= json.length()) {
            return null;
        }
        
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    break;
                }
            }
        }
        
        return json.substring(startIndex, i + 1);
    }
    
    private static String[] extractJsonArray(String arrayJson) {
        if (!arrayJson.startsWith("[") || !arrayJson.endsWith("]")) {
            return new String[0];
        }
        
        String content = arrayJson.substring(1, arrayJson.length() - 1).trim();
        if (content.isEmpty()) {
            return new String[0];
        }
        
        List<String> items = new ArrayList<>();
        int start = 0;
        boolean inQuotes = false;
        int braceLevel = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') {
                    braceLevel++;
                } else if (c == '}') {
                    braceLevel--;
                } else if (c == ',' && braceLevel == 0) {
                    items.add(content.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        
        if (start < content.length()) {
            items.add(content.substring(start).trim());
        }
        
        return items.toArray(new String[0]);
    }
    
    private static String extractIsbnFromIdentifiers(String identifiersJson) {
        String[] identifiers = extractJsonArray(identifiersJson);
        for (String identifier : identifiers) {
            if (identifier.contains("ISBN")) {
                String isbn = extractJsonString(identifier, "identifier");
                if (isbn != null) {
                    return isbn;
                }
            }
        }
        return null;
    }
    
    /**
     * descarga una imagen desde una url y la guarda en el directorio local de imagenes
     * crea un nombre de archivo unico basado en el isbn para evitar conflictos
     * 
     * @param imageUrl url completa de la imagen a descargar
     * @param isbn codigo isbn del libro para generar nombre unico de archivo
     * @return ruta absoluta del archivo descargado o null si hay error
     */
    public static String downloadImage(String imageUrl, String isbn) {
        // validar parametros de entrada
        if (imageUrl == null || imageUrl.isEmpty() || isbn == null || isbn.isEmpty()) {
            return null;
        }
        
        try {
            // abrir conexion para descargar la imagen
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "SGI-LIB/1.0");
            
            // generar nombre de archivo unico basado en isbn
            // remover caracteres especiales del isbn para nombre de archivo valido
            String fileName = "book_" + isbn.replaceAll("[^a-zA-Z0-9]", "") + ".jpg";
            String filePath = IMAGES_DIR + File.separator + fileName;
            
            // descargar y guardar la imagen usando streams
            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(filePath)) {
                
                // buffer para leer datos en bloques de 4kb para eficiencia
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                // leer y escribir la imagen en bloques
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            // retornar la ruta absoluta del archivo descargado
            return new File(filePath).getAbsolutePath();
            
        } catch (Exception e) {
            System.err.println("error descargando imagen: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * enriquece un libro existente con informacion adicional de google books api
     * solo busca y agrega datos que falten (imagenes, isbn, metadatos)
     * es el metodo principal para mejorar libros que ya estan en el sistema
     * 
     * @param book libro existente a enriquecer
     * @return true si se agrego alguna informacion nueva, false si no se cambio nada
     */
    public static boolean enrichBook(Book book) {
        try {
            // verificar si el libro necesita enriquecimiento
            boolean needsData = needsEnrichment(book);
            if (!needsData) {
                return false; // el libro ya tiene toda la informacion necesaria
            }
            
            BookApiResult result = null;
            
            // estrategia de busqueda: isbn primero (mas preciso)
            if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
                result = searchByIsbn(book.getIsbn());
            }
            
            // si no encontro por isbn, buscar por titulo y autor
            if (result == null && book.getTitle() != null) {
                String authorName = "";
                // usar el primer autor si existe
                if (!book.getAuthors().isEmpty()) {
                    authorName = book.getAuthors().get(0).getName();
                }
                result = searchBook(book.getTitle(), authorName);
            }
            
            // si no se encontro informacion, no hay nada que hacer
            if (result == null) {
                return false;
            }
            
            boolean updated = false;
            
            // agregar imagen de portada si no tiene una personalizada
            if (!book.hasCustomImage() && result.hasImage()) {
                // usar isbn como identificador, o titulo si no hay isbn
                String identifier = book.getIsbn() != null ? book.getIsbn() : book.getTitle();
                String localImagePath = downloadImage(result.imageUrl, identifier);
                
                if (localImagePath != null) {
                    book.setCustomImagePath(localImagePath);
                    updated = true;
                }
            }
            
            // nota: no se puede actualizar isbn en libros existentes porque es final
            // pero esta logica estaria aqui para futuras versiones que lo permitan
            if ((book.getIsbn() == null || book.getIsbn().trim().isEmpty()) && 
                result.isbn != null && !result.isbn.isEmpty()) {
                // en el modelo actual book, el isbn es final
                // pero se considera para nuevos libros
                updated = true;
            }
            
            return updated;
            
        } catch (Exception e) {
            System.err.println("error enriqueciendo libro: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * verifica si un libro necesita enriquecimiento analizando que datos le faltan
     * evalua imagen personalizada, isbn y nacionalidades de autores
     * 
     * @param book libro a evaluar
     * @return true si necesita enriquecimiento, false si esta completo
     */
    private static boolean needsEnrichment(Book book) {
        // criterios para determinar si un libro necesita enriquecimiento:
        
        // 1. no tiene imagen personalizada (usa imagen generada automaticamente)
        if (!book.hasCustomImage()) {
            return true;
        }
        
        // 2. no tiene isbn o esta vacio
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            return true;
        }
        
        // 3. no tiene autores o algun autor no tiene nacionalidad especificada
        for (Author author : book.getAuthors()) {
            if (author.getNationality() == null || 
                author.getNationality().trim().isEmpty() || 
                "no especificada".equals(author.getNationality().toLowerCase())) {
                return true;
            }
        }
        
        // si llego aqui, el libro tiene toda la informacion necesaria
        return false;
    }
}
