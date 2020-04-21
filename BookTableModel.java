package teratail.q254994;

import java.util.*;

import javax.swing.table.AbstractTableModel;

class BookTableModel extends AbstractTableModel {
  enum Columns {
    No()
      { Object getValueAt(Data data){return data.no;} },
    Title()
      { Object getValueAt(Data data){return data.title;} },
    Author()
      { Object getValueAt(Data data){return data.author;} },
    Publisher()
      { Object getValueAt(Data data){return data.publisher;} },
    ISBN()
      { Object getValueAt(Data data){return data.isbn;} },
    ReleaseDay("発売日")
      { Object getValueAt(Data data){return data.releaseDay;} },
    Status()
      { Object getValueAt(Data data){return data.status;} };

    String columnName;
    Columns() { this(null); }
    Columns(String columnName) { this.columnName = columnName; }
    int getIndex() { return ordinal(); }
    String getColumnName() { return columnName == null ? this.name() : columnName; }
    abstract Object getValueAt(Data data);
    static Columns get(int columnIndex) { return values()[columnIndex]; }
  }

  private class Data {
    int no;
    String title;
    String author;
    String publisher;
    String isbn;
    Date releaseDay;
    BookStatus status;
    int flag;
  }
  private List<Data> dataList = new ArrayList<>();

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

  void put(int no, String title, String author, String publisher, String isbn, Date releaseDay, BookStatus status) {
    for(int row=0; row<dataList.size(); row++) {
      Data d = dataList.get(row);
      if(d.no == no) {
        d.flag = 0;
        if(d.status != status) {
          d.status = status;
          fireTableCellUpdated(row, Columns.Status.getIndex());
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
    return Columns.values().length;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return Columns.get(columnIndex).getColumnName();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return Columns.get(columnIndex).getValueAt(dataList.get(rowIndex));
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == Columns.Status.getIndex();
  }

  @Override
  public void setValueAt(Object val, int rowIndex, int columnIndex) {
    if(columnIndex == Columns.Status.getIndex()) {
      dataList.get(rowIndex).status = (BookStatus)val;
      fireTableCellUpdated(rowIndex, columnIndex);
    } else {
      throw new IllegalArgumentException();
    }
  }
}