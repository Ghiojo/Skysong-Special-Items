package org.skysongdev.skysongitems.listeners;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which right-clicks open a bound book. The database lookup that follows
 * is not exercised here: every case below must stop before it, which is why the
 * listener can be built without a plugin.
 */
class OpenDescRightClickTest extends MockBukkitTest {
    private static final int BOOK_ID = 42;

    private OpenDescRightClick listener;
    private PlayerMock player;

    @BeforeEach
    void createListenerAndPlayer() {
        listener = new OpenDescRightClick(null);
        player = server.addPlayer();
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
