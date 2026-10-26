package org.skysongdev.skysongitems.commands.books;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.commands.DBReload;
import org.skysongdev.skysongitems.commands.Subcommand;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.BookDescriptions;
import org.skysongdev.skysongitems.testsupport.BookReadingPlayer;
import org.skysongdev.skysongitems.testsupport.InMemoryBooks;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins down how {@code /si book} routes its arguments: which handler runs, who may
 * run it, and what tab completion offers. The handlers themselves are tested in
 * {@link BookCommandsTest}.
 */
class BookCommandTest extends MockBukkitTest {
    private static final String ITEM_EDIT_PERMISSION = "skysongspecialitems.cit";
    private static final List<String> ALL_SUBCOMMAND_NAMES = List.of(
            "open", "view", "list", "save", "edit", "delete", "bind", "unbind", "dmonly", "rightclick", "reload");
    /** Subcommands whose handlers act on the sender's own player and inventory. */
    private static final List<String> PLAYER_ONLY_SUBCOMMANDS = List.of(
            "open", "view", "save", "edit", "bind", "unbind", "dmonly", "rightclick");
    private static final String NO_PERMISSION = "[SkysongItems] You don't have permissions to do that command!";

    private BookDescriptionRepository repository;
    private BookCommand bookCommand;
    private BookReadingPlayer player;

    @BeforeEach
    void setUp() throws SQLException {
        SkysongItems plugin = MockBukkit.load(SkysongItems.class);
        repository = InMemoryBooks.emptyRepository();
        bookCommand = new BookCommand(InMemoryBooks.runningInline(plugin, repository));
        player = new BookReadingPlayer(server, "Reader");
        server.addPlayer(player);
    }

    static Stream<Arguments> handlerForEachName() {
        return Stream.of(
                Arguments.of("open", OpenBook.class),
                Arguments.of("view", ViewExtendedLore.class),
                Arguments.of("list", ListBooks.class),
                Arguments.of("save", SaveBook.class),
                Arguments.of("edit", SetBookContent.class),
                Arguments.of("delete", DeleteBook.class),
                Arguments.of("bind", BindBook.class),
                Arguments.of("unbind", UnbindBook.class),
                Arguments.of("dmonly", SetDMOnly.class),
                Arguments.of("rightclick", SetRightClickBook.class),
                Arguments.of("reload", DBReload.class));
    }

    @ParameterizedTest
    @MethodSource("handlerForEachName")
    void eachNameResolvesToItsHandler(String name, Class<?> handler) {
        assertInstanceOf(handler, bookCommand.getRegistry().find(name).orElseThrow().executor());
    }

    @Test
    void everySubcommandButViewIsRefusedWithoutPermission() {
        for (String name : ALL_SUBCOMMAND_NAMES) {
            if (!"view".equals(name)) {
                run(name);

                assertEquals(NO_PERMISSION, nextPlainMessage(), "/si book " + name);
            }
        }
    }

    @Test
    void playerOnlyListMatchesTheRegistry() {
        List<String> registered = bookCommand.getRegistry().all().stream()
                .filter(Subcommand::isPlayerOnly)
                .map(subcommand -> subcommand.names().get(0))
                .toList();

        assertEquals(PLAYER_ONLY_SUBCOMMANDS, registered);
    }

    @Test
    void playerOnlySubcommandsAreRefusedToTheConsole() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        for (String name : PLAYER_ONLY_SUBCOMMANDS) {
            assertDoesNotThrow(() -> bookCommand.onCommand(console, new StubCommand(), "si", new String[]{name, "1"}),
                    "/si book " + name);

            String message = PlainTextComponentSerializer.plainText().serialize(console.nextComponentMessage());
            assertEquals("[SkysongItems] This command can only be used by players!", message, "/si book " + name);
        }
    }

    @Test
    void noSubcommandAsksForOne() {
        run();

        assertEquals("[ERROR] Too Little arguments! (Usage: /si book (subcommand))", nextPlainMessage());
    }

    @Test
    void unknownSubcommandAsksForValidArguments() {
        run("savebook");

        assertEquals("[SkysongItems] Please introduce valid arguments!", nextPlainMessage());
    }

    @Test
    void tabCompletionListsEverySubcommandToItemEditors() {
        player.addAttachment(MockBukkit.createMockPlugin(), ITEM_EDIT_PERMISSION, true);

        assertEquals(ALL_SUBCOMMAND_NAMES, tabComplete(""));
    }

    @Test
    void tabCompletionOffersOnlyViewWithoutPermission() {
        assertEquals(List.of("view"), tabComplete(""));
    }

    @Test
    void tabCompletionFiltersByTypedPrefix() {
        player.addAttachment(MockBukkit.createMockPlugin(), ITEM_EDIT_PERMISSION, true);

        assertEquals(List.of("delete", "dmonly"), tabComplete("d"));
    }

    @Test
    void saveStoresTheHeldBook() throws SQLException {
        player.addAttachment(MockBukkit.createMockPlugin(), ITEM_EDIT_PERMISSION, true);
        player.getInventory().setItemInMainHand(writtenBook("Tome", "Only page"));

        run("save");
        server.getScheduler().performOneTick();

        assertEquals("[SkysongItems]Book has been saved to the database with the ID (1)", nextPlainMessage());
        assertEquals("Tome", repository.findById(1).getName());
    }

    @Test
    void listPagesPointAtTheNewCommand() throws SQLException {
        player.addAttachment(MockBukkit.createMockPlugin(), ITEM_EDIT_PERMISSION, true);
        repository.create(new BookDescriptions(0, "First", "Content"));

        run("list");
        server.getScheduler().performOneTick();

        List<String> replies = Stream.generate(player::nextComponentMessage)
                .takeWhile(Objects::nonNull)
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();
        assertTrue(replies.get(replies.size() - 1).endsWith("To view more books, type /si book list 2"));
    }

    @Test
    void viewOpensTheBoundBookForAPlayerWithoutPermission() throws SQLException {
        repository.create(new BookDescriptions(0, "Lore", "Bound page"));
        player.getInventory().setItemInMainHand(itemBoundTo(1));

        run("view");
        server.getScheduler().performOneTick();

        BookMeta opened = (BookMeta) player.getOpenedBooks().get(0).getItemMeta();
        assertEquals("Lore", opened.getTitle());
    }

    private void run(String... args) {
        bookCommand.onCommand(player, new StubCommand(), "si", args);
    }

    private List<String> tabComplete(String... args) {
        return bookCommand.onTabComplete(player, new StubCommand(), "si", args);
    }

    private String nextPlainMessage() {
        Component message = player.nextComponentMessage();
        return PlainTextComponentSerializer.plainText().serialize(message);
    }

    private static ItemStack itemBoundTo(int bookId) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER, bookId);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack writtenBook(String title, String... pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(title);
        meta.setAuthor("Writer");
        meta.addPage(pages);
        book.setItemMeta(meta);
        return book;
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
