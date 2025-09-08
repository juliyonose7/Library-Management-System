package ui;

import model.Book;
import model.Client;
import model.Library;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class CatalogPanel extends JPanel {
    private final Library library;
    private final BookTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final JComboBox<String> clientCombo;
    private final JLabel bookCoverLabel;
    private final JPanel bookInfoPanel;
    private final JButton btnSell;

    public CatalogPanel(Library library) {
        this.library = library;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // crear modelo de tabla y tabla
        tableModel = new BookTableModel(library.getCatalog());
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true); // hace que la tabla use todo el espacio disponible
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // crear panel principal con la tabla
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // crear panel de vista previa del libro (con mas espacio)
        bookInfoPanel = new JPanel(new BorderLayout(8, 8));
        bookInfoPanel.setBorder(new TitledBorder("Vista Previa del Libro Seleccionado"));
        bookInfoPanel.setPreferredSize(new Dimension(0, 450)); // altura aun mas grande
        bookInfoPanel.setMinimumSize(new Dimension(700, 400)); // tamaño minimo aumentado mas
        
        // panel contenedor para la portada (centrado)
        JPanel coverContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        coverContainer.setOpaque(false);
        
        // label para la portada del libro
        bookCoverLabel = new JLabel();
        bookCoverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bookCoverLabel.setVerticalAlignment(SwingConstants.CENTER);
        bookCoverLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        bookCoverLabel.setText("<html><div style='text-align: center; color: gray; font-size: 12px;'>" +
                              "<br><br>» Selecciona un libro de la tabla<br>para ver su portada generada<br><br></div></html>");
        
        coverContainer.add(bookCoverLabel);
        bookInfoPanel.add(coverContainer, BorderLayout.CENTER);
        
        // usar JSplitPane para mejor distribucion del espacio
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, bookInfoPanel);
        splitPane.setResizeWeight(0.45); // 45% para la tabla, 55% para la vista previa - mas espacio para vista previa
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(8); // divisor mas grueso para mejor control
        splitPane.setDividerLocation(300); // posicion inicial del divisor
        
        add(splitPane, BorderLayout.CENTER);

        // panel superior con busqueda
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        searchField = new JTextField(20);
        JButton btnSearch = new JButton("Buscar");
        JButton btnReset = new JButton("Mostrar todo");
        JButton btnAdd = new JButton("Agregar libro");
        JButton btnEdit = new JButton("Editar libro");
        JButton btnDelete = new JButton("Eliminar libro");
        JButton btnEnrichAll = new JButton("Buscar imágenes faltantes");
        top.add(new JLabel("Buscar:"));
        top.add(searchField);
        top.add(btnSearch);
        top.add(btnReset);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnEnrichAll);
        add(top, BorderLayout.NORTH);

        // panel inferior con venta
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        btnSell = new JButton("Vender libro seleccionado a:");
        clientCombo = new JComboBox<>();
        bottom.add(btnSell);
        bottom.add(clientCombo);
        add(bottom, BorderLayout.SOUTH);

        // Inicializar combo de clientes y estado del botón
        refreshClientCombo();
        btnSell.setEnabled(!library.getClients().isEmpty());

        // Event listeners
        btnSearch.addActionListener(e -> doSearch());
        btnReset.addActionListener(e -> {
            searchField.setText("");
            doSearch();
        });
        btnAdd.addActionListener(e -> {
            AddBookDialog dlg = new AddBookDialog(SwingUtilities.getWindowAncestor(this), library);
            dlg.setVisible(true);
        });
        btnEdit.addActionListener(e -> doEdit());
        btnDelete.addActionListener(e -> doDelete());
        btnEnrichAll.addActionListener(e -> doEnrichAll());
        btnSell.addActionListener(e -> doSell());

        // Listener para selección en la tabla
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBookPreview();
            }
        });
        
        // Listeners para cambios en la biblioteca
        library.setOnCatalogChanged(() -> {
            tableModel.setBooks(library.getCatalog());
            updateBookPreview(); // Actualizar vista previa cuando cambie el catálogo
        });
        
        // Listener para cambios en clientes - actualiza el combo de venta
        library.setOnClientsChanged(() -> {
            refreshClientCombo();
            // También actualizar el botón para reflejar si hay clientes disponibles
            btnSell.setEnabled(!library.getClients().isEmpty());
        });
        
        // Listener para redimensionado de la ventana - actualiza la vista previa
        bookInfoPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Retraso pequeño para evitar múltiples actualizaciones durante el redimensionado
                SwingUtilities.invokeLater(() -> updateBookPreview());
            }
        });
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) {
            tableModel.setBooks(library.getCatalog());
        } else {
            tableModel.setBooks(library.searchByTitleOrAuthor(q));
        }
    }

    private void doEdit() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro primero.");
            return;
        }
        Book b = tableModel.getBookAt(sel);
        EditBookDialog dlg = new EditBookDialog(SwingUtilities.getWindowAncestor(this), library, b);
        dlg.setVisible(true);
    }

    private void doDelete() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro primero.");
            return;
        }
        Book b = tableModel.getBookAt(sel);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Estás seguro de que quieres eliminar el libro '" + b.getTitle() + "'?",
            "Confirmar eliminación", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Eliminar automáticamente autores sin libros
            removeBookAndCleanupAuthors(b);
            JOptionPane.showMessageDialog(this, "Libro eliminado exitosamente.");
        }
    }

    private void doSell() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro primero.");
            return;
        }
        Book b = tableModel.getBookAt(sel);
        if (b.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "El libro no está disponible (stock 0).");
            return;
        }
        int idx = clientCombo.getSelectedIndex();
        if (idx < 0 || library.getClients().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar un cliente. Agrega clientes en la pestaña 'Clientes' primero.");
            return;
        }
        Client c = library.getClients().get(idx);
        boolean ok = library.sellBook(b.getIsbn(), c.getId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Venta realizada: " + b.getTitle() + " → " + c.getName());
        } else {
            JOptionPane.showMessageDialog(this, "No fue posible realizar la venta.");
        }
    }

    private void refreshClientCombo() {
        clientCombo.removeAllItems();
        
        if (library.getClients().isEmpty()) {
            clientCombo.addItem("-- No hay clientes registrados --");
            clientCombo.setEnabled(false);
        } else {
            clientCombo.setEnabled(true);
            for (Client c : library.getClients()) {
                clientCombo.addItem(c.getName() + " (" + c.getId() + ")");
            }
        }
    }
    
    private void updateBookPreview() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            Book selectedBook = tableModel.getBookAt(selectedRow);
            
            // Calcular tamaño de portada dinámicamente basado en el espacio disponible
            int availableWidth = bookInfoPanel.getWidth();
            int availableHeight = bookInfoPanel.getHeight();
            
            // Algoritmo mejorado para pantallas grandes - usar más espacio disponible
            int maxCoverWidth = Math.max(250, availableWidth / 4); // Mínimo 250px, máximo 1/4 del ancho
            int maxCoverHeight = Math.max(300, (int)(availableHeight * 0.8)); // Mínimo 300px, máximo 80% de la altura
            
            // Calcular tamaño final manteniendo proporción de libro (1:1.4)
            int coverWidth = Math.min(maxCoverWidth, (int)(maxCoverHeight / 1.4));
            int coverHeight = (int) (coverWidth * 1.4);
            
            // Para pantallas muy grandes, permitir tamaños aún mayores
            if (availableWidth > 1400) {
                coverWidth = Math.min(400, coverWidth * 2);
                coverHeight = (int) (coverWidth * 1.4);
            }
            
            // Generar portada del libro con tamaño responsivo
            ImageIcon coverIcon = BookCoverGenerator.generateBookCover(selectedBook, coverWidth, coverHeight);
            bookCoverLabel.setIcon(coverIcon);
            bookCoverLabel.setText("");
            
            // Crear panel de información detallada (al lado de la portada)
            JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 2, 2));
            detailsPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
            detailsPanel.setOpaque(false);
            
            // Información del libro con mejor formato
            detailsPanel.add(createInfoLabel("» Título", selectedBook.getTitle()));
            
            if (!selectedBook.getAuthors().isEmpty()) {
                if (selectedBook.getAuthors().size() == 1) {
                    model.Author author = selectedBook.getAuthors().get(0);
                    String authorInfo = author.getName() + " (" + author.getNationality() + ")";
                    detailsPanel.add(createInfoLabel("» Autor", authorInfo));
                } else {
                    String authors = selectedBook.getAuthors().get(0).getName() + " y otros (" + 
                        selectedBook.getAuthors().size() + " autores)";
                    detailsPanel.add(createInfoLabel("» Autores", authors));
                }
            }
            
            detailsPanel.add(createInfoLabel("» Precio", String.format("$%.2f", selectedBook.getPrice())));
            detailsPanel.add(createInfoLabel("» Stock", String.valueOf(selectedBook.getStock())));
            detailsPanel.add(createInfoLabel("» Año", String.valueOf(selectedBook.getYear())));
            detailsPanel.add(createInfoLabel("» ISBN", selectedBook.getIsbn()));
            detailsPanel.add(createInfoLabel("» Tipo", selectedBook.getTypeSpecific()));
            
            // Agregar información sobre imagen personalizada
            if (selectedBook.hasCustomImage()) {
                detailsPanel.add(createInfoLabel("» Imagen", "Personalizada"));
            } else {
                detailsPanel.add(createInfoLabel("» Imagen", "Generada automáticamente"));
            }
            
            // Panel de botones para gestionar imagen
            JPanel imageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            imageButtonsPanel.setOpaque(false);
            
            JButton btnAddImage = new JButton("Agregar Imagen");
            JButton btnRemoveImage = new JButton("Eliminar Imagen");
            btnRemoveImage.setEnabled(selectedBook.hasCustomImage());
            
            btnAddImage.addActionListener(e -> addCustomImage(selectedBook));
            btnRemoveImage.addActionListener(e -> removeCustomImage(selectedBook));
            
            imageButtonsPanel.add(btnAddImage);
            imageButtonsPanel.add(btnRemoveImage);
            detailsPanel.add(imageButtonsPanel);
            
            // Limpiar y reorganizar el panel
            bookInfoPanel.removeAll();
            
            // Panel contenedor principal horizontal
            JPanel contentPanel = new JPanel(new BorderLayout(15, 5));
            contentPanel.setOpaque(false);
            
            // Panel izquierdo para la portada - con más espacio
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            leftPanel.setOpaque(false);
            leftPanel.setPreferredSize(new Dimension(coverWidth + 40, coverHeight + 20)); // Espacio extra alrededor
            leftPanel.add(bookCoverLabel);
            
            contentPanel.add(leftPanel, BorderLayout.WEST);
            contentPanel.add(detailsPanel, BorderLayout.CENTER);
            
            bookInfoPanel.add(contentPanel, BorderLayout.CENTER);
            
        } else {
            // No hay libro seleccionado
            bookInfoPanel.removeAll();
            
            bookCoverLabel.setIcon(null);
            bookCoverLabel.setText("<html><div style='text-align: center; color: gray; font-size: 12px;'>" +
                                  "<br><br>» Selecciona un libro de la tabla<br>para ver su portada generada<br><br></div></html>");
            
            JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerPanel.setOpaque(false);
            centerPanel.add(bookCoverLabel);
            bookInfoPanel.add(centerPanel, BorderLayout.CENTER);
        }
        
        bookInfoPanel.revalidate();
        bookInfoPanel.repaint();
    }
    
    /**
     * procesa todos los libros del catalogo para buscar y asignar imagenes faltantes
     * utiliza google books api para encontrar portadas reales de los libros
     * incluye barra de progreso y la capacidad de cancelar el proceso
     */
    private void doEnrichAll() {
        java.util.List<Book> booksNeedingEnrichment = new java.util.ArrayList<>();
        
        // identificar todos los libros que necesitan imagen de portada
        for (Book book : library.getCatalog()) {
            if (!book.hasCustomImage()) {
                booksNeedingEnrichment.add(book);
            }
        }
        
        // verificar si hay libros para procesar
        if (booksNeedingEnrichment.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "todos los libros ya tienen imagenes asignadas.",
                "sin libros para enriquecer", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // confirmar con el usuario antes de iniciar el proceso
        int confirm = JOptionPane.showConfirmDialog(this,
            "se buscaran imagenes para " + booksNeedingEnrichment.size() + " libro(s).\\n" +
            "este proceso puede tomar varios minutos.\\n" +
            "¿continuar?",
            "confirmar busqueda masiva",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // crear dialogo de progreso para mostrar el avance del procesamiento
        JDialog progressDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
            "buscando imagenes", true);
        
        // configurar barra de progreso
        JProgressBar progressBar = new JProgressBar(0, booksNeedingEnrichment.size());
        progressBar.setStringPainted(true); // mostrar porcentaje como texto
        JLabel statusLabel = new JLabel("preparando busqueda...");
        
        // crear panel del dialogo de progreso
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBorder(new javax.swing.border.EmptyBorder(20, 20, 20, 20));
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // configurar el dialogo
        progressDialog.add(progressPanel);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(this);
        
        // ejecutar enriquecimiento en hilo separado usando swingworker
        // esto permite actualizar la ui mientras se procesa en segundo plano
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                int processed = 0;
                int enriched = 0;
                
                // procesar cada libro individualmente
                for (Book book : booksNeedingEnrichment) {
                    // verificar si el usuario cancelo el proceso
                    if (isCancelled()) break;
                    
                    // actualizar el estado mostrado al usuario
                    publish("procesando: " + book.getTitle());
                    
                    try {
                        // intentar enriquecer el libro con datos de la api
                        boolean wasEnriched = model.BookApiService.enrichBook(book);
                        if (wasEnriched) {
                            enriched++;
                        }
                        
                        // pausa de 1 segundo para no sobrecargar la api de google books
                        // esto es importante para ser respetuosos con el servicio
                        Thread.sleep(1000);
                        
                    } catch (Exception e) {
                        // continuar con el siguiente libro si hay error
                        System.err.println("error enriqueciendo " + book.getTitle() + ": " + e.getMessage());
                    }
                    
                    // actualizar contadores y progreso
                    processed++;
                    setProgress(processed);
                }
                
                // guardar cambios en la base de datos solo si hubo mejoras
                if (enriched > 0) {
                    model.XMLDatabaseManager.saveToXML(library);
                }
                
                // mostrar resumen final
                publish("completado: " + enriched + " libros enriquecidos de " + processed + " procesados");
                return null;
            }
            
            /**
             * metodo llamado periodicamente para actualizar la interfaz
             * recibe los mensajes publicados desde doinbackground
             */
            @Override
            protected void process(java.util.List<String> chunks) {
                if (!chunks.isEmpty()) {
                    // actualizar el texto de estado con el ultimo mensaje
                    statusLabel.setText(chunks.get(chunks.size() - 1));
                    // actualizar la barra de progreso
                    progressBar.setValue(getProgress());
                    progressBar.setString(getProgress() + " / " + progressBar.getMaximum());
                }
            }
            
            /**
             * metodo llamado cuando el procesamiento termina o es cancelado
             */
            @Override
            protected void done() {
                // cerrar el dialogo de progreso
                progressDialog.dispose();
                // actualizar la vista previa para mostrar nuevas imagenes
                updateBookPreview();
                // notificar al usuario que el proceso termino
                JOptionPane.showMessageDialog(CatalogPanel.this,
                    "proceso completado. revisa los libros para ver las nuevas imagenes.",
                    "enriquecimiento completado", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        // boton para cancelar el proceso en cualquier momento
        JButton cancelButton = new JButton("cancelar");
        cancelButton.addActionListener(e -> {
            worker.cancel(true); // cancelar el worker
            progressDialog.dispose(); // cerrar dialogo
        });
        progressPanel.add(cancelButton, BorderLayout.SOUTH);
        
        // iniciar el procesamiento y mostrar el dialogo
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    private void removeBookAndCleanupAuthors(Book book) {
        // Obtener los autores del libro antes de eliminarlo
        java.util.List<model.Author> authorsToCheck = new java.util.ArrayList<>(book.getAuthors());
        
        // Eliminar el libro
        library.removeBook(book);
        
        // Verificar si algún autor se quedó sin libros y eliminarlo
        for (model.Author author : authorsToCheck) {
            if (author.getBooks().isEmpty()) {
                library.removeAuthor(author);
            }
        }
    }
    
    private JLabel createInfoLabel(String title, String value) {
        String text = String.format("<html><b>%s:</b> %s</html>", title, value);
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return label;
    }
    
    private void addCustomImage(Book book) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar imagen para " + book.getTitle());
        
        // Configurar filtros para archivos de imagen
        javax.swing.filechooser.FileNameExtensionFilter filter = 
            new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de imagen", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            book.setCustomImagePath(imagePath);
            updateBookPreview(); // Actualizar la vista previa
            
            // Guardar cambios en la base de datos XML
            model.XMLDatabaseManager.saveToXML(library);
            
            JOptionPane.showMessageDialog(this, 
                "Imagen agregada exitosamente para: " + book.getTitle(),
                "Imagen Agregada", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void removeCustomImage(Book book) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de que quieres eliminar la imagen personalizada de '" + book.getTitle() + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            book.removeCustomImage();
            updateBookPreview(); // Actualizar la vista previa
            
            // Guardar cambios en la base de datos XML
            model.XMLDatabaseManager.saveToXML(library);
            
            JOptionPane.showMessageDialog(this,
                "Imagen eliminada exitosamente",
                "Imagen Eliminada",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}