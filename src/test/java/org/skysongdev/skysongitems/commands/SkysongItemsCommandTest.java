package org.skysongdev.skysongitems.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.commands.misc.UpdateItem;
import org.skysongdev.skysongitems.commands.properties.PropertiesCommand;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins down how {@code /si} routes its arguments: which handler runs, the
 * messages for unknown and forbidden subcommands, and what tab completion offers.
 */
class SkysongItemsCommandTest extends MockBukkitTest {
    private static final String ITEM_EDIT_PERMISSION = "skysongspecialitems.cit";
    private static final List<String> ALL_LISTED_SUBCOMMANDS = List.of(
            "disable", "openbook", "savebook", "setbook", "delbook", "bindbook", "viewdesc", "listbooks",
            "setdm", "setrc", "unbind", "dbreload", "updatelore", "tag", "prop", "property", "update",
            "reload", "rename", "charges", "loaditem", "saveitem", "setmodel", "cmd");
    private static final List<String> SUBCOMMANDS_LISTED_WITHOUT_PERMISSION = List.of(
            "viewdesc", "update", "rename", "charges");

    private final RecordingPropertiesCommand propertiesCommand = new RecordingPropertiesCommand();
    private final RecordingUpdateItem updateItem = new RecordingUpdateItem();
    private SkysongItemsCommand siCommand;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        // The error messages are built with the plugin's MiniMessage instance.
        MockBukkit.load(SkysongItems.class);
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

    @Test
    void subcommandNameIsCaseInsensitive() {
        run("PROP", "view");

        assertEquals(List.of(List.of("view")), propertiesCommand.calls);
    }

    @Test
    void unknownSubcommandAsksForValidArguments() {
        run("nonsense");

        assertEquals("[SkysongItems] Please introduce valid arguments!", nextPlainMessage());
    }

    @Test
    void itemEditSubcommandIsRefusedWithoutPermission() {
        run("reload");

        assertEquals("[SkysongItems] You don't have permissions to do that command!", nextPlainMessage());
    }

    @Test
    void tabCompletionListsEverySubcommandToItemEditors() {
        player.addAttachment(MockBukkit.createMockPlugin(), ITEM_EDIT_PERMISSION, true);

        assertEquals(ALL_LISTED_SUBCOMMANDS, tabComplete(""));
    }

    @Test
    void tabCompletionListsOnlyPlayerSubcommandsWithoutPermission() {
        assertEquals(SUBCOMMANDS_LISTED_WITHOUT_PERMISSION, tabComplete(""));
    }

    @Test
    void tabCompletionFiltersByTypedPrefix() {
        player.addAttachment(MockBukkit.createMockPlugin(), ITEM_EDIT_PERMISSION, true);

        assertEquals(List.of("savebook", "setbook", "setdm", "setrc", "saveitem", "setmodel"), tabComplete("s"));
    }

    @Test
    void tabCompletionOfArgumentsForSubcommandWithoutCompleterIsBlank() {
        assertEquals(List.of(""), tabComplete("openbook", ""));
    }

    private void run(String... args) {
        siCommand.onCommand(player, new StubCommand(), "si", args);
    }

    private List<String> tabComplete(String... args) {
        return siCommand.onTabComplete(player, new StubCommand(), "si", args);
    }

    private String nextPlainMessage() {
        Component message = player.nextComponentMessage();
        return PlainTextComponentSerializer.plainText().serialize(message);
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
