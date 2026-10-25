package org.skysongdev.skysongitems.testsupport;

import org.bukkit.plugin.Plugin;
import org.h2.jdbcx.JdbcDataSource;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.BookQueries;

import java.sql.SQLException;
import java.util.UUID;

/** Book storage for tests, backed by an in-memory H2 database in MySQL mode. */
public final class InMemoryBooks {
    private InMemoryBooks() {
    }

    /** An empty books table in a fresh database, kept open until the JVM exits. */
    public static BookDescriptionRepository emptyRepository() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        BookDescriptionRepository repository = new BookDescriptionRepository(dataSource::getConnection);
        repository.createTable();
        return repository;
    }

    /**
     * Queries on {@code repository} that run as soon as they are submitted, on the
     * calling thread. Their results are still handed back on the next server tick.
     */
    public static BookQueries runningInline(Plugin plugin, BookDescriptionRepository repository) {
        return new BookQueries(plugin, () -> repository, new DirectExecutorService());
    }
}
