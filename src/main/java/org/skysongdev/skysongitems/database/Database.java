package org.skysongdev.skysongitems.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * The pool of connections to the MySQL database that stores the books.
 *
 * <p>The pool opens on the first request for a connection. When the database cannot be
 * reached, that request fails with an {@link SQLException} and the next one tries again,
 * so the plugin recovers once the database is back without a restart. Once open, the pool
 * replaces connections the server has dropped and keeps idle ones alive.
 */
public final class Database implements ConnectionSource, AutoCloseable {
    private static final String POOL_NAME = "SkysongItems-database";
    // Book queries run one at a time on the BookQueries worker thread, so a few connections are enough.
    private static final int MAXIMUM_POOL_SIZE = 4;
    private static final int MINIMUM_IDLE = 1;
    // Keep a lost database from holding up the queued book queries for Hikari's default 30 seconds.
    private static final long CONNECTION_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(5);
    // Same interval as the ping task this pool replaces, well under MySQL's default wait_timeout.
    private static final long KEEPALIVE_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final HikariConfig poolConfig;
    private final BookDescriptionRepository books = new BookDescriptionRepository(this);
    private HikariDataSource pool;

    public Database(DatabaseSettings settings) {
        this.poolConfig = poolConfig(settings);
    }

    /** The book descriptions stored in this database. */
    public BookDescriptionRepository getBooks() {
        return books;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return openPool().getConnection();
    }

    /** Closes every pooled connection. A later request for a connection opens the pool again. */
    @Override
    public void close() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    private HikariDataSource openPool() throws SQLException {
        if (pool == null) {
            try {
                pool = new HikariDataSource(poolConfig);
            } catch (PoolInitializationException e) {
                throw new SQLException("Could not connect to the database", e);
            }
        }
        return pool;
    }

    static HikariConfig poolConfig(DatabaseSettings settings) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(POOL_NAME);
        config.setJdbcUrl(settings.jdbcUrl());
        config.setUsername(settings.user());
        config.setPassword(settings.password());
        config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        config.setMinimumIdle(MINIMUM_IDLE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MILLIS);
        config.setKeepaliveTime(KEEPALIVE_MILLIS);
        return config;
    }
}
