package model;


import java.util.ArrayList;
import java.util.List;


public abstract class Book {
private final String title;
private final String isbn;
private final double price;
private final int year;
private final List<Author> authors = new ArrayList<>();
private int stock;
private String customImagePath; // nueva propiedad para imagen personalizada


public Book(String title, String isbn, double price, int year, int stock) {
this.title = title;
this.isbn = isbn;
this.price = price;
this.year = year;
this.stock = stock;
this.customImagePath = null; // inicialmente sin imagen personalizada
}


public String getTitle() { return title; }
public String getIsbn() { return isbn; }
public double getPrice() { return price; }
public int getYear() { return year; }
public List<Author> getAuthors() { return authors; }
public void addAuthor(Author a) { if (!authors.contains(a)) authors.add(a); }
public int getStock() { return stock; }
public void setStock(int stock) { this.stock = stock; }
public boolean decrementStock() { if (stock>0) { stock--; return true; } return false; }
public abstract String getTypeSpecific();

// metodos para manejar imagen personalizada
public String getCustomImagePath() { return customImagePath; }
public void setCustomImagePath(String path) { this.customImagePath = path; }
public boolean hasCustomImage() { return customImagePath != null && !customImagePath.isEmpty(); }
public void removeCustomImage() { this.customImagePath = null; }
}