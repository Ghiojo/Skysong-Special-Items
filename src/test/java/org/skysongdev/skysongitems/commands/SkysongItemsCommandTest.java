package org.skysongdev.skysongitems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.commands.misc.UpdateItem;
import org.skysongdev.skysongitems.commands.properties.PropertiesCommand;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkysongItemsCommandTest extends MockBukkitTest {
    private final RecordingPropertiesCommand propertiesCommand = new RecordingPropertiesCommand();
    private final RecordingUpdateItem updateItem = new RecordingUpdateItem();
    private SkysongItemsCommand siCommand;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        siCommand = new SkysongItemsCommand(propertiesCommand, updateItem);
        player = server.addPlayer();
    }

    @Test
    void propRunsOnlyThePropertiesCommand() {
        run("prop", "view");

        assertEquals(List.of(List.of("view")), propertiesCommand.calls);
        assertTrue(updateItem.calls.isEmpty(), "/si prop must not also run /si update");
    }

    @Test
    void propertyAliasRunsOnlyThePropertiesCommand() {
        run("property", "view");

        assertEquals(List.of(List.of("view")), propertiesCommand.calls);
        assertTrue(updateItem.calls.isEmpty(), "/si property must not also run /si update");
    }

    @Test
    void updateRunsOnlyTheUpdateCommand() {
        run("update");

        assertEquals(List.of(List.of()), updateItem.calls);
        assertTrue(propertiesCommand.calls.isEmpty());
    }

    private void run(String... args) {
        siCommand.onCommand(player, new StubCommand(), "si", args);
    }

    /** Records the arguments of every call instead of editing the held item. */
    private static final class RecordingPropertiesCommand extends PropertiesCommand {
        private final List<List<String>> calls = new ArrayList<>();

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            calls.add(List.of(args));
            return true;
        }
    }

    /** Records the arguments of every call instead of updating the held item. */
    private static final class RecordingUpdateItem extends UpdateItem {
        private final List<List<String>> calls = new ArrayList<>();

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            calls.add(List.of(args));
            return true;
        }
    }

    /** The subcommands never look at the command object, so any instance will do. */
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
