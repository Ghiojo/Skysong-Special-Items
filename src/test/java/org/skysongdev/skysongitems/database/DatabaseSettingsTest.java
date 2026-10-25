package org.skysongdev.skysongitems.database;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DatabaseSettingsTest {

    @Test
    void readsEveryValueWithAQuotedPortAsInTheBundledConfig() {
        DatabaseSettings settings = DatabaseSettings.fromConfig(config("3307"));

        assertEquals(new DatabaseSettings("db.example.org", 3307, "skysong", "secret", "books"), settings);
    }

    @Test
    void readsAPortWrittenWithoutQuotes() {
        assertEquals(3308, DatabaseSettings.fromConfig(config(3308)).port());
    }

    @Test
    void usesTheMySqlDefaultPortWhenNoneIsSet() {
        YamlConfiguration config = config(null);

        assertEquals(DatabaseSettings.DEFAULT_PORT, DatabaseSettings.fromConfig(config).port());
    }

    @Test
    void usesTheMySqlDefaultPortWhenThePortIsNotANumber() {
        assertEquals(DatabaseSettings.DEFAULT_PORT, DatabaseSettings.fromConfig(config("three")).port());
    }

    @Test
    void jdbcUrlIncludesThePort() {
        DatabaseSettings settings = new DatabaseSettings("db.example.org", 3307, "skysong", "secret", "books");

        assertEquals("jdbc:mysql://db.example.org:3307/books", settings.jdbcUrl());
    }

    @Test
    void toStringLeavesOutThePassword() {
        DatabaseSettings settings = new DatabaseSettings("db.example.org", 3307, "skysong", "secret", "books");

        assertFalse(settings.toString().contains("secret"));
    }

    /** A database section with the given port: a string as in the bundled config.yml, a number, or null for none. */
    private static YamlConfiguration config(Object port) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("database.host", "db.example.org");
        config.set("database.port", port);
        config.set("database.user", "skysong");
        config.set("database.password", "secret");
        config.set("database.database_name", "books");
        return config;
    }
}
