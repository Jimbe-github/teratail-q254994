package teratail.q254994;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class Q254994 extends JFrame {
  private static String DRIVER = "com.mysql.cj.jdbc.Driver";
  private static String URL = "jdbc:mysql://localhost:3306/q254994?characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B9:00&rewriteBatchedStatements=true";
  private static String USER = "root";
  private static String PASS = "root";
  private static String TABLE = "tbl_book";
  private enum tbl_book {
    No, title, author, publisher, ISBN, releaseday, status;
  };

  private static final String UPDATE =
      "update " + TABLE +
      " set " + tbl_book.status + "=?" +
      " where no=?";

  private BookTableModel tableModel;

  Q254994() throws ClassNotFoundException {
    super("Q254994");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(640,400);
    setLocationRelativeTo(null);

    Class.forName(DRIVER);

    tableModel = new BookTableModel();
    tableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        if(e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6) { //status
          int no = (Integer)tableModel.getValueAt(e.getFirstRow(), 0);
          BookStatus status = (BookStatus)tableModel.getValueAt(e.getFirstRow(), 6);

          try(Connection con = getConnection();
              PreparedStatement pstmt = con.prepareStatement(UPDATE);) {
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, no);
            int num = pstmt.executeUpdate();
            System.out.println("UPDATE結果：" + num);
          } catch(SQLException ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    JTable table = new JTable();
    table.setModel(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    TableColumnModel tcm = table.getColumnModel();

    //No
    tcm.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        value = String.format("%03d", (Integer)value);
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    });

    tcm.getColumn(1).setPreferredWidth(150);
    tcm.getColumn(2).setPreferredWidth(150);

    //発売年
    tcm.getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        value = new SimpleDateFormat("yyyy").format((java.util.Date)value);
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    });

    //Status
    JComboBox<BookStatus> cb = new JComboBox<BookStatus>(BookStatus.values());
    cb.setEditable(true);
    cb.setBorder(BorderFactory.createEmptyBorder());
    tcm.getColumn(6).setCellEditor(new DefaultCellEditor(cb));

    final RegistPanel registPanel = new RegistPanel(new RegistPanel.Listener() {
      @Override
      public void registPerformed(BookDataProducer producer) {
        try(Connection con = getConnection();) {
          registTable(con, producer);
          loadTable(con);
        } catch(Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(Q254994.this, "例外発生：" + e.toString());
        }
      }
    });

    add(new JScrollPane(table), BorderLayout.CENTER);
    add(registPanel, BorderLayout.SOUTH);

    try(Connection con = getConnection();) {
      loadTable(con);
    } catch(Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(Q254994.this, "例外発生：" + e.toString());
    }
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL,USER,PASS);
  }

  private static final String SELECT =
      "select " + tbl_book.No + "," + tbl_book.title + "," + tbl_book.author + "," + tbl_book.publisher + "," +
          tbl_book.ISBN + "," + tbl_book.releaseday + "," + tbl_book.status +
      " from " + TABLE +
      " order by No";

  private void loadTable(Connection con) throws SQLException {
    try(Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT);) {

      tableModel.startUpdateMode();
      while(rs.next()) {
        tableModel.put(rs.getInt(tbl_book.No.toString()),
            rs.getString(tbl_book.title.toString()),
            rs.getString(tbl_book.author.toString()),
            rs.getString(tbl_book.publisher.toString()),
            rs.getString(tbl_book.ISBN.toString()),
            new java.util.Date(rs.getDate(tbl_book.releaseday.toString()).getTime()),
            BookStatus.from(rs.getString(tbl_book.status.toString())));
      }
      tableModel.finishUpdateMode();
    }
  }

  private static final String INSERT =
      "insert into " + TABLE +
      " (" + tbl_book.title + "," + tbl_book.author + "," + tbl_book.publisher + "," +
            tbl_book.ISBN + "," + tbl_book.releaseday + "," + tbl_book.status +
      ") values (?, ?, ?, ?, ?, ?)";

  private void registTable(Connection con, BookDataProducer producer) throws SQLException {

    //** キーと思われる No 列は AUTO-INCLIMENT にすれば加算・設定は不要 **

    try(PreparedStatement pstmt = con.prepareStatement(INSERT);) {
      pstmt.setString(1, producer.getTitle());
      pstmt.setString(2, producer.getAuthor());
      pstmt.setString(3, producer.getPublisher());
      pstmt.setString(4, producer.getISBN());
      pstmt.setDate(5, new java.sql.Date(producer.getReleaseDay().getTime()));
      pstmt.setString(6, BookStatus.NORMAL.toString());
      int num = pstmt.executeUpdate();
      System.out.println("INSERT結果：" + num);
      producer.clear();
    }
  }

  public static void main(String[] args) throws ClassNotFoundException {
    new Q254994().setVisible(true);
  }
}

