package org.skysongdev.skysongitems.database;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Runs the book queries against an in-memory H2 database in MySQL mode, which accepts
 * the same table definition and SQL the plugin sends to MySQL.
 */
class BookDescriptionRepositoryTest {
    private BookDescriptionRepository repository;

    @BeforeEach
    void createEmptyTable() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        // A fresh database per test, kept open between connections until the JVM exits.
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        repository = new BookDescriptionRepository(dataSource::getConnection);
        repository.createTable();
    }

    @Test
    void createTableCanRunAgainOnAnExistingTable() {
        assertDoesNotThrow(repository::createTable);
    }

    @Test
    void createdBookIsFoundByTheLatestId() throws SQLException {
        repository.create(new BookDescriptions(0, "Tome", "First page[PageEnd]Second page"));

        int id = repository.findLatestId();
        BookDescriptions found = repository.findById(id);

        assertEquals(1, id);
        assertEquals("Tome", found.getName());
        assertEquals("First page[PageEnd]Second page", found.getBookcontent());
    }

    @Test
    void latestIdIsZeroWhenThereAreNoBooks() throws SQLException {
        assertEquals(0, repository.findLatestId());
    }

    @Test
    void latestIdIsTheMostRecentBook() throws SQLException {
        repository.create(new BookDescriptions(0, "First", ""));
        repository.create(new BookDescriptions(0, "Second", ""));

        assertEquals(2, repository.findLatestId());
    }

    @Test
    void findingAMissingBookReturnsNull() throws SQLException {
        assertNull(repository.findById(99));
    }

    @Test
    void textWithQuotesIsStoredAsWritten() throws SQLException {
        String name = "Robert'); DROP TABLE book_descriptions;--";
        repository.create(new BookDescriptions(0, name, "It's \"quoted\""));

        BookDescriptions found = repository.findById(repository.findLatestId());

        assertEquals(name, found.getName());
        assertEquals("It's \"quoted\"", found.getBookcontent());
    }

    @Test
    void updateReplacesNameAndContent() throws SQLException {
        repository.create(new BookDescriptions(0, "Draft", "Old"));
        int id = repository.findLatestId();

        repository.update(new BookDescriptions(id, "Final", "New"));

        BookDescriptions found = repository.findById(id);
        assertEquals("Final", found.getName());
        assertEquals("New", found.getBookcontent());
    }

    @Test
    void deleteRemovesOnlyThatBook() throws SQLException {
        repository.create(new BookDescriptions(0, "Kept", ""));
        repository.create(new BookDescriptions(0, "Deleted", ""));

        repository.delete(2);

        assertNull(repository.findById(2));
        assertEquals("Kept", repository.findById(1).getName());
    }

    @Test
    void deletingAMissingBookDoesNotThrow() {
        assertDoesNotThrow(() -> repository.delete(99));
    }

    @Test
    void listAllReturnsNamesInIdOrderWithoutContent() throws SQLException {
        repository.create(new BookDescriptions(0, "First", "Content one"));
        repository.create(new BookDescriptions(0, "Second", "Content two"));

        List<BookDescriptions> books = repository.listAll();

        assertEquals(2, books.size());
        assertEquals(1, books.get(0).getId());
        assertEquals("First", books.get(0).getName());
        assertEquals("", books.get(0).getBookcontent());
        assertEquals(2, books.get(1).getId());
        assertEquals("Second", books.get(1).getName());
    }

    @Test
    void listAllIsEmptyWhenThereAreNoBooks() throws SQLException {
        assertEquals(List.of(), repository.listAll());
    }
}
