

import java.util.ArrayList;
import java.util.List;


public class Client {
private final String name;
private final String id;
private final List<Book> purchased = new ArrayList<>();


public Client(String name, String id) { this.name = name; this.id = id; }
public String getName() { return name; }
public String getId() { return id; }
public List<Book> getPurchased() { return purchased; }
public void addPurchased(Book b) { purchased.add(b); }
}