package org.skysongdev.skysongitems.database;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Where the book database lives and how to log in, read from the {@code database}
 * section of config.yml.
 *
 * @param host         the MySQL host name, without a port
 * @param port         the MySQL port
 * @param user         the user to log in as
 * @param password     that user's password
 * @param databaseName the schema that holds the {@code book_descriptions} table
 */
public record DatabaseSettings(String host, int port, String user, String password, String databaseName) {
    /** The MySQL port used when config.yml does not set one, or sets one that is not a number. */
    public static final int DEFAULT_PORT = 3306;

    /** Reads the settings from the {@code database} section of the given configuration. */
    public static DatabaseSettings fromConfig(ConfigurationSection config) {
        return new DatabaseSettings(
                config.getString("database.host"),
                readPort(config),
                config.getString("database.user"),
                config.getString("database.password"),
                config.getString("database.database_name"));
    }

    /**
     * The bundled config.yml writes the port as a quoted string, which {@code getInt} would
     * ignore, so the port is read as text and parsed.
     */
    private static int readPort(ConfigurationSection config) {
        String port = config.getString("database.port");
        if (port == null) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(port.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    /** The JDBC URL of the database, for example {@code jdbc:mysql://localhost:3306/book_descriptions}. */
    public String jdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
    }

    /** Leaves the password out, so the settings can be logged safely. */
    @Override
    public String toString() {
        return "DatabaseSettings[url=" + jdbcUrl() + ", user=" + user + "]";
    }
}
