package org.skysongdev.skysongitems.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@code /si dbreload} with the bundled config.yml, whose MySQL server does not exist in
 * tests: the reload fails on the database worker and still confirms, as it always has.
 */
class DBReloadTest extends MockBukkitTest {
    private static final int MAX_TICKS = 200;
    private static final long TICK_MILLIS = 50;

    private SkysongItems plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    @Test
    void reloadSwitchesToANewRepositoryAndConfirmsOnceTheWorkerIsDone() throws InterruptedException {
        BookDescriptionRepository before = plugin.getBookRepository();

        new DBReload().onCommand(player, plugin.getCommand("skysongitems"), "si", new String[0]);

        assertNotSame(before, plugin.getBookRepository());
        assertNull(player.nextComponentMessage(), "the confirmation waits for the database worker");
        assertEquals("[SkysongItems]Database Reloaded", awaitReply());
    }

    /** Runs server ticks, in real time, until the player gets a message. */
    private String awaitReply() throws InterruptedException {
        for (int tick = 0; tick < MAX_TICKS; tick++) {
            Thread.sleep(TICK_MILLIS);
            server.getScheduler().performOneTick();
            Component message = player.nextComponentMessage();
            if (message != null) {
                return PlainTextComponentSerializer.plainText().serialize(message);
            }
        }
        return null;
    }
}
