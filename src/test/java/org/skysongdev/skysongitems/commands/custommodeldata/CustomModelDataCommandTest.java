package org.skysongdev.skysongitems.commands.custommodeldata;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests what {@code /si cmd} says when no sub-command is given. */
class CustomModelDataCommandTest extends MockBukkitTest {
    @Test
    void noSubcommandShowsTheCmdUsage() {
        MockBukkit.load(SkysongItems.class);
        PlayerMock player = server.addPlayer();

        new CustomModelDataCommand().onCommand(player, new StubCommand(), "si", new String[0]);

        assertEquals("[SkysongItems] Usage: /si cmd <sub-command>",
                PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage()));
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
