package teratail.q254994;

interface BookDataProducer {
  String getTitle();
  String getAuthor();
  String getPublisher();
  String getISBN();
  java.util.Date getReleaseDay();

  void clear();
}