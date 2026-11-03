package org.skysongdev.skysongitems.commands.misc;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests {@code /si loaditem}. */
class LoadItemTest extends MockBukkitTest {
    private final LoadItem loadItem = new LoadItem();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        // The messages are built with the plugin's MiniMessage instance.
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
        player.addAttachment(SkysongItems.getPlugin(), "skysongitems.load", true);
    }

    @Test
    void withoutANameShowsTheUsage() {
        boolean handled = loadItem.onCommand(player, new StubCommand(), "si", new String[0]);

        assertTrue(handled);
        assertEquals("[SkysongItems] Usage: /loaditem <item_name>", nextPlainMessage());
        assertTrue(player.getInventory().isEmpty(), "no item is given without a name");
    }

    @Test
    void anUnknownNameIsReported() {
        loadItem.onCommand(player, new StubCommand(), "si", new String[]{"no_such_item"});

        assertEquals("[SkysongItems] Item not found!", nextPlainMessage());
    }

    private String nextPlainMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    /** The command never looks at the command object, so any instance will do. */
    private static final class StubCommand extends Command {
        private StubCommand() {
            super("si");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            return true;
        }
    }
}