interface BookDataProducer {
  String getTitle();
  String getAuthor();
  String getPublisher();
  String getISBN();
  java.util.Date getReleaseDay();

  void clear();
}

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
    layoutLabelAndField("Title", titleField, gbc, 0, 0, GridBagConstraints.REMAINDER);

    authorField = new JTextField();
    authorField.getDocument().addDocumentListener(validateDocumentListener);
    layoutLabelAndField("Author", authorField, gbc, 0, 1, GridBagConstraints.REMAINDER);

    publisherField = new JTextField();
    publisherField.getDocument().addDocumentListener(validateDocumentListener);
    layoutLabelAndField("Publisher", publisherField, gbc, 0, 2, GridBagConstraints.REMAINDER);

    isbnField = new JTextField(13);
    Document doc = isbnField.getDocument();
    doc.addDocumentListener(validateDocumentListener);
    if(doc instanceof AbstractDocument) {
      ((AbstractDocument)doc).setDocumentFilter(new DocumentSizeFilter(13));
    }
    layoutLabelAndField("ISBN", isbnField, gbc, 0, 3, 1);

    releasedayComboBox = new JComboBox<>(new String[]{ "-", "2020", "2019", "2018" });
    releasedayComboBox.addActionListener(validateActionListener);
    layoutLabelAndField("発売年", releasedayComboBox, gbc, GridBagConstraints.RELATIVE, 3, GridBagConstraints.REMAINDER);

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

enum BookStatus {
  NORMAL("-"), RENTED("貸出中"), RETURN("返却");
  private String text;
  BookStatus(String text) {
    this.text = text;
  }
  public String toString() { return text; }
  static BookStatus from(String text) {
    for(BookStatus e : values()) if(e.text.equals(text)) return e;
    return valueOf(text);
  }
}

class BookTableModel extends AbstractTableModel {
  private class Data {
    int no;
    String title;
    String author;
    String publisher;
    String isbn;
    java.util.Date releaseDay;
    BookStatus status;

    int flag;
  }
  private java.util.List<Data> dataList = new ArrayList<>();

  void startUpdateMode() {
    for(Data d : dataList) d.flag = 1;
  }

  void finishUpdateMode() {
    for(int row=dataList.size()-1; row>=0; row--) {
      if(dataList.get(row).flag == 1) {
        dataList.remove(row);
        fireTableRowsDeleted(row, row);
      }
    }
  }

  void put(int no, String title, String author, String publisher, String isbn, java.util.Date releaseDay, BookStatus status) {
    for(int row=0; row<dataList.size(); row++) {
      Data d = dataList.get(row);
      if(d.no == no) {
        d.flag = 0;
        if(d.status != status) {
          d.status = status;
          fireTableCellUpdated(row, 6);
        }
        return;
      }
    }

    Data data = new Data();
    data.no = no;
    data.title = title;
    data.author = author;
    data.publisher = publisher;
    data.isbn = isbn;
    data.releaseDay = releaseDay;
    data.status = status;
    data.flag = 0;
    dataList.add(data);
    fireTableRowsInserted(dataList.size()-1, dataList.size()-1);
  }

  @Override
  public int getRowCount() {
    return dataList.size();
  }

  @Override
  public int getColumnCount() {
    return 7;
  }

  @Override
  public String getColumnName(int columnIndex) {
    switch(columnIndex) { //getValueAt と合わせての switch
    case 0:
      return "No";
    case 1:
      return "Title";
    case 2:
      return "Author";
    case 3:
      return "Publisher";
    case 4:
      return "ISBN";
    case 5:
      return "発売年";
    case 6:
      return "Status";
    }
    throw new IllegalArgumentException();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch(columnIndex) {
    case 0:
      return dataList.get(rowIndex).no;
    case 1:
      return dataList.get(rowIndex).title;
    case 2:
      return dataList.get(rowIndex).author;
    case 3:
      return dataList.get(rowIndex).publisher;
    case 4:
      return dataList.get(rowIndex).isbn;
    case 5:
      return dataList.get(rowIndex).releaseDay;
    case 6:
      return dataList.get(rowIndex).status;
    }
    throw new IllegalArgumentException();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 6; //status
  }

  @Override
  public void setValueAt(Object val, int rowIndex, int columnIndex) {
    if(columnIndex == 6) { //status
      dataList.get(rowIndex).status = (BookStatus)val;
      fireTableCellUpdated(rowIndex, columnIndex);
    } else {
      throw new IllegalArgumentException();
    }
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