


// Update the import to the correct package, for example:
// import your.package.name.Book; // Remove or replace with the actual package, e.g.:
// import com.example.model.Book; // Replace with the actual package path if Book is in a different package


import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class BookTableModel extends AbstractTableModel {
private List<Book> books = new ArrayList<>();
private final String[] cols = {"Título", "ISBN", "Precio", "Año", "Autores", "Tipo", "Stock"};


public BookTableModel(List<Book> books) { setBooks(books); }
public void setBooks(List<Book> newBooks) { this.books = new ArrayList<>(newBooks); fireTableDataChanged(); }
public Book getBookAt(int row) { return books.get(row); }


@Override
public int getRowCount() { return books.size(); }
@Override
public int getColumnCount() { return cols.length; }
@Override
public String getColumnName(int col) { return cols[col]; }
@Override
public Object getValueAt(int rowIndex, int columnIndex) {
Book b = books.get(rowIndex);
switch (columnIndex) {
case 0: return b.getTitle();
case 1: return b.getIsbn();
case 2: return String.format("$ %.2f", b.getPrice());
case 3: return b.getYear();
case 4: return b.getAuthors().stream().map(Author::getName).collect(Collectors.joining(", "));
case 5: return b.getTypeSpecific();
case 6: return b.getStock();
}
return null;
}
}