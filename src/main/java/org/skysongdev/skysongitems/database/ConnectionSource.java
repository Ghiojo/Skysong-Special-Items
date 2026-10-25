package org.skysongdev.skysongitems.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hands out database connections. The caller closes each connection it gets,
 * which returns it to the pool when the source is pooled.
 */
@FunctionalInterface
public interface ConnectionSource {
    Connection getConnection() throws SQLException;
}
