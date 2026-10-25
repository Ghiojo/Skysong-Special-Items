package org.skysongdev.skysongitems.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.BookDescriptions;
import org.skysongdev.skysongitems.testsupport.BookReadingPlayer;
import org.skysongdev.skysongitems.testsupport.InMemoryBooks;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which right-clicks open a bound book, and what happens once the book has
 * been looked up in an in-memory database on the next tick.
 */
class OpenDescRightClickTest extends MockBukkitTest {
    private static final int BOOK_ID = 42;

    private BookDescriptionRepository repository;
    private OpenDescRightClick listener;
    private BookReadingPlayer player;

    @BeforeEach
    void createListenerAndPlayer() throws SQLException {
        repository = InMemoryBooks.emptyRepository();
        listener = new OpenDescRightClick(InMemoryBooks.runningInline(MockBukkit.createMockPlugin(), repository));
        player = new BookReadingPlayer(server, "Reader");
        server.addPlayer(player);
    }

    @Test
    void rightClickOpensTheBoundBookOnTheNextTick() throws SQLException {
        repository.create(new BookDescriptions(0, "Tome", "First page[PageEnd]Second page"));
        ItemStack item = rightClickableItem(1);
        player.getInventory().setItemInMainHand(item);

        listener.onPlayerItemUse(rightClick(item, EquipmentSlot.HAND));

        assertTrue(player.getOpenedBooks().isEmpty(), "the book opens once the query is done");
        server.getScheduler().performOneTick();
        BookMeta opened = (BookMeta) player.getOpenedBooks().get(0).getItemMeta();
        assertEquals("Tome", opened.getTitle());
        assertEquals(List.of("First page", "Second page"), opened.getPages());
    }

    @Test
    void rightClickOnAMissingBookSaysSo() {
        ItemStack item = rightClickableItem(BOOK_ID);
        player.getInventory().setItemInMainHand(item);

        listener.onPlayerItemUse(rightClick(item, EquipmentSlot.HAND));
        server.getScheduler().performOneTick();

        assertEquals("[ERROR] Couldn't find a book with this id!", nextPlainMessage());
        assertTrue(player.getOpenedBooks().isEmpty());
    }

    @Test
    void dmOnlyBookIsRefusedWithoutTheDmPermission() throws SQLException {
        repository.create(new BookDescriptions(0, "Secret", "Hidden"));
        ItemStack item = dmOnly(rightClickableItem(1));
        player.getInventory().setItemInMainHand(item);

        listener.onPlayerItemUse(rightClick(item, EquipmentSlot.HAND));
        server.getScheduler().performOneTick();

        assertEquals("[SkysongItems] You do not have permission to view this lore", nextPlainMessage());
        assertTrue(player.getOpenedBooks().isEmpty());
    }

    @Test
    void dmOnlyBookOpensWithTheDmPermission() throws SQLException {
        repository.create(new BookDescriptions(0, "Secret", "Hidden"));
        ItemStack item = dmOnly(rightClickableItem(1));
        player.getInventory().setItemInMainHand(item);
        player.addAttachment(MockBukkit.createMockPlugin(), "skysongspecialitems.dm", true);

        listener.onPlayerItemUse(rightClick(item, EquipmentSlot.HAND));
        server.getScheduler().performOneTick();

        assertEquals(1, player.getOpenedBooks().size());
    }

    @Test
    void rightClickableItemWithoutBoundBookDoesNotThrow() {
        ItemStack item = rightClickableItem(null);
        player.getInventory().setItemInMainHand(item);
        PlayerInteractEvent event = rightClick(item, EquipmentSlot.HAND);

        assertDoesNotThrow(() -> listener.onPlayerItemUse(event));
        assertTrue(listener.boundBookToOpen(event).isEmpty());
    }

    @Test
    void mainHandOpensItsBoundBook() {
        ItemStack item = rightClickableItem(BOOK_ID);
        player.getInventory().setItemInMainHand(item);

        assertEquals(OptionalInt.of(BOOK_ID), listener.boundBookToOpen(rightClick(item, EquipmentSlot.HAND)));
    }

    @Test
    void offHandDoesNotOpenASecondBookWhenMainHandAlreadyDoes() {
        ItemStack mainHandItem = rightClickableItem(BOOK_ID);
        ItemStack offHandItem = rightClickableItem(BOOK_ID + 1);
        player.getInventory().setItemInMainHand(mainHandItem);
        player.getInventory().setItemInOffHand(offHandItem);

        assertTrue(listener.boundBookToOpen(rightClick(offHandItem, EquipmentSlot.OFF_HAND)).isEmpty());
    }

    @Test
    void offHandStillOpensItsBookWhenMainHandIsEmpty() {
        ItemStack offHandItem = rightClickableItem(BOOK_ID);
        player.getInventory().setItemInOffHand(offHandItem);

        assertEquals(OptionalInt.of(BOOK_ID), listener.boundBookToOpen(rightClick(offHandItem, EquipmentSlot.OFF_HAND)));
    }

    @Test
    void emptyHandOpensNothing() {
        PlayerInteractEvent event = rightClick(null, EquipmentSlot.HAND);

        assertDoesNotThrow(() -> listener.onPlayerItemUse(event));
        assertTrue(listener.boundBookToOpen(event).isEmpty());
    }

    @Test
    void itemWithoutTheRightClickFlagOpensNothing() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER, BOOK_ID);
        item.setItemMeta(meta);

        assertTrue(listener.boundBookToOpen(rightClick(item, EquipmentSlot.HAND)).isEmpty());
    }

    @Test
    void leftClickOpensNothing() {
        ItemStack item = rightClickableItem(BOOK_ID);
        PlayerInteractEvent event = new PlayerInteractEvent(
                player, Action.LEFT_CLICK_AIR, item, null, BlockFace.SELF, EquipmentSlot.HAND);

        assertTrue(listener.boundBookToOpen(event).isEmpty());
    }

    private static ItemStack dmOnly(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.DM_ONLY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    private String nextPlainMessage() {
        Component message = player.nextComponentMessage();
        return message == null ? null : PlainTextComponentSerializer.plainText().serialize(message);
    }

    /** An item with the right-click flag set, bound to {@code bookId} unless it is null. */
    private static ItemStack rightClickableItem(Integer bookId) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemKeys.RIGHT_CLICKABLE, PersistentDataType.BOOLEAN, true);
        if (bookId != null) {
            container.set(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER, bookId);
        }
        item.setItemMeta(meta);
        return item;
    }

    private PlayerInteractEvent rightClick(ItemStack item, EquipmentSlot hand) {
        return new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, item, null, BlockFace.SELF, hand);
    }
}
