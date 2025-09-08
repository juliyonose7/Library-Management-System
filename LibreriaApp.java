import ui.CatalogPanel;
import ui.AuthorsPanel;
import ui.ClientsPanel;
import model.Library;
import model.XMLDatabaseManager;

import javax.swing.*;
import java.awt.*;


public class LibreriaApp {
public static void main(String[] args) {
// inicializar FlatLaf
try {
UIManager.setLookAndFeel(new FlatDarkLaf());
} catch (Exception ex) {
System.err.println("no se pudo inicializar FlatLaf, usando look & feel por defecto.");
}


SwingUtilities.invokeLater(() -> {
// cargar datos desde la base de datos xml
Library library = XMLDatabaseManager.loadFromXML();


        JFrame frame = new JFrame("SGI LIB - Sistema de Gestión de Librería");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 600)); // tamaño minimo
        frame.setSize(1200, 800); // tamaño inicial mas grande
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // iniciar maximizada


JTabbedPane tabs = new JTabbedPane();
tabs.add("Catálogo", new CatalogPanel(library));
tabs.add("Autores", new AuthorsPanel(library));
tabs.add("Clientes", new ClientsPanel(library));


frame.getContentPane().add(tabs, BorderLayout.CENTER);
frame.setVisible(true);
});
}
}