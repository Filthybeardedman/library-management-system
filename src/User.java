import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private List<Book> borrowedBooks = new ArrayList<Book>();

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getID() {return id;}
    public String getName() {return name;}
    public List<Book> getBorrowedBooks() {return borrowedBooks;}

    public void borrow(Book book) {borrowedBooks.add(book);}
    public void returnBook(Book book) {borrowedBooks.remove(book);}
}
