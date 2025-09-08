package ui;

import model.Book;
import model.Novel;
import model.TextBook;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BookCoverGenerator {
    
    private static final Color[] NOVEL_COLORS = {
        new Color(139, 69, 19),   // SaddleBrown
        new Color(128, 0, 128),   // Purple
        new Color(220, 20, 60),   // Crimson
        new Color(25, 25, 112),   // MidnightBlue
        new Color(139, 0, 139),   // DarkMagenta
        new Color(85, 107, 47),   // DarkOliveGreen
        new Color(72, 61, 139),   // DarkSlateBlue
        new Color(165, 42, 42)    // Brown
    };
    
    private static final Color[] TEXTBOOK_COLORS = {
        new Color(70, 130, 180),  // SteelBlue
        new Color(32, 178, 170),  // LightSeaGreen
        new Color(255, 140, 0),   // DarkOrange
        new Color(34, 139, 34),   // ForestGreen
        new Color(205, 92, 92),   // IndianRed
        new Color(123, 104, 238), // MediumSlateBlue
        new Color(220, 220, 220), // Gainsboro
        new Color(169, 169, 169)  // DarkGray
    };
    
    public static ImageIcon generateBookCover(Book book, int width, int height) {
        // si el libro tiene una imagen personalizada, usarla
        if (book.hasCustomImage()) {
            return loadCustomImage(book.getCustomImagePath(), width, height);
        }
        
        // si no, generar portada automatica
        return generateAutomaticCover(book, width, height);
    }
    
    private static ImageIcon loadCustomImage(String imagePath, int targetWidth, int targetHeight) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("imagen no encontrada: " + imagePath);
                return null;
            }
            
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                System.err.println("no se pudo cargar la imagen: " + imagePath);
                return null;
            }
            
            // redimensionar la imagen manteniendo la proporcion
            BufferedImage resizedImage = resizeImage(originalImage, targetWidth, targetHeight);
            return new ImageIcon(resizedImage);
            
        } catch (IOException e) {
            System.err.println("error al cargar imagen: " + e.getMessage());
            return null;
        }
    }
    
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // calcular dimensiones manteniendo proporcion
        double scaleX = (double) targetWidth / originalImage.getWidth();
        double scaleY = (double) targetHeight / originalImage.getHeight();
        double scale = Math.min(scaleX, scaleY);
        
        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);
        
        // crear imagen redimensionada
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // configurar calidad de renderizado
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // fondo negro para bordes
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, targetWidth, targetHeight);
        
        // centrar la imagen
        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;
        
        g2d.drawImage(originalImage, x, y, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    private static ImageIcon generateAutomaticCover(Book book, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Habilitar antialiasing para mejor calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Seleccionar color de fondo basado en el tipo de libro
        Color[] colors = (book instanceof Novel) ? NOVEL_COLORS : TEXTBOOK_COLORS;
        Color backgroundColor = colors[Math.abs(book.getTitle().hashCode()) % colors.length];
        
        // Crear gradiente de fondo
        GradientPaint gradient = new GradientPaint(
            0, 0, backgroundColor,
            width, height, backgroundColor.darker()
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Agregar borde decorativo
        g2d.setColor(backgroundColor.darker().darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(5, 5, width - 10, height - 10);
        
        // Configurar fuente para el título
        Font titleFont = new Font("Serif", Font.BOLD, 14);
        g2d.setFont(titleFont);
        g2d.setColor(Color.WHITE);
        
        // Dibujar título con wordwrap
        String title = book.getTitle();
        if (title.length() > 25) {
            title = title.substring(0, 22) + "...";
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        String[] words = title.split(" ");
        StringBuilder line = new StringBuilder();
        int y = 30;
        int lineHeight = fm.getHeight();
        
        for (String word : words) {
            if (fm.stringWidth(line + word + " ") > width - 20) {
                // Dibujar línea actual
                String currentLine = line.toString().trim();
                int x = (width - fm.stringWidth(currentLine)) / 2;
                g2d.drawString(currentLine, x, y);
                y += lineHeight;
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
            
            if (y > height - 60) break; // Evitar que el texto se salga del área
        }
        
        // Dibujar última línea
        if (line.length() > 0) {
            String lastLine = line.toString().trim();
            int x = (width - fm.stringWidth(lastLine)) / 2;
            g2d.drawString(lastLine, x, y);
        }
        
        // Dibujar información del autor
        Font authorFont = new Font("SansSerif", Font.ITALIC, 10);
        g2d.setFont(authorFont);
        g2d.setColor(Color.LIGHT_GRAY);
        
        if (!book.getAuthors().isEmpty()) {
            String author = book.getAuthors().get(0).getName();
            if (author.length() > 20) {
                author = author.substring(0, 17) + "...";
            }
            fm = g2d.getFontMetrics();
            int x = (width - fm.stringWidth(author)) / 2;
            g2d.drawString(author, x, height - 40);
        }
        
        // Dibujar tipo de libro
        String type = (book instanceof Novel) ? 
            "Novela - " + ((Novel) book).getGenre() : 
            "Libro de Texto - " + ((TextBook) book).getLevel();
        
        if (type.length() > 25) {
            type = type.substring(0, 22) + "...";
        }
        
        Font typeFont = new Font("SansSerif", Font.PLAIN, 9);
        g2d.setFont(typeFont);
        fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(type)) / 2;
        g2d.drawString(type, x, height - 25);
        
        // Dibujar año
        String year = String.valueOf(book.getYear());
        g2d.drawString(year, width - fm.stringWidth(year) - 10, height - 10);
        
        // Dibujar precio
        String price = String.format("$%.2f", book.getPrice());
        g2d.setColor(Color.YELLOW);
        Font priceFont = new Font("SansSerif", Font.BOLD, 10);
        g2d.setFont(priceFont);
        fm = g2d.getFontMetrics();
        g2d.drawString(price, 10, height - 10);
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }
}
