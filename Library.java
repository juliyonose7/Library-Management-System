

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Library {
private final List<Book> catalog = new ArrayList<>();
private final List<Author> authors = new ArrayList<>();
private final List<Client> clients = new ArrayList<>();


private Runnable onAuthorsChanged = () -> {};
private Runnable onClientsChanged = () -> {};
private Runnable onCatalogChanged = () -> {};


public List<Book> getCatalog() { return catalog; }
public List<Author> getAuthors() { return authors; }
public List<Client> getClients() { return clients; }


public void setOnAuthorsChanged(Runnable r) { this.onAuthorsChanged = r; }
public void setOnClientsChanged(Runnable r) { this.onClientsChanged = r; }
public void setOnCatalogChanged(Runnable r) { this.onCatalogChanged = r; }


public void addBook(Book b) { catalog.add(b); onCatalogChanged.run(); }
public void addAuthor(Author a) { authors.add(a); onAuthorsChanged.run(); }
public void addClient(Client c) { clients.add(c); onClientsChanged.run(); }


public Author findAuthorByName(String name) {
for (Author a : authors) if (a.getName().equalsIgnoreCase(name)) return a;
return null;
}


public List<Book> searchByTitleOrAuthor(String q) {
String ql = q.toLowerCase();
return catalog.stream().filter(b -> b.getTitle().toLowerCase().contains(ql)
|| b.getAuthors().stream().anyMatch(a -> a.getName().toLowerCase().contains(ql)))
.collect(Collectors.toList());
}


public boolean sellBook(String isbn, String clientId) {
Book book = catalog.stream().filter(b -> b.getIsbn().equals(isbn)).findFirst().orElse(null);
Client client = clients.stream().filter(c -> c.getId().equals(clientId)).findFirst().orElse(null);
if (book == null || client == null) return false;
if (!book.decrementStock()) return false;
client.addPurchased(book);
onCatalogChanged.run();
onClientsChanged.run();
return true;
}
}