public class Book {
    private int id;
    private String title;
    private String author;
    private boolean borrowed;

    public Book(int id, String title, String author) {
        //一意なIDを自動生成
        this.id = id;
        this.title = title;
        this.author = author;
        borrowed = false;
    }

    public int getId() {return id;}
    public String getTitle() {return title;}
    public String getAuthor() {return author;}
    public boolean isBorrowed() {return borrowed;}

    public void borrow() {borrowed = true;}
    public void returnBook() {borrowed = false;}
}