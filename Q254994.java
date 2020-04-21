package teratail.q254994;

import java.awt.*;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class Q254994 extends JFrame {

  Q254994() throws ClassNotFoundException {
    super("Q254994");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(640,400);
    setLocationRelativeTo(null);

    BookDBOperator dbOperator = new BookDBOperator();

    BookTableModel tableModel = new BookTableModel();
    tableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        if(e.getType() == TableModelEvent.UPDATE && e.getColumn() == BookTableModel.Columns.Status.getIndex()) {
          int no = (Integer)tableModel.getValueAt(e.getFirstRow(), 0);
          BookStatus status = (BookStatus)tableModel.getValueAt(e.getFirstRow(), e.getColumn());
          try {
            int num = dbOperator.updateStatus(no, status);
            System.out.println("UPDATE結果：" + num);
          } catch(Exception ex) {
            catchException(ex);
          }
        }
      }
    });

    JTable table = new JTable();
    table.setModel(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    settingColumns(table.getColumnModel());

    RegistPanel registPanel = new RegistPanel(new RegistPanel.Listener() {
      @Override
      public void registPerformed(BookDataProducer producer) {
        try {
          int num = dbOperator.insertBook(producer);
          System.out.println("INSERT結果：" + num);
          producer.clear();

          dbOperator.loadAllBooks(tableModel);
        } catch(Exception e) {
          catchException(e);
        }
      }
    });

    add(new JScrollPane(table), BorderLayout.CENTER);
    add(registPanel, BorderLayout.SOUTH);

    try {
      dbOperator.loadAllBooks(tableModel);
    } catch(Exception e) {
      catchException(e);
    }
  }

  private void settingColumns(TableColumnModel tcm) {
    //No
    tcm.getColumn(BookTableModel.Columns.No.getIndex()).setCellRenderer(new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        value = String.format("%03d", (Integer)value);
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    });

    tcm.getColumn(BookTableModel.Columns.Title.getIndex()).setPreferredWidth(150);
    tcm.getColumn(BookTableModel.Columns.Author.getIndex()).setPreferredWidth(150);

    //発売年
    tcm.getColumn(BookTableModel.Columns.ReleaseDay.getIndex()).setCellRenderer(new DefaultTableCellRenderer() {
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
    tcm.getColumn(BookTableModel.Columns.Status.getIndex()).setCellEditor(new DefaultCellEditor(cb));
  }

  void catchException(Exception e) {
    e.printStackTrace();
    JOptionPane.showMessageDialog(this, "例外発生：" + e.toString());
  }

  public static void main(String[] args) throws ClassNotFoundException {
    new Q254994().setVisible(true);
  }
}
