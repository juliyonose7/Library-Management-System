import javax.swing.*;
import java.awt.*;
import javax.swing.JOptionPane;

// Add other necessary imports here

public class CatalogPanel extends JPanel {

	private Library library; // Add this field for the library instance
	private JComboBox<String> clientCombo = new JComboBox<>();
	private JButton btnSearch = new JButton("Buscar");
	// Declare other buttons similarly if needed
	private JButton btnReset = new JButton("Resetear");
	private JButton btnAdd = new JButton("Agregar");
	private JButton btnSell = new JButton("Vender");
	private JTextField searchField = new JTextField(20); // Added searchField

	private BookTableModel tableModel; // Added tableModel declaration
	private JTable table; // Added JTable declaration

	public CatalogPanel(Library library) { // Pass Library as a constructor parameter
		this.library = library;

		setLayout(new BorderLayout());

		tableModel = new BookTableModel(library.getCatalog());
		table = new JTable(tableModel);

		JPanel top = new JPanel();
		top.add(searchField);
		top.add(btnSearch);
		top.add(btnReset);
		add(top, BorderLayout.NORTH);

		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.add(clientCombo);
		bottom.add(btnAdd);
		bottom.add(btnSell);
		add(bottom, BorderLayout.SOUTH);

		refreshClientCombo();

		btnSearch.addActionListener(e -> doSearch());
		btnReset.addActionListener(e -> { searchField.setText(""); doSearch(); });
		btnAdd.addActionListener(e -> {
			AddBookDialog dlg = new AddBookDialog(SwingUtilities.getWindowAncestor(this), library);
			dlg.setVisible(true);
		});
		btnSell.addActionListener(e -> doSell());

	library.setOnCatalogChanged(() -> tableModel.setBooks(library.getCatalog()));
	library.setOnClientsChanged(this::refreshClientCombo);
}


private void doSearch() {
String q = searchField.getText().trim();
if (q.isEmpty()) tableModel.setBooks(library.getCatalog());
else tableModel.setBooks(library.searchByTitleOrAuthor(q));
}


private void doSell() {
int sel = table.getSelectedRow();
if (sel < 0) { JOptionPane.showMessageDialog(this, "Selecciona un libro primero."); return; }
Book b = tableModel.getBookAt(sel);
if (b.getStock() <= 0) { JOptionPane.showMessageDialog(this, "El libro no está disponible (stock 0)."); return; }
int idx = clientCombo.getSelectedIndex();
if (idx < 0) { JOptionPane.showMessageDialog(this, "Debes seleccionar un cliente."); return; }
Client c = library.getClients().get(idx);
boolean ok = library.sellBook(b.getIsbn(), c.getId());
if (ok) JOptionPane.showMessageDialog(this, "Venta realizada: " + b.getTitle() + " → " + c.getName());
else JOptionPane.showMessageDialog(this, "No fue posible realizar la venta.");
}


private void refreshClientCombo() {
clientCombo.removeAllItems();
for (Client c : library.getClients()) clientCombo.addItem(c.getName() + " (" + c.getId() + ")");
}
}