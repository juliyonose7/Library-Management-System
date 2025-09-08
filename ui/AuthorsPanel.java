package ui;

import model.Author;
import model.Book;
import model.Library;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AuthorsPanel extends JPanel {
    public AuthorsPanel(Library library) {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // lista de autores
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> authorsList = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(authorsList);

        // detalles del autor
        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setPreferredSize(new Dimension(400, 300));

        // container para dividir la vista
        JPanel container = new JPanel(new GridLayout(1, 2, 12, 12));
        container.setOpaque(false);
        container.add(scroll);
        container.add(detailsScroll);
        add(container, BorderLayout.CENTER);

        // panel para gestionar autores
        JPanel managePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        managePanel.setOpaque(false);
        JTextField nameField = new JTextField(15);
        JTextField natField = new JTextField(10);
        JButton addAuthorBtn = new JButton("Agregar autor");
        JButton deleteAuthorBtn = new JButton("Eliminar autor seleccionado");
        
        managePanel.add(new JLabel("Nombre:"));
        managePanel.add(nameField);
        managePanel.add(new JLabel("Nacionalidad:"));
        managePanel.add(natField);
        managePanel.add(addAuthorBtn);
        managePanel.add(deleteAuthorBtn);
        add(managePanel, BorderLayout.SOUTH);

        // funcion para refrescar la lista
        Runnable refresh = () -> {
            listModel.clear();
            for (Author a : library.getAuthors()) {
                String displayText = a.getName() + " (" + a.getNationality() + ")";
                listModel.addElement(displayText);
            }
        };
        refresh.run();
        library.setOnAuthorsChanged(refresh);

        // listener para mostrar detalles del autor seleccionado
        authorsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = authorsList.getSelectedValue();
                if (sel != null) {
                    // extraer solo el nombre (antes del parentesis)
                    String authorName = sel.split(" \\(")[0];
                    Author a = library.findAuthorByName(authorName);
                    if (a != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Nombre: ").append(a.getName()).append('\n');
                        sb.append("Nacionalidad: ").append(a.getNationality()).append('\n');
                        sb.append("Libros escritos: ").append(a.getBooks().size()).append('\n');
                        sb.append("Libros:\n");
                        if (a.getBooks().isEmpty()) {
                            sb.append(" (ninguno)\n");
                        } else {
                            for (Book b : a.getBooks()) {
                                sb.append(" - ").append(b.getTitle()).append(" (ISBN: ").append(b.getIsbn()).append(")\n");
                            }
                        }
                        details.setText(sb.toString());
                    }
                }
            }
        });

        // listener para agregar autor
        addAuthorBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String nat = natField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
                return;
            }
            if (nat.isEmpty()) {
                nat = "No especificada";
            }
            library.addAuthor(new Author(name, nat));
            nameField.setText("");
            natField.setText("");
        });
        
        // Listener para eliminar autor
        deleteAuthorBtn.addActionListener(e -> {
            String sel = authorsList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un autor primero.");
                return;
            }
            
            // Extraer solo el nombre (antes del paréntesis)
            String authorName = sel.split(" \\(")[0];
            Author author = library.findAuthorByName(authorName);
            
            if (author == null) {
                JOptionPane.showMessageDialog(this, "Autor no encontrado.");
                return;
            }
            
            // Verificar si el autor tiene libros
            if (!author.getBooks().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "El autor '" + author.getName() + "' tiene " + author.getBooks().size() + 
                    " libro(s) asociado(s).\n¿Estás seguro de que quieres eliminarlo?\n" +
                    "Esto también eliminará la relación con los libros.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                
                // Remover el autor de todos sus libros
                for (Book book : new java.util.ArrayList<>(author.getBooks())) {
                    book.getAuthors().remove(author);
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que quieres eliminar al autor '" + author.getName() + "'?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            library.removeAuthor(author);
            JOptionPane.showMessageDialog(this, "Autor eliminado exitosamente.");
        });
    }
}