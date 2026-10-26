package org.skysongdev.skysongitems.commands.books;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.BookDescriptions;
import org.skysongdev.skysongitems.database.BookQueries;
import org.skysongdev.skysongitems.testsupport.BookReadingPlayer;
import org.skysongdev.skysongitems.testsupport.InMemoryBooks;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The book commands against an in-memory database. Each query runs as soon as the
 * command does, but every reply and item change waits for the next server tick, the
 * way it waits for the database worker on a real server.
 */
class BookCommandsTest extends MockBukkitTest {
    private static final String SQL_ERROR = "[ERROR] There's an SQL error! (Check Console)";

    private SkysongItems plugin;
    private BookDescriptionRepository repository;
    private BookQueries bookQueries;
    private BookReadingPlayer player;

    @BeforeEach
    void setUp() throws SQLException {
        plugin = MockBukkit.load(SkysongItems.class);
        repository = InMemoryBooks.emptyRepository();
        bookQueries = InMemoryBooks.runningInline(plugin, repository);
        player = new BookReadingPlayer(server, "Reader");
        server.addPlayer(player);
    }

    @Test
    void saveBookStoresTheHeldBookAndRepliesWithItsId() throws SQLException {
        player.getInventory().setItemInMainHand(writtenBook("Tome", "First page", "Second page"));

        run(new SaveBook(bookQueries));

        assertEquals(List.of("[SkysongItems]Book has been saved to the database with the ID (1)"), repliesOnNextTick());
        BookDescriptions saved = repository.findById(1);
        assertEquals("Tome", saved.getName());
        assertEquals("First page[PageEnd]Second page", saved.getBookcontent());
    }

    @Test
    void setBookReplacesTheStoredBook() throws SQLException {
        repository.create(new BookDescriptions(0, "Draft", "Old"));
        player.getInventory().setItemInMainHand(writtenBook("Final", "New"));

        run(new SetBookContent(bookQueries), "1");

        assertEquals(List.of("[SkysongItems]Book has been edited!"), repliesOnNextTick());
        assertEquals("Final", repository.findById(1).getName());
        assertEquals("New", repository.findById(1).getBookcontent());
    }

    @Test
    void deleteBookRemovesItAndConfirms() throws SQLException {
        repository.create(new BookDescriptions(0, "Doomed", ""));

        run(new DeleteBook(bookQueries), "1");

        assertEquals(List.of("[SkysongItems]Book with id (1) has been deleted from the database"), repliesOnNextTick());
        assertNull(repository.findById(1));
    }

    @Test
    void listBooksShowsTheStoredBooks() throws SQLException {
        repository.create(new BookDescriptions(0, "First", "Content"));
        repository.create(new BookDescriptions(0, "Second", "Content"));

        run(new ListBooks(bookQueries));

        String rule = "===============================================";
        assertEquals(List.of(
                "List of set of 10 books number 1",
                rule,
                "First(1)",
                "Second(2)",
                rule + "\nTo view more books, type /si book list 2"), repliesOnNextTick());
    }

    @Test
    void listBooksOnAnEmptyDatabaseSaysSo() {
        run(new ListBooks(bookQueries));

        assertEquals(List.of("There are no books on the database!"), repliesOnNextTick());
    }

    @Test
    void openBookOpensTheStoredBookOnTheNextTick() throws SQLException {
        repository.create(new BookDescriptions(0, "Tome", "First page[PageEnd]Second page"));

        run(new OpenBook(bookQueries), "1");

        assertTrue(player.getOpenedBooks().isEmpty(), "the book opens once the query is done");
        server.getScheduler().performOneTick();
        BookMeta opened = (BookMeta) player.getOpenedBooks().get(0).getItemMeta();
        assertEquals("Tome", opened.getTitle());
        assertEquals("Skysong", opened.getAuthor());
        assertEquals(List.of("First page", "Second page"), opened.getPages());
    }

    @Test
    void openBookWithAMissingIdSaysSo() {
        run(new OpenBook(bookQueries), "7");

        assertEquals(List.of("[ERROR] Couldn't find a book with this id!"), repliesOnNextTick());
        assertTrue(player.getOpenedBooks().isEmpty());
    }

    @Test
    void openBookReportsAnSqlError() {
        BookQueries failing = InMemoryBooks.runningInline(plugin, new BookDescriptionRepository(() -> {
            throw new SQLException("Database down");
        }));

        run(new OpenBook(failing), "1");

        assertEquals(List.of(SQL_ERROR), repliesOnNextTick());
    }

    @Test
    void bindBookBindsTheHeldItemOnceTheBookIsFound() throws SQLException {
        repository.create(new BookDescriptions(0, "Tome", ""));
        player.getInventory().setItemInMainHand(new ItemStack(Material.PAPER));

        run(new BindBook(bookQueries), "1");

        assertNull(boundBook(), "the item is bound once the query is done");
        assertEquals(List.of("[SkysongItems] Book with id 1 bound successfully!"), repliesOnNextTick());
        assertEquals(1, boundBook());
    }

    @Test
    void bindBookWithAMissingIdLeavesTheItemUnbound() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.PAPER));

        run(new BindBook(bookQueries), "7");

        assertEquals(List.of("[ERROR] Couldn't find a book with this id!"), repliesOnNextTick());
        assertNull(boundBook());
    }

    private void run(CommandExecutor executor, String... args) {
        executor.onCommand(player, plugin.getCommand("skysongitems"), "si", args);
    }

    /** Checks that nothing was sent yet, runs one server tick, and returns what was sent then. */
    private List<String> repliesOnNextTick() {
        assertNull(player.nextComponentMessage(), "replies are sent once the query is done");
        server.getScheduler().performOneTick();
        List<String> replies = new ArrayList<>();
        Component message;
        while ((message = player.nextComponentMessage()) != null) {
            replies.add(PlainTextComponentSerializer.plainText().serialize(message));
        }
        return replies;
    }

    private Integer boundBook() {
        ItemStack held = player.getInventory().getItemInMainHand();
        return held.getItemMeta().getPersistentDataContainer().get(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER);
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
}
