package teratail.q254994;

import java.sql.*;

class BookDBOperator {
  private static String DRIVER = "com.mysql.cj.jdbc.Driver";
  private static String URL = "jdbc:mysql://localhost:3306/q254994?characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B9:00&rewriteBatchedStatements=true";
  private static String USER = "root";
  private static String PASS = "root";
  private static String TABLE = "tbl_book";
  private enum tbl_book {
    No, title, author, publisher, ISBN, releaseday, status;
  };

  BookDBOperator() throws ClassNotFoundException {
    Class.forName(DRIVER);
  }

  private static final String SELECT =
      "select " + tbl_book.No + "," + tbl_book.title + "," + tbl_book.author + "," + tbl_book.publisher + "," +
          tbl_book.ISBN + "," + tbl_book.releaseday + "," + tbl_book.status +
      " from " + TABLE +
      " order by No";

  void loadAllBooks(BookTableModel tableModel) throws SQLException {
    try(Connection con = getConnection();
        Statement stmt = con.createStatement();
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

  int insertBook(BookDataProducer producer) throws SQLException {

    //** キーと思われる No 列は AUTO-INCLIMENT にすれば加算・設定は不要 **

    try(Connection con = getConnection();
        PreparedStatement pstmt = con.prepareStatement(INSERT);) {
      pstmt.setString(1, producer.getTitle());
      pstmt.setString(2, producer.getAuthor());
      pstmt.setString(3, producer.getPublisher());
      pstmt.setString(4, producer.getISBN());
      pstmt.setDate(5, new java.sql.Date(producer.getReleaseDay().getTime()));
      pstmt.setString(6, BookStatus.NORMAL.toString());
      return pstmt.executeUpdate();
    }
  }

  private static final String UPDATE =
      "update " + TABLE +
      " set " + tbl_book.status + "=?" +
      " where no=?";

  int updateStatus(int no, BookStatus status) throws SQLException {
    try(Connection con = getConnection();
        PreparedStatement pstmt = con.prepareStatement(UPDATE);) {
      pstmt.setString(1, status.toString());
      pstmt.setInt(2, no);
      return pstmt.executeUpdate();
    }
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL,USER,PASS);
  }
}