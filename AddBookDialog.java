

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class AddBookDialog extends JDialog {
public AddBookDialog(Window owner, Library library) {
super(owner, "Agregar libro", ModalityType.APPLICATION_MODAL);
setSize(520, 380);
setLocationRelativeTo(owner);
setLayout(new BorderLayout(6,6));


JPanel form = new JPanel(new GridLayout(0,2,8,8));
form.setBorder(new EmptyBorder(12,12,12,12));


JTextField titleField = new JTextField();
JTextField isbnField = new JTextField();
JTextField priceField = new JTextField();
JTextField yearField = new JTextField();
JTextField authorsField = new JTextField();
JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));


JRadioButton rbText = new JRadioButton("Libro de texto");
JRadioButton rbNovel = new JRadioButton("Novela");
ButtonGroup bg = new ButtonGroup(); bg.add(rbText); bg.add(rbNovel); rbText.setSelected(true);
JTextField levelField = new JTextField();
JTextField genreField = new JTextField();


form.add(new JLabel("Título:")); form.add(titleField);
form.add(new JLabel("ISBN:")); form.add(isbnField);
form.add(new JLabel("Precio:")); form.add(priceField);
form.add(new JLabel("Año publicación:")); form.add(yearField);
form.add(new JLabel("Autores (separados por coma):")); form.add(authorsField);
form.add(new JLabel("Stock:")); form.add(stockSpinner);
form.add(new JLabel("Tipo:"));
JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); typePanel.setOpaque(false);
typePanel.add(rbText); typePanel.add(rbNovel);
form.add(typePanel);
form.add(new JLabel("Nivel (si libro de texto):")); form.add(levelField);
form.add(new JLabel("Género (si novela):")); form.add(genreField);

}

}

