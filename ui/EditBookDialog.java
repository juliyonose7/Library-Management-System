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

public class EditBookDialog extends JDialog {
    private final Book originalBook;
    private final Library library;
    
    public EditBookDialog(Window owner, Library library, Book book) {
        super(owner, "Editar libro", ModalityType.APPLICATION_MODAL);
        this.library = library;
        this.originalBook = book;
        
        setSize(520, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(6, 6));

        // panel del formulario
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        // campos del formulario prellenados con datos actuales
        JTextField titleField = new JTextField(book.getTitle());
        JTextField isbnField = new JTextField(book.getIsbn());
        JTextField priceField = new JTextField(String.valueOf(book.getPrice()));
        JTextField yearField = new JTextField(String.valueOf(book.getYear()));
        
        // prellenar autores y nacionalidades
        String authorsStr = book.getAuthors().stream()
                .map(Author::getName)
                .collect(Collectors.joining(", "));
        String nationalitiesStr = book.getAuthors().stream()
                .map(Author::getNationality)
                .collect(Collectors.joining(", "));
        
        JTextField authorsField = new JTextField(authorsStr);
        JTextField nationalitiesField = new JTextField(nationalitiesStr);
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(book.getStock(), 0, 10000, 1));

        // radio buttons para tipo de libro
        JRadioButton rbText = new JRadioButton("Libro de texto");
        JRadioButton rbNovel = new JRadioButton("Novela");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbText);
        bg.add(rbNovel);
        
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
        
        // configurar segun el tipo actual del libro
        if (book instanceof TextBook) {
            rbText.setSelected(true);
            levelCombo.setSelectedItem(((TextBook) book).getLevel());
        } else if (book instanceof Novel) {
            rbNovel.setSelected(true);
            genreCombo.setSelectedItem(((Novel) book).getGenre());
        }

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
        
        // configurar visibilidad inicial segun el tipo actual
        if (book instanceof TextBook) {
            levelLabel.setVisible(true);
            levelCombo.setVisible(true);
            genreLabel.setVisible(false);
            genreCombo.setVisible(false);
        } else {
            levelLabel.setVisible(false);
            levelCombo.setVisible(false);
            genreLabel.setVisible(true);
            genreCombo.setVisible(true);
        }

        add(form, BorderLayout.CENTER);

        // panel de botones
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton btnCancel = new JButton("Cancelar");
        JButton btnAutoFetch = new JButton("Buscar imagen automáticamente");
        JButton btnSave = new JButton("Guardar cambios");
        buttons.add(btnCancel);
        buttons.add(btnAutoFetch);
        buttons.add(btnSave);
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
        
        // funcionalidad de busqueda automatica de imagen usando google books api
        // permite al usuario obtener la portada real del libro desde google books
        btnAutoFetch.addActionListener(e -> {
            // verificar si el libro ya tiene imagen personalizada
            if (originalBook.hasCustomImage()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "este libro ya tiene una imagen personalizada.\\n" +
                    "¿quieres reemplazarla con una imagen de google books?",
                    "confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION);
                
                // si el usuario no confirma, cancelar la operacion
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // cambiar boton para mostrar que esta procesando
            btnAutoFetch.setEnabled(false);
            btnAutoFetch.setText("buscando imagen...");
            
            // ejecutar busqueda en hilo separado para no bloquear la interfaz
            SwingUtilities.invokeLater(() -> {
                try {
                    model.BookApiService.BookApiResult result = null;
                    
                    // estrategia de busqueda: isbn primero (mas preciso)
                    if (originalBook.getIsbn() != null && !originalBook.getIsbn().trim().isEmpty()) {
                        result = model.BookApiService.searchByIsbn(originalBook.getIsbn());
                    }
                    
                    // si no encontro por isbn, buscar por titulo y autor
                    if (result == null) {
                        String authorName = "";
                        if (!originalBook.getAuthors().isEmpty()) {
                            authorName = originalBook.getAuthors().get(0).getName();
                        }
                        result = model.BookApiService.searchBook(originalBook.getTitle(), authorName);
                    }
                    
                    // verificar si se encontro resultado con imagen
                    if (result != null && result.hasImage()) {
                        // descargar la imagen de google books
                        String identifier = originalBook.getIsbn() != null ? 
                            originalBook.getIsbn() : originalBook.getTitle();
                        String localImagePath = model.BookApiService.downloadImage(
                            result.imageUrl, identifier);
                        
                        if (localImagePath != null) {
                            // asignar la nueva imagen al libro para preview inmediato
                            originalBook.setCustomImagePath(localImagePath);
                            
                            // notificar al usuario del exito
                            JOptionPane.showMessageDialog(this, 
                                "imagen descargada y asignada exitosamente.\\n" +
                                "la imagen se actualizara al guardar los cambios.",
                                "imagen encontrada", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            // error en la descarga
                            JOptionPane.showMessageDialog(this, 
                                "se encontro una imagen pero no se pudo descargar.\\n" +
                                "verifica la conexion a internet.",
                                "error de descarga", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        // no se encontro imagen
                        JOptionPane.showMessageDialog(this, 
                            "no se encontro una imagen para este libro.\\n" +
                            "verifica el titulo, isbn o conexion a internet.",
                            "sin imagen", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    // manejar errores de la api o conexion
                    JOptionPane.showMessageDialog(this, 
                        "error durante la busqueda: " + ex.getMessage(),
                        "error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // siempre restaurar el boton al estado original
                    btnAutoFetch.setEnabled(true);
                    btnAutoFetch.setText("buscar imagen automaticamente");
                }
            });
        });
        btnSave.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String isbn = isbnField.getText().trim();
                String priceStr = priceField.getText().trim();
                String yearStr = yearField.getText().trim();
                String authorsStr2 = authorsField.getText().trim();
                String nationalitiesStr2 = nationalitiesField.getText().trim();
                int stock = (Integer) stockSpinner.getValue();

                if (title.isEmpty() || isbn.isEmpty() || priceStr.isEmpty() || yearStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos básicos son obligatorios.");
                    return;
                }

                double price = Double.parseDouble(priceStr);
                int year = Integer.parseInt(yearStr);

                // crear el libro actualizado
                Book updatedBook;
                if (rbText.isSelected()) {
                    String level = (String) levelCombo.getSelectedItem();
                    updatedBook = new TextBook(title, isbn, price, year, stock, level);
                } else {
                    String genre = (String) genreCombo.getSelectedItem();
                    updatedBook = new Novel(title, isbn, price, year, stock, genre);
                }

                // copiar imagen personalizada si la tenia
                if (originalBook.hasCustomImage()) {
                    updatedBook.setCustomImagePath(originalBook.getCustomImagePath());
                }

                // limpiar autores del libro original
                cleanupOldAuthors();

                // procesar nuevos autores y nacionalidades
                if (!authorsStr2.isEmpty()) {
                    List<String> authorNames = Arrays.stream(authorsStr2.split(","))
                            .map(String::trim)
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toList());
                    
                    List<String> nationalities = Arrays.stream(nationalitiesStr2.split(","))
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
                        updatedBook.addAuthor(author);
                        author.addBook(updatedBook);
                    }
                }

                // reemplazar el libro en la biblioteca
                library.removeBook(originalBook);
                library.addBook(updatedBook);

                JOptionPane.showMessageDialog(this, "Libro actualizado exitosamente.");
                dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio y año deben ser números válidos.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar el libro: " + ex.getMessage());
            }
        });
    }
    
    private void cleanupOldAuthors() {
        // obtener los autores del libro original antes de limpiarlo
        List<Author> authorsToCheck = new java.util.ArrayList<>(originalBook.getAuthors());
        
        // remover este libro de cada autor
        for (Author author : authorsToCheck) {
            author.getBooks().remove(originalBook);
            // si el autor se quedo sin libros, eliminarlo de la biblioteca
            if (author.getBooks().isEmpty()) {
                library.removeAuthor(author);
            }
        }
    }
}
