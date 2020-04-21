package teratail.q254994;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

class RegistPanel extends JPanel implements BookDataProducer {
  interface Listener {
    void registPerformed(BookDataProducer producer);
  }
  private JTextField titleField;
  private JTextField authorField;
  private JTextField publisherField;
  private JTextField isbnField;
  private JComboBox<String> releasedayComboBox;
  private JButton registButton;

  private DocumentListener validateDocumentListener = new DocumentListener() {
    @Override
    public void removeUpdate(DocumentEvent e) { validateFields(); }
    @Override
    public void insertUpdate(DocumentEvent e) { validateFields(); }
    @Override
    public void changedUpdate(DocumentEvent e) { validateFields(); }
  };
  private ActionListener validateActionListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) { validateFields(); }
  };

  RegistPanel(Listener listener) {
    super(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    GridBagConstraints gbc = new GridBagConstraints();

    titleField = new JTextField();
    titleField.getDocument().addDocumentListener(validateDocumentListener);
    layoutLabelAndField(BookTableModel.Columns.Title.getColumnName(), titleField, gbc, 0, 0, GridBagConstraints.REMAINDER);

    authorField = new JTextField();
    authorField.getDocument().addDocumentListener(validateDocumentListener);
    layoutLabelAndField(BookTableModel.Columns.Author.getColumnName(), authorField, gbc, 0, 1, GridBagConstraints.REMAINDER);

    publisherField = new JTextField();
    publisherField.getDocument().addDocumentListener(validateDocumentListener);
    layoutLabelAndField(BookTableModel.Columns.Publisher.getColumnName(), publisherField, gbc, 0, 2, GridBagConstraints.REMAINDER);

    isbnField = new JTextField(13);
    Document doc = isbnField.getDocument();
    doc.addDocumentListener(validateDocumentListener);
    if(doc instanceof AbstractDocument) {
      ((AbstractDocument)doc).setDocumentFilter(new DocumentSizeFilter(13));
    }
    layoutLabelAndField(BookTableModel.Columns.ISBN.getColumnName(), isbnField, gbc, 0, 3, 1);

    releasedayComboBox = new JComboBox<>(new String[]{ "-", "2020", "2019", "2018" }); //このデータはテキトウ
    releasedayComboBox.addActionListener(validateActionListener);
    layoutLabelAndField(BookTableModel.Columns.ReleaseDay.getColumnName(), releasedayComboBox, gbc, GridBagConstraints.RELATIVE, 3, GridBagConstraints.REMAINDER);

    registButton = new JButton("登録");
    registButton.setEnabled(false);
    if(listener != null) registButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        listener.registPerformed(RegistPanel.this);
      }
    });
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(0,0,0,0);
    add(registButton, gbc);
  }

  private void layoutLabelAndField(String labeltext, JComponent comp,
                                      GridBagConstraints gbc, int gridx, int gridy, int compGridwidth) {
    gbc.gridx = gridx;
    gbc.gridy = gridy;
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(0,10,0,5); //top,left,bottom,right
    add(new JLabel(labeltext), gbc);

    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridwidth = compGridwidth;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0,0,0,0);
    add(comp, gbc);
  }

  private void validateFields() {
    registButton.setEnabled(!(titleField.getText().isEmpty() ||
        authorField.getText().isEmpty() ||
        publisherField.getText().isEmpty() ||
        isbnField.getText().length() != 13 ||
        releasedayComboBox.getSelectedItem().toString().equals("-")));
  }

  public String getTitle() { return titleField.getText(); }
  public String getAuthor() { return authorField.getText(); }
  public String getPublisher() { return publisherField.getText(); }
  public String getISBN() { return isbnField.getText(); }
  public java.util.Date getReleaseDay() {
    try {
      return new SimpleDateFormat("yyyy-M-d").parse((String)releasedayComboBox.getSelectedItem()+"-1-1");
    } catch(ParseException e) {
      return null;
    }
  }
  public void clear() {
    titleField.setText("");
    authorField.setText("");
    publisherField.setText("");
    isbnField.setText("");
    releasedayComboBox.setSelectedIndex(0);
  }
}

class DocumentSizeFilter extends DocumentFilter {
  private int maxCharacters;
  DocumentSizeFilter(int maxChars) {
    maxCharacters = maxChars;
  }
  public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
    if ((fb.getDocument().getLength() + str.length()) <= maxCharacters) {
      super.insertString(fb, offs, str, a);
    }
  }
  public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {
    if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters) {
      super.replace(fb, offs, length, str, a);
    }
  }
}