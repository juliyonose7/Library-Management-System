package model;

public class TextBook extends Book {
    private final String level;
    
    public TextBook(String title, String isbn, double price, int year, int stock, String level) {
        super(title, isbn, price, year, stock);
        this.level = level;
    }
    
    public String getLevel() { 
        return level; 
    }
    
    @Override
    public String getTypeSpecific() { 
        return "Libro de texto (" + level + ")"; 
    }
}