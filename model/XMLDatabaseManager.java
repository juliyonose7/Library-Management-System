package model;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XMLDatabaseManager {
    private static final String DATABASE_FILE = "database.xml";
    
    // cargar datos desde xml
    public static Library loadFromXML() {
        Library library = new Library();
        
        try {
            File file = new File(DATABASE_FILE);
            if (!file.exists()) {
                // si no existe el archivo, crear uno vacio
                saveToXML(library);
                return library;
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();
            
            // cargar autores
            loadAuthors(doc, library);
            
            // cargar libros
            loadBooks(doc, library);
            
            // cargar clientes
            loadClients(doc, library);
            
        } catch (Exception e) {
            System.err.println("error cargando xml: " + e.getMessage());
            e.printStackTrace();
        }
        
        return library;
    }
    
    // guardar datos a xml
    public static void saveToXML(Library library) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            // elemento raiz
            Element root = doc.createElement("library");
            doc.appendChild(root);
            
            // guardar autores
            saveAuthors(doc, root, library);
            
            // guardar libros
            saveBooks(doc, root, library);
            
            // guardar clientes
            saveClients(doc, root, library);
            
            // escribir al archivo
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(DATABASE_FILE));
            transformer.transform(source, result);
            
        } catch (Exception e) {
            System.err.println("error guardando xml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void loadAuthors(Document doc, Library library) {
        NodeList authorNodes = doc.getElementsByTagName("author");
        
        for (int i = 0; i < authorNodes.getLength(); i++) {
            Node authorNode = authorNodes.item(i);
            if (authorNode.getNodeType() == Node.ELEMENT_NODE && 
                authorNode.getParentNode().getNodeName().equals("authors")) {
                
                Element authorElement = (Element) authorNode;
                String name = getElementText(authorElement, "name");
                String nationality = getElementText(authorElement, "nationality");
                
                if (name != null && !name.isEmpty()) {
                    library.addAuthor(new Author(name, nationality != null ? nationality : ""));
                }
            }
        }
    }
    
    private static void loadBooks(Document doc, Library library) {
        NodeList bookNodes = doc.getElementsByTagName("book");
        
        for (int i = 0; i < bookNodes.getLength(); i++) {
            Node bookNode = bookNodes.item(i);
            if (bookNode.getNodeType() == Node.ELEMENT_NODE && 
                bookNode.getParentNode().getNodeName().equals("books")) {
                
                Element bookElement = (Element) bookNode;
                
                String title = getElementText(bookElement, "title");
                String isbn = getElementText(bookElement, "isbn");
                double price = Double.parseDouble(getElementText(bookElement, "price"));
                int year = Integer.parseInt(getElementText(bookElement, "year"));
                int stock = Integer.parseInt(getElementText(bookElement, "stock"));
                String type = getElementText(bookElement, "type");
                
                Book book;
                if ("Novel".equals(type)) {
                    String genre = getElementText(bookElement, "genre");
                    book = new Novel(title, isbn, price, year, stock, genre != null ? genre : "General");
                } else {
                    String level = getElementText(bookElement, "level");
                    book = new TextBook(title, isbn, price, year, stock, level != null ? level : "General");
                }
                
                // Agregar autores al libro
                NodeList authorNodes = bookElement.getElementsByTagName("author");
                for (int j = 0; j < authorNodes.getLength(); j++) {
                    String authorName = authorNodes.item(j).getTextContent();
                    Author author = library.findAuthorByName(authorName);
                    if (author != null) {
                        book.addAuthor(author);
                        author.addBook(book);
                    }
                }
                
                // Cargar imagen personalizada si existe
                String customImagePath = getElementText(bookElement, "customImagePath");
                if (customImagePath != null && !customImagePath.isEmpty()) {
                    book.setCustomImagePath(customImagePath);
                }
                
                library.addBook(book);
            }
        }
    }
    
    private static void loadClients(Document doc, Library library) {
        NodeList clientNodes = doc.getElementsByTagName("client");
        
        for (int i = 0; i < clientNodes.getLength(); i++) {
            Node clientNode = clientNodes.item(i);
            if (clientNode.getNodeType() == Node.ELEMENT_NODE) {
                Element clientElement = (Element) clientNode;
                
                String name = getElementText(clientElement, "name");
                String id = getElementText(clientElement, "id");
                
                Client client = new Client(name, id);
                
                // Cargar libros comprados
                NodeList purchasedNodes = clientElement.getElementsByTagName("purchased");
                if (purchasedNodes.getLength() > 0) {
                    Element purchasedElement = (Element) purchasedNodes.item(0);
                    NodeList bookNodes = purchasedElement.getElementsByTagName("book");
                    
                    for (int j = 0; j < bookNodes.getLength(); j++) {
                        Element bookElement = (Element) bookNodes.item(j);
                        String isbn = getElementText(bookElement, "isbn");
                        
                        // Buscar el libro en el catálogo
                        for (Book book : library.getCatalog()) {
                            if (book.getIsbn().equals(isbn)) {
                                client.addPurchased(book);
                                break;
                            }
                        }
                    }
                }
                
                library.addClient(client);
            }
        }
    }
    
    private static void saveAuthors(Document doc, Element root, Library library) {
        Element authorsElement = doc.createElement("authors");
        root.appendChild(authorsElement);
        
        for (Author author : library.getAuthors()) {
            Element authorElement = doc.createElement("author");
            authorsElement.appendChild(authorElement);
            
            Element nameElement = doc.createElement("name");
            nameElement.setTextContent(author.getName());
            authorElement.appendChild(nameElement);
            
            Element nationalityElement = doc.createElement("nationality");
            nationalityElement.setTextContent(author.getNationality());
            authorElement.appendChild(nationalityElement);
        }
    }
    
    private static void saveBooks(Document doc, Element root, Library library) {
        Element booksElement = doc.createElement("books");
        root.appendChild(booksElement);
        
        for (Book book : library.getCatalog()) {
            Element bookElement = doc.createElement("book");
            booksElement.appendChild(bookElement);
            
            addElement(doc, bookElement, "title", book.getTitle());
            addElement(doc, bookElement, "isbn", book.getIsbn());
            addElement(doc, bookElement, "price", String.valueOf(book.getPrice()));
            addElement(doc, bookElement, "year", String.valueOf(book.getYear()));
            addElement(doc, bookElement, "stock", String.valueOf(book.getStock()));
            
            if (book instanceof Novel) {
                addElement(doc, bookElement, "type", "Novel");
                addElement(doc, bookElement, "genre", ((Novel) book).getGenre());
            } else if (book instanceof TextBook) {
                addElement(doc, bookElement, "type", "TextBook");
                addElement(doc, bookElement, "level", ((TextBook) book).getLevel());
            }
            
            // Guardar autores del libro
            Element authorsElement = doc.createElement("authors");
            bookElement.appendChild(authorsElement);
            
            for (Author author : book.getAuthors()) {
                Element authorElement = doc.createElement("author");
                authorElement.setTextContent(author.getName());
                authorsElement.appendChild(authorElement);
            }
            
            // Guardar imagen personalizada si existe
            if (book.hasCustomImage()) {
                addElement(doc, bookElement, "customImagePath", book.getCustomImagePath());
            }
        }
    }
    
    private static void saveClients(Document doc, Element root, Library library) {
        Element clientsElement = doc.createElement("clients");
        root.appendChild(clientsElement);
        
        for (Client client : library.getClients()) {
            Element clientElement = doc.createElement("client");
            clientsElement.appendChild(clientElement);
            
            addElement(doc, clientElement, "name", client.getName());
            addElement(doc, clientElement, "id", client.getId());
            
            // Guardar libros comprados
            Element purchasedElement = doc.createElement("purchased");
            clientElement.appendChild(purchasedElement);
            
            for (Book book : client.getPurchased()) {
                Element bookElement = doc.createElement("book");
                purchasedElement.appendChild(bookElement);
                
                addElement(doc, bookElement, "title", book.getTitle());
                addElement(doc, bookElement, "isbn", book.getIsbn());
            }
        }
    }
    
    private static String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
    
    private static void addElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }
}
