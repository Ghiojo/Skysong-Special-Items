package org.skysongdev.skysongitems.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the {@code book_descriptions} table. This is the only class that
 * holds SQL; every query is a prepared statement, and every connection is closed
 * (returned to the pool) before the method returns.
 */
public final class BookDescriptionRepository {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS book_descriptions("
            + "id int AUTO_INCREMENT primary key, name TINYTEXT, bookcontent MEDIUMTEXT)";
    private static final String SELECT_BY_ID = "SELECT name, bookcontent FROM book_descriptions WHERE id = ?";
    private static final String SELECT_LATEST_ID = "SELECT MAX(id) AS id FROM book_descriptions";
    private static final String SELECT_ALL = "SELECT id, name FROM book_descriptions ORDER BY id";
    private static final String INSERT = "INSERT INTO book_descriptions(name, bookcontent) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE book_descriptions SET name = ?, bookcontent = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM book_descriptions WHERE id = ?";

    private final ConnectionSource connections;

    public BookDescriptionRepository(ConnectionSource connections) {
        this.connections = connections;
    }

    /** Creates the table if it does not exist yet. */
    public void createTable() throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE)) {
            statement.execute();
        }
    }

    /** The book with this id, or null when there is none. */
    public BookDescriptions findById(int id) throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                return new BookDescriptions(id, results.getString("name"), results.getString("bookcontent"));
            }
        }
    }

    /** The highest book id, 0 when the table is empty. */
    public int findLatestId() throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_LATEST_ID);
             ResultSet results = statement.executeQuery()) {
            return results.next() ? results.getInt("id") : -1;
        }
    }

    /** Every book, ordered by id, with names only: the content is left empty. */
    public List<BookDescriptions> listAll() throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet results = statement.executeQuery()) {
            List<BookDescriptions> books = new ArrayList<>();
            while (results.next()) {
                books.add(new BookDescriptions(results.getInt("id"), results.getString("name"), ""));
            }
            return books;
        }
    }

    /** Stores a new book. The database assigns its id. */
    public void create(BookDescriptions book) throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, book.getName());
            statement.setString(2, book.getBookcontent());
            statement.executeUpdate();
        }
    }

    /** Replaces the name and content of the book with the same id. */
    public void update(BookDescriptions book) throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, book.getName());
            statement.setString(2, book.getBookcontent());
            statement.setInt(3, book.getId());
            statement.executeUpdate();
        }
    }

    /** Deletes the book with this id. Deleting an id that does not exist does nothing. */
    public void delete(int id) throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
}
