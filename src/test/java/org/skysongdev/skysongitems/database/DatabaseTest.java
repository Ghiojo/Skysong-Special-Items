package org.skysongdev.skysongitems.database;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {
    private static final DatabaseSettings SETTINGS =
            new DatabaseSettings("db.example.org", 3307, "skysong", "secret", "books");
    // Port 1 on this machine refuses connections, so opening the pool fails straight away.
    private static final DatabaseSettings UNREACHABLE =
            new DatabaseSettings("127.0.0.1", 1, "skysong", "secret", "books");

    @Test
    void poolConnectsWithTheConfiguredUrlAndCredentials() {
        HikariConfig config = Database.poolConfig(SETTINGS);

        assertAll(
                () -> assertEquals("jdbc:mysql://db.example.org:3307/books", config.getJdbcUrl()),
                () -> assertEquals("skysong", config.getUsername()),
                () -> assertEquals("secret", config.getPassword()));
    }

    @Test
    void poolKeepsIdleConnectionsAlive() {
        assertTrue(Database.poolConfig(SETTINGS).getKeepaliveTime() > 0);
    }

    @Test
    void unreachableDatabaseFailsWithAnSqlExceptionEveryTime() {
        try (Database database = new Database(UNREACHABLE)) {
            assertThrows(SQLException.class, database::getConnection);
            // The failed attempt leaves no broken pool behind, so the next request tries again.
            assertThrows(SQLException.class, database::getConnection);
        }
    }

    @Test
    void closingADatabaseThatNeverConnectedDoesNotThrow() {
        assertDoesNotThrow(() -> new Database(SETTINGS).close());
    }
}
