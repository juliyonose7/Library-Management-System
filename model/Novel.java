package model;


public class Novel extends Book {
private final String genre;
public Novel(String title, String isbn, double price, int year, int stock, String genre) {
super(title, isbn, price, year, stock);
this.genre = genre;
}
public String getGenre() { return genre; }
@Override
public String getTypeSpecific() { return "Novela (" + genre + ")"; }
}