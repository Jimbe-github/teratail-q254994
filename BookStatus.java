package teratail.q254994;

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