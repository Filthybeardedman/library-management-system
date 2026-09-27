import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class Main {
    public static void main(String[] args) {
        // Swingの画面はイベント処理用のスレッドで起動
        SwingUtilities.invokeLater(() -> new LibraryWindow().setVisible(true));
    }

    private static class LibraryWindow extends JFrame {
        // 本とユーザーの情報をメモリ上で管理
        private final Library library = new Library();
        private final List<User> users = new ArrayList<User>();
        private int nextBookId = 1;
        private int nextUserId = 1;

        // 登録・検索フォームで使う入力欄
        private final JTextField titleField = new JTextField(16);
        private final JTextField authorField = new JTextField(12);
        private final JTextField userNameField = new JTextField(12);
        private final JComboBox<String> searchType = new JComboBox<String>(
            new String[] {"ID", "タイトル", "作者"});
        private final JTextField searchField = new JTextField(16);

        // 貸出対象ユーザーと、蔵書一覧の表示に使う部品
        private final JComboBox<User> userSelector = new JComboBox<User>();
        private final DefaultTableModel tableModel = new DefaultTableModel(
                new Object[] {"ID", "タイトル", "作者", "状態"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable bookTable = new JTable(tableModel);

        // ウィンドウの基本設定と、各パネル・一覧表を配置
        LibraryWindow() {
            setTitle("図書管理システム");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(850, 560);
            setLocationRelativeTo(null);

            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.setBorder(new EmptyBorder(12, 12, 12, 12));
            content.add(createTopPanel(), BorderLayout.NORTH);

            bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            bookTable.setFillsViewportHeight(true);
            content.add(new JScrollPane(bookTable), BorderLayout.CENTER);
            content.add(createActionPanel(), BorderLayout.SOUTH);
            setContentPane(content);

            userSelector.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof User) {
                        // 選択肢にはユーザーIDと名前を表示する。
                        User user = (User) value;
                        setText(user.getID() + " - " + user.getName());
                    }
                    return this;
                }
            });
        }

        // 本の登録、ユーザー登録、検索の操作欄
        private JPanel createTopPanel() {
            JPanel topPanel = new JPanel(new GridLayout(3, 1, 6, 6));

            JPanel bookRegistration = new JPanel();
            bookRegistration.add(new JLabel("本の登録"));
            bookRegistration.add(new JLabel("タイトル"));
            bookRegistration.add(titleField);
            bookRegistration.add(new JLabel("作者"));
            bookRegistration.add(authorField);
            JButton addBookButton = new JButton("本を登録");
            addBookButton.addActionListener(event -> addBook());
            bookRegistration.add(addBookButton);
            topPanel.add(bookRegistration);

            JPanel userRegistration = new JPanel();
            userRegistration.add(new JLabel("ユーザー登録"));
            userRegistration.add(new JLabel("名前"));
            userRegistration.add(userNameField);
            JButton addUserButton = new JButton("ユーザーを登録");
            addUserButton.addActionListener(event -> addUser());
            userRegistration.add(addUserButton);
            topPanel.add(userRegistration);

            JPanel searchPanel = new JPanel();
            searchPanel.add(new JLabel("検索"));
            searchPanel.add(searchType);
            searchPanel.add(searchField);
            JButton searchButton = new JButton("検索");
            searchButton.addActionListener(event -> searchBooks());
            searchPanel.add(searchButton);
            JButton showAllButton = new JButton("一覧");
            showAllButton.addActionListener(event -> refreshTable(library.getAllBooks()));
            searchPanel.add(showAllButton);
            topPanel.add(searchPanel);

            return topPanel;
        }

        // 選択した本の貸出・返却・削除を行う操作欄
        private JPanel createActionPanel() {
            JPanel actions = new JPanel();
            actions.add(new JLabel("利用者"));
            userSelector.setPrototypeDisplayValue(new User(0, "ユーザー名"));
            actions.add(userSelector);

            JButton borrowButton = new JButton("選択した本を貸出");
            borrowButton.addActionListener(event -> changeLoanStatus(true));
            actions.add(borrowButton);

            JButton returnButton = new JButton("選択した本を返却");
            returnButton.addActionListener(event -> changeLoanStatus(false));
            actions.add(returnButton);

            JButton removeButton = new JButton("選択した本を削除");
            removeButton.addActionListener(event -> removeBook());
            actions.add(removeButton);
            return actions;
        }

        // 入力された本を登録し、一覧を更新
        private void addBook() {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (title.isEmpty() || author.isEmpty()) {
                showMessage("タイトルと作者を入力してください。");
                return;
            }

            Book book = new Book(nextBookId, title, author);
            if (library.addBook(book)) {
                nextBookId++;
                titleField.setText("");
                authorField.setText("");
                refreshTable(library.getAllBooks());
            }
        }

        // ユーザーを登録し、貸出対象の選択肢に追加
        private void addUser() {
            String name = userNameField.getText().trim();
            if (name.isEmpty()) {
                showMessage("ユーザー名を入力してください。");
                return;
            }

            User user = new User(nextUserId, name);
            users.add(user);
            nextUserId++;
            userSelector.addItem(user);
            userSelector.setSelectedItem(user);
            userNameField.setText("");
        }

        // 選択中の条件で本を検索、検索語が空なら全件表示
        private void searchBooks() {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                refreshTable(library.getAllBooks());
                return;
            }

            String selectedType = (String) searchType.getSelectedItem();
            if ("ID".equals(selectedType)) {
                try {
                    Book book = library.findBookById(Integer.parseInt(keyword));
                    refreshTable(book == null
                            ? Collections.<Book>emptyList()
                            : Collections.singletonList(book));
                } catch (NumberFormatException exception) {
                    showMessage("IDは数字で入力してください。");
                }
            } else if ("タイトル".equals(selectedType)) {
                refreshTable(library.searchByTitle(keyword));
            } else {
                refreshTable(library.searchByAuthor(keyword));
            }
        }

        // 選択した本の状態と、ユーザーの借用リストを同時に更新
        private void changeLoanStatus(boolean borrowing) {
            Book book = getSelectedBook();
            User user = (User) userSelector.getSelectedItem();
            if (book == null) {
                showMessage("一覧から本を選択してください。");
                return;
            }
            if (user == null) {
                showMessage("先にユーザーを登録してください。");
                return;
            }

            boolean succeeded = borrowing
                    ? library.borrowBook(book.getId(), user)
                    : library.returnBook(book.getId(), user);
            if (!succeeded) {
                showMessage(borrowing ? "この本は貸出できません。" : "このユーザーはこの本を借りていません。");
            }
            refreshTable(library.getAllBooks());
        }

        // 選択中の本を削除する。貸出中の本はLibrary側で削除を拒否
        private void removeBook() {
            Book book = getSelectedBook();
            if (book == null) {
                showMessage("一覧から本を選択してください。");
                return;
            }
            if (!library.removeBook(book.getId())) {
                showMessage("本が見つからないか、貸出中のため削除できません。");
                return;
            }
            refreshTable(library.getAllBooks());
        }

        // 表で選択された行のIDから、Library内のBookを取得する
        private Book getSelectedBook() {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow < 0) {
                return null;
            }
            int id = (Integer) tableModel.getValueAt(selectedRow, 0);
            return library.findBookById(id);
        }

        // 指定された本の一覧で表の内容を置き換える
        private void refreshTable(List<Book> books) {
            tableModel.setRowCount(0);
            for (Book book : books) {
                tableModel.addRow(new Object[] {
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.isBorrowed() ? "貸出中" : "貸出可"
                });
            }
        }

        // 入力不足や操作失敗をダイアログで知らせる
        private void showMessage(String message) {
            JOptionPane.showMessageDialog(this, message);
        }
    }
}