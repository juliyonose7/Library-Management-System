
import javax.swing.*;

public class ClientsPanel {

	private Library library = new Library(); // Add this line to declare and initialize 'library'

	private JList<Client> clientsList = new JList<>(); // Declare and initialize clientsList

	private JTextArea clientDetails = new JTextArea(); // Declare and initialize clientDetails

	private JButton addClientBtn = new JButton("Agregar Cliente"); // Declare and initialize addClientBtn

	private JTextField nameField = new JTextField(); // Declare and initialize nameField
	private JTextField idField = new JTextField();   // Declare and initialize idField

	// Define 'refresh' as a Runnable that refreshes the clients list or UI as needed
	Runnable refresh = () -> {
		clientsList.setListData(library.getClients().toArray(new Client[0]));
	};

	public ClientsPanel() {
		refresh.run();
		library.setOnClientsChanged(refresh);

		clientsList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int idx = clientsList.getSelectedIndex();
				if (idx >= 0) {
					Client c = library.getClients().get(idx);
					StringBuilder sb = new StringBuilder();
					sb.append("Nombre: ").append(c.getName()).append('\n');
					sb.append("ID: ").append(c.getId()).append('\n');
					sb.append("Libros comprados:\n");
					if (c.getPurchased().isEmpty()) sb.append(" (ninguno)\n");
					for (Book b : c.getPurchased()) sb.append(" - ").append(b.getTitle()).append(" (ISBN: ").append(b.getIsbn()).append(")\n");
					clientDetails.setText(sb.toString());
				}
			}
		});

		addClientBtn.addActionListener(e -> {
			String name = nameField.getText().trim();
			String id = idField.getText().trim();
			if (name.isEmpty() || id.isEmpty()) { JOptionPane.showMessageDialog(null, "Nombre e ID son obligatorios."); return; }
			library.addClient(new Client(name, id));
			nameField.setText(""); idField.setText("");
		});
	}
}