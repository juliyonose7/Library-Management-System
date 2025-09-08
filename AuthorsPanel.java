import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class AuthorsPanel extends JPanel {
	public AuthorsPanel(Library library) {
		setLayout(new BorderLayout(8,8));
		setBorder(new EmptyBorder(12,12,12,12));


DefaultListModel<String> listModel = new DefaultListModel<>();
JList<String> authorsList = new JList<>(listModel);
JScrollPane scroll = new JScrollPane(authorsList);


JTextArea details = new JTextArea();
details.setEditable(false); details.setLineWrap(true); details.setWrapStyleWord(true);
JScrollPane detailsScroll = new JScrollPane(details);
detailsScroll.setPreferredSize(new Dimension(400, 300));


JPanel container = new JPanel(new GridLayout(1,2,12,12));
container.setOpaque(false);
container.add(scroll); container.add(detailsScroll);
add(container, BorderLayout.CENTER);


JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
addPanel.setOpaque(false);
JTextField nameField = new JTextField(15);
JTextField natField = new JTextField(10);
JButton addAuthorBtn = new JButton("Agregar autor");
addPanel.add(new JLabel("Nombre:")); addPanel.add(nameField);
addPanel.add(new JLabel("Nacionalidad:")); addPanel.add(natField);
addPanel.add(addAuthorBtn);
add(addPanel, BorderLayout.SOUTH);


Runnable refresh = () -> {
listModel.clear();
for (Author a : library.getAuthors()) listModel.addElement(a.getName());
};
refresh.run();
library.setOnAuthorsChanged(refresh);


authorsList.addListSelectionListener(e -> {
	if (!e.getValueIsAdjusting()) {
		String sel = authorsList.getSelectedValue();
		if (sel != null) {
			Author a = library.findAuthorByName(sel);
			if (a != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("Nacionalidad: ").append(a.getNationality()).append('\n');
				sb.append("Libros:\n");
				for (Book b : a.getBooks()) {
					sb.append(" - ").append(b.getTitle()).append(" (ISBN: ").append(b.getIsbn()).append(")\n");
				}
				details.setText(sb.toString());
			} else {
				details.setText("");
			}
		} else {
			details.setText("");
		}
	}
});


addAuthorBtn.addActionListener(e -> {
String name = nameField.getText().trim();
String nat = natField.getText().trim();
if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "El nombre es obligatorio."); return; }
library.addAuthor(new Author(name, nat));
nameField.setText(""); natField.setText("");
});
}
}