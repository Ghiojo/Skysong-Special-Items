package org.skysongdev.skysongitems.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.commands.books.BindBook;
import org.skysongdev.skysongitems.commands.books.DeleteBook;
import org.skysongdev.skysongitems.commands.books.ListBooks;
import org.skysongdev.skysongitems.commands.books.OpenBook;
import org.skysongdev.skysongitems.commands.books.SaveBook;
import org.skysongdev.skysongitems.commands.books.SetBookContent;
import org.skysongdev.skysongitems.commands.books.SetDMOnly;
import org.skysongdev.skysongitems.commands.books.SetRightClickBook;
import org.skysongdev.skysongitems.commands.books.UnbindBook;
import org.skysongdev.skysongitems.commands.books.ViewExtendedLore;
import org.skysongdev.skysongitems.commands.charges.ChargesCommand;
import org.skysongdev.skysongitems.commands.custommodeldata.CustomModelDataCommand;
import org.skysongdev.skysongitems.commands.lore.RenameCommand;
import org.skysongdev.skysongitems.commands.misc.LoadItem;
import org.skysongdev.skysongitems.commands.misc.ReloadCommand;
import org.skysongdev.skysongitems.commands.misc.SaveItem;
import org.skysongdev.skysongitems.commands.misc.SetItemModel;
import org.skysongdev.skysongitems.commands.misc.UpdateItem;
import org.skysongdev.skysongitems.commands.properties.PropertiesCommand;
import org.skysongdev.skysongitems.commands.tags.TagCommand;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

    /** Subcommands anyone may run; every other one needs the item edit permission. */
    private static final List<String> SUBCOMMANDS_WITHOUT_PERMISSION = List.of("viewdesc", "prop", "property", "update");

    /** Subcommands whose handlers act on the sender's own player and inventory. */
    private static final List<String> PLAYER_ONLY_SUBCOMMANDS = List.of(
            "disable", "openbook", "savebook", "setbook", "bindbook", "viewdesc", "setdm", "setrc",
            "unbind", "updatelore", "update", "reload", "rename");

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

    static Stream<Arguments> handlerForEachName() {
        return Stream.of(
                Arguments.of("disable", DisableFunc.class),
                Arguments.of("dis", DisableFunc.class),
                Arguments.of("openbook", OpenBook.class),
                Arguments.of("savebook", SaveBook.class),
                Arguments.of("setbook", SetBookContent.class),
                Arguments.of("delbook", DeleteBook.class),
                Arguments.of("bindbook", BindBook.class),
                Arguments.of("viewdesc", ViewExtendedLore.class),
                Arguments.of("listbooks", ListBooks.class),
                Arguments.of("setdm", SetDMOnly.class),
                Arguments.of("setrc", SetRightClickBook.class),
                Arguments.of("unbind", UnbindBook.class),
                Arguments.of("dbreload", DBReload.class),
                Arguments.of("tag", TagCommand.class),
                Arguments.of("prop", PropertiesCommand.class),
                Arguments.of("property", PropertiesCommand.class),
                Arguments.of("update", UpdateItem.class),
                Arguments.of("reload", ReloadCommand.class),
                Arguments.of("rename", RenameCommand.class),
                Arguments.of("charges", ChargesCommand.class),
                Arguments.of("loaditem", LoadItem.class),
                Arguments.of("saveitem", SaveItem.class),
                Arguments.of("setmodel", SetItemModel.class),
                Arguments.of("cmd", CustomModelDataCommand.class));
    }

    @ParameterizedTest
    @MethodSource("handlerForEachName")
    void eachNameResolvesToItsHandler(String name, Class<?> handler) {
        assertInstanceOf(handler, siCommand.getRegistry().find(name).orElseThrow().executor());
    }

    @Test
    void updateloreIsRegistered() {
        assertTrue(siCommand.getRegistry().find("updatelore").isPresent());
    }

    @Test
    void everyItemEditSubcommandIsRefusedWithoutPermission() {
        List<String> itemEditNames = siCommand.getRegistry().all().stream()
                .map(subcommand -> subcommand.listedNames().get(0))
                .filter(name -> !SUBCOMMANDS_WITHOUT_PERMISSION.contains(name))
                .toList();

        for (String name : itemEditNames) {
            run(name);

            assertEquals("[SkysongItems] You don't have permissions to do that command!", nextPlainMessage(), "/si " + name);
        }
    }

    @Test
    void playerOnlySubcommandsAreRefusedToTheConsole() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        for (String name : PLAYER_ONLY_SUBCOMMANDS) {
            assertDoesNotThrow(() -> siCommand.onCommand(console, new StubCommand(), "si", new String[]{name, "1"}), "/si " + name);

            String message = PlainTextComponentSerializer.plainText().serialize(console.nextComponentMessage());
            assertEquals("[SkysongItems] This command can only be used by players!", message, "/si " + name);
        }
        assertTrue(updateItem.calls.isEmpty(), "/si update must not reach its handler from the console");
    }

    @Test
    void playerOnlyListMatchesTheRegistry() {
        List<String> registered = siCommand.getRegistry().all().stream()
                .filter(Subcommand::isPlayerOnly)
                .map(subcommand -> subcommand.listedNames().get(0))
                .toList();

        assertEquals(PLAYER_ONLY_SUBCOMMANDS, registered);
    }

    @Test
    void noSubcommandAsksForOne() {
        run();

        assertEquals("[ERROR] Too Little arguments! (Usage: /si (subcommand))", nextPlainMessage());
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
