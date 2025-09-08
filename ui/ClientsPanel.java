package ui;

import model.Book;
import model.Client;
import model.Library;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientsPanel extends JPanel {
    public ClientsPanel(Library library) {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // lista de clientes
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> clientsList = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(clientsList);

        // detalles del cliente
        JTextArea clientDetails = new JTextArea();
        clientDetails.setEditable(false);
        clientDetails.setLineWrap(true);
        clientDetails.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(clientDetails);
        detailsScroll.setPreferredSize(new Dimension(400, 300));

        // container para dividir la vista
        JPanel container = new JPanel(new GridLayout(1, 2, 12, 12));
        container.setOpaque(false);
        container.add(scroll);
        container.add(detailsScroll);
        add(container, BorderLayout.CENTER);

        // panel para gestionar clientes
        JPanel managePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        managePanel.setOpaque(false);
        JTextField nameField = new JTextField(15);
        JTextField idField = new JTextField(10);
        JButton addClientBtn = new JButton("Agregar cliente");
        JButton deleteClientBtn = new JButton("Eliminar cliente seleccionado");
        
        managePanel.add(new JLabel("Nombre:"));
        managePanel.add(nameField);
        managePanel.add(new JLabel("ID:"));
        managePanel.add(idField);
        managePanel.add(addClientBtn);
        managePanel.add(deleteClientBtn);
        add(managePanel, BorderLayout.SOUTH);

        // funcion para refrescar la lista
        Runnable refresh = () -> {
            listModel.clear();
            for (Client c : library.getClients()) {
                listModel.addElement(c.getName() + " (" + c.getId() + ")");
            }
        };
        refresh.run();
        library.setOnClientsChanged(refresh);

        // listener para mostrar detalles del cliente seleccionado
        clientsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = clientsList.getSelectedIndex();
                if (idx >= 0) {
                    Client c = library.getClients().get(idx);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Nombre: ").append(c.getName()).append('\n');
                    sb.append("ID: ").append(c.getId()).append('\n');
                    sb.append("Libros comprados:\n");
                    if (c.getPurchased().isEmpty()) {
                        sb.append(" (ninguno)\n");
                    }
                    for (Book b : c.getPurchased()) {
                        sb.append(" - ").append(b.getTitle()).append(" (ISBN: ").append(b.getIsbn()).append(")\n");
                    }
                    clientDetails.setText(sb.toString());
                }
            }
        });

        // listener para agregar cliente
        addClientBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String id = idField.getText().trim();
            if (name.isEmpty() || id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre e ID son obligatorios.");
                return;
            }
            
            // verificar si ya existe un cliente con ese id
            for (Client existingClient : library.getClients()) {
                if (existingClient.getId().equals(id)) {
                    JOptionPane.showMessageDialog(this, "Ya existe un cliente con ese ID.");
                    return;
                }
            }
            
            library.addClient(new Client(name, id));
            nameField.setText("");
            idField.setText("");
            JOptionPane.showMessageDialog(this, "Cliente agregado exitosamente.");
        });
        
        // listener para eliminar cliente
        deleteClientBtn.addActionListener(e -> {
            int selectedIndex = clientsList.getSelectedIndex();
            if (selectedIndex < 0) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente primero.");
                return;
            }
            
            Client client = library.getClients().get(selectedIndex);
            
            // verificar si el cliente ha comprado libros
            if (!client.getPurchased().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "El cliente '" + client.getName() + "' ha comprado " + client.getPurchased().size() + 
                    " libro(s).\n¿Estás seguro de que quieres eliminarlo?\n" +
                    "Se perderá el historial de compras.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que quieres eliminar al cliente '" + client.getName() + "'?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            library.removeClient(client);
            JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente.");
        });
    }
}