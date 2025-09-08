


import java.util.ArrayList;
import java.util.List;


public class Author {
private final String name;
private final String nationality;
private final List<Book> books = new ArrayList<>();


public Author(String name, String nationality) {
this.name = name;
this.nationality = nationality;
}
public String getName() { return name; }
public String getNationality() { return nationality; }
public List<Book> getBooks() { return books; }
public void addBook(Book b) { if (!books.contains(b)) books.add(b); }
}