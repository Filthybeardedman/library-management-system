import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Library {
    private List<Book> books = new ArrayList<Book>();

    // 図書の追加
    public boolean addBook(Book book) {
        if (book == null || findBookById(book.getId()) != null) {
            return false;
        }
        books.add(book);
        return true;
    }

    public boolean removeBook(int id) {
        Book book = findBookById(id);
        if (book == null || book.isBorrowed()) {
            return false;
        }
        return books.remove(book);
    }

    //図書の検索
    public List<Book> getAllBooks() {
        return new ArrayList<Book>(books);
    }

    public Book findBookById(int id) {
        for (Book book : books) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    public List<Book> searchByTitle(String title) {
        List<Book> matches = new ArrayList<Book>();
        if (title == null) {
            return matches;
        }

        String keyword = title.toLowerCase(Locale.ROOT);
        for (Book book : books) {
            if (book.getTitle().toLowerCase(Locale.ROOT).contains(keyword)) {
                matches.add(book);
            }
        }
        return matches;
    }

    public List<Book> searchByAuthor(String author) {
        List<Book> matches = new ArrayList<Book>();
        if (author == null) {
            return matches;
        }

        String keyword = author.toLowerCase(Locale.ROOT);
        for (Book book : books) {
            if (book.getAuthor().toLowerCase(Locale.ROOT).contains(keyword)) {
                matches.add(book);
            }
        }
        return matches;
    }

    // 図書の貸出
    public boolean borrowBook(int id, User user) {
        Book book = findBookById(id);
        if (book == null || user == null || book.isBorrowed()) {
            return false;
        }
        book.borrow();
        user.borrow(book);
        return true;
    }

    // 図書の返却
    public boolean returnBook(int id, User user) {
        Book book = findBookById(id);
        if (book == null || user == null || !book.isBorrowed()
                || !user.getBorrowedBooks().contains(book)) {
            return false;
        }
        book.returnBook();
        user.returnBook(book);
        return true;
    }
}
