package ui;

import model.Author;
import model.Book;
import model.Novel;
import model.TextBook;
import model.Library;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddBookDialog extends JDialog {
    public AddBookDialog(Window owner, Library library) {
        super(owner, "Agregar libro", ModalityType.APPLICATION_MODAL);
        setSize(520, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(6, 6));

        // panel del formulario
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        // campos del formulario
        JTextField titleField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField authorsField = new JTextField();
        JTextField nationalitiesField = new JTextField();
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));

        // radio buttons para tipo de libro
        JRadioButton rbText = new JRadioButton("Libro de texto");
        JRadioButton rbNovel = new JRadioButton("Novela");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbText);
        bg.add(rbNovel);
        rbText.setSelected(true);
        
        // dropdown para generos de novelas
        String[] genres = {
            "Ficción", "Romance", "Misterio", "Thriller", "Ciencia Ficción", 
            "Fantasía", "Horror", "Aventura", "Drama", "Comedia", 
            "Histórica", "Biografía", "Autobiografía", "Ensayo", "Poesía"
        };
        JComboBox<String> genreCombo = new JComboBox<>(genres);
        
        // dropdown para niveles academicos
        String[] levels = {
            "Primaria", "Secundaria", "Preparatoria", "Universidad", 
            "Posgrado", "Maestría", "Doctorado", "Técnico", 
            "Profesional", "Básico", "Intermedio", "Avanzado"
        };
        JComboBox<String> levelCombo = new JComboBox<>(levels);

        // añadir componentes al formulario
        form.add(new JLabel("Título:"));
        form.add(titleField);
        form.add(new JLabel("ISBN:"));
        form.add(isbnField);
        form.add(new JLabel("Precio:"));
        form.add(priceField);
        form.add(new JLabel("Año publicación:"));
        form.add(yearField);
        form.add(new JLabel("Autores (separados por coma):"));
        form.add(authorsField);
        form.add(new JLabel("Nacionalidades (separadas por coma):"));
        form.add(nationalitiesField);
        form.add(new JLabel("Stock:"));
        form.add(stockSpinner);
        form.add(new JLabel("Tipo:"));
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setOpaque(false);
        typePanel.add(rbText);
        typePanel.add(rbNovel);
        form.add(typePanel);
        
        // labels y componentes dinamicos
        JLabel levelLabel = new JLabel("Nivel académico:");
        JLabel genreLabel = new JLabel("Género:");
        
        form.add(levelLabel);
        form.add(levelCombo);
        form.add(genreLabel);
        form.add(genreCombo);
        
        // configurar visibilidad inicial (libro de texto seleccionado por defecto)
        levelLabel.setVisible(true);
        levelCombo.setVisible(true);
        genreLabel.setVisible(false);
        genreCombo.setVisible(false);

        add(form, BorderLayout.CENTER);

        // panel de botones
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton btnCancel = new JButton("Cancelar");
        JButton btnAutoFetch = new JButton("Buscar datos automáticamente");
        JButton btnAdd = new JButton("Agregar");
        buttons.add(btnCancel);
        buttons.add(btnAutoFetch);
        buttons.add(btnAdd);
        add(buttons, BorderLayout.SOUTH);

        // listeners para cambiar visibilidad segun tipo de libro
        rbText.addActionListener(e -> {
            levelLabel.setVisible(true);
            levelCombo.setVisible(true);
            genreLabel.setVisible(false);
            genreCombo.setVisible(false);
            form.revalidate();
            form.repaint();
        });
        
        rbNovel.addActionListener(e -> {
            levelLabel.setVisible(false);
            levelCombo.setVisible(false);
            genreLabel.setVisible(true);
            genreCombo.setVisible(true);
            form.revalidate();
            form.repaint();
        });

        // event listeners
        btnCancel.addActionListener(e -> dispose());
        
        // funcionalidad de busqueda automatica de datos usando google books api
        // permite al usuario obtener informacion completa del libro con un solo clic
        btnAutoFetch.addActionListener(e -> {
            // obtener los datos que el usuario ya ingreso
            String title = titleField.getText().trim();
            String isbn = isbnField.getText().trim();
            String author = authorsField.getText().trim();
            
            // validar que haya al menos titulo o isbn para buscar
            if (title.isEmpty() && isbn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ingresa al menos el titulo o isbn para buscar.");
                return;
            }
            
            // cambiar boton para mostrar que esta procesando
            btnAutoFetch.setEnabled(false);
            btnAutoFetch.setText("buscando...");
            
            // ejecutar busqueda en hilo separado para no congelar la interfaz
            // esto es importante para mantener la responsividad de la aplicacion
            SwingUtilities.invokeLater(() -> {
                try {
                    model.BookApiService.BookApiResult result = null;
                    
                    // estrategia de busqueda: isbn primero (mas preciso)
                    if (!isbn.isEmpty()) {
                        result = model.BookApiService.searchByIsbn(isbn);
                    }
                    
                    // si no encontro por isbn, buscar por titulo y autor
                    if (result == null && !title.isEmpty()) {
                        // usar solo el primer autor si hay varios separados por coma
                        String authorName = author.split(",")[0].trim();
                        result = model.BookApiService.searchBook(title, authorName);
                    }
                    
                    if (result != null) {
                        // llenar campos solo si estan vacios para no sobrescribir datos del usuario
                        
                        // completar titulo si esta vacio
                        if (result.title != null && !result.title.isEmpty() && titleField.getText().trim().isEmpty()) {
                            titleField.setText(result.title);
                        }
                        
                        // completar isbn si esta vacio
                        if (result.isbn != null && !result.isbn.isEmpty() && isbnField.getText().trim().isEmpty()) {
                            isbnField.setText(result.isbn);
                        }
                        
                        // completar autores y nacionalidades si estan vacios
                        if (!result.authors.isEmpty() && authorsField.getText().trim().isEmpty()) {
                            authorsField.setText(String.join(", ", result.authors));
                            nationalitiesField.setText(String.join(", ", result.authorNationalities));
                        }
                        
                        // completar ano de publicacion si esta vacio
                        if (result.publishedYear != null && yearField.getText().trim().isEmpty()) {
                            yearField.setText(String.valueOf(result.publishedYear));
                        }
                        
                        // si se encontro genero, configurar automaticamente como novela
                        if (result.genre != null && !result.genre.isEmpty()) {
                            // cambiar a novela y mostrar dropdown de generos
                            rbNovel.setSelected(true);
                            rbNovel.getActionListeners()[0].actionPerformed(null); // activar cambio de visibilidad
                            
                            // buscar genero similar en el dropdown
                            for (int i = 0; i < genreCombo.getItemCount(); i++) {
                                String item = genreCombo.getItemAt(i);
                                // busqueda flexible: si contiene el genero o viceversa
                                if (result.genre.toLowerCase().contains(item.toLowerCase()) || 
                                    item.toLowerCase().contains(result.genre.toLowerCase())) {
                                    genreCombo.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                        
                        // mostrar mensaje de exito al usuario
                        JOptionPane.showMessageDialog(this, 
                            "datos encontrados y completados automaticamente.\\n" +
                            "revisa la informacion antes de agregar el libro.",
                            "busqueda exitosa", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // mostrar mensaje cuando no se encuentran resultados
                        JOptionPane.showMessageDialog(this, 
                            "no se encontraron datos para este libro.\\n" +
                            "verifica el titulo, isbn o conexion a internet.",
                            "sin resultados", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    // manejar errores de la api o conexion
                    JOptionPane.showMessageDialog(this, 
                        "error durante la busqueda: " + ex.getMessage(),
                        "error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // siempre restaurar el boton al estado original
                    btnAutoFetch.setEnabled(true);
                    btnAutoFetch.setText("buscar datos automaticamente");
                }
            });
        });
        btnAdd.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String isbn = isbnField.getText().trim();
                String priceStr = priceField.getText().trim();
                String yearStr = yearField.getText().trim();
                String authorsStr = authorsField.getText().trim();
                String nationalitiesStr = nationalitiesField.getText().trim();
                int stock = (Integer) stockSpinner.getValue();

                if (title.isEmpty() || isbn.isEmpty() || priceStr.isEmpty() || yearStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos básicos son obligatorios.");
                    return;
                }

                double price = Double.parseDouble(priceStr);
                int year = Integer.parseInt(yearStr);

                Book book;
                if (rbText.isSelected()) {
                    String level = (String) levelCombo.getSelectedItem();
                    book = new TextBook(title, isbn, price, year, stock, level);
                } else {
                    String genre = (String) genreCombo.getSelectedItem();
                    book = new Novel(title, isbn, price, year, stock, genre);
                }

                // procesar autores y nacionalidades
                if (!authorsStr.isEmpty()) {
                    List<String> authorNames = Arrays.stream(authorsStr.split(","))
                            .map(String::trim)
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toList());
                    
                    List<String> nationalities = Arrays.stream(nationalitiesStr.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    for (int i = 0; i < authorNames.size(); i++) {
                        String authorName = authorNames.get(i);
                        String nationality = (i < nationalities.size() && !nationalities.get(i).isEmpty()) 
                                ? nationalities.get(i) : "No especificada";
                        
                        Author author = library.findAuthorByName(authorName);
                        if (author == null) {
                            author = new Author(authorName, nationality);
                            library.addAuthor(author);
                        }
                        book.addAuthor(author);
                        author.addBook(book);
                    }
                }

                // agregar el libro a la biblioteca
                library.addBook(book);
                
                // intentar enriquecer automaticamente con datos de la api en segundo plano
                // esto busca imagen de portada y metadatos faltantes sin interrumpir al usuario
                SwingUtilities.invokeLater(() -> {
                    try {
                        boolean enriched = model.BookApiService.enrichBook(book);
                        if (enriched) {
                            // guardar cambios solo si se agrego informacion nueva
                            model.XMLDatabaseManager.saveToXML(library);
                        }
                    } catch (Exception ex) {
                        // ignorar errores de enriquecimiento ya que no son criticos
                        // el libro ya fue agregado exitosamente
                        System.err.println("error enriqueciendo libro: " + ex.getMessage());
                    }
                });
                
                JOptionPane.showMessageDialog(this, "libro agregado exitosamente.");
                dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio y año deben ser números válidos.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al agregar el libro: " + ex.getMessage());
            }
        });
    }
}