package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.event.ItemTagChangeEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the item HP edits of {@link ItemEditor}. They share their logic with the
 * charges edits, which {@link ItemEditorTest} covers in full, so these only check
 * that HP uses its own tags, trait and lore line, and has no adding lock.
 */
class ItemHpEditTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of("physical", "functional", "skill");

    private ItemEditor editor;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        editor = SkysongItems.getPlugin().getItemEditor();
    }

    private static ItemStack itemWithLore() {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData.from(meta).setLoreSections(SECTIONS);
        item.setItemMeta(meta);
        return item;
    }

    private static Integer read(ItemStack item, NamespacedKey key) {
        return item.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static String lastLoreLine(ItemStack item) {
        return plain(item.getItemMeta().lore().getLast());
    }

    @Test
    void setMaxItemHpGivesAnItemFullHpTheTraitAndItsLine() {
        ItemStack item = itemWithLore();

        assertEquals(ItemEditor.Result.DONE, editor.setMaxItemHp(item, 10));

        assertEquals(10, read(item, ItemKeys.ITEM_HP));
        assertEquals(10, read(item, ItemKeys.MAX_ITEM_HP));
        assertEquals(List.of("item_hp"), ItemData.from(item.getItemMeta()).getTraits());
        assertEquals("[HP: 10/10]", lastLoreLine(item));
        assertNull(read(item, ItemKeys.CHARGES));
    }

    @Test
    void theHpLineIsDrawnInRedRightAfterTheChargesLine() {
        ItemStack item = itemWithLore();
        editor.setMaxItemHp(item, 10);
        editor.setMaxCharges(item, 5);

        List<Component> lore = item.getItemMeta().lore();

        assertEquals("[Charges: 5/5]", plain(lore.get(lore.size() - 2)));
        assertEquals("[HP: 10/10]", plain(lore.getLast()));
        assertTrue(MiniMessage.miniMessage().serialize(lore.getLast()).toLowerCase().contains("#e13232"));
    }

    @Test
    void addItemHpIsKeptBetweenZeroAndTheMax() {
        ItemStack item = itemWithLore();
        editor.setMaxItemHp(item, 10);

        editor.addItemHp(item, -15);
        assertEquals(0, read(item, ItemKeys.ITEM_HP));

        editor.addItemHp(item, 25);
        assertEquals(10, read(item, ItemKeys.ITEM_HP));
    }

    @Test
    void addItemHpNeedsAnItemWithHpEvenWhenItHasCharges() {
        ItemStack item = itemWithLore();
        editor.setMaxCharges(item, 5);

        assertEquals(ItemEditor.Result.NO_HP, editor.addItemHp(item, 1));

        assertNull(read(item, ItemKeys.ITEM_HP));
    }

    @Test
    void addItemHpIgnoresTheChargesAddingLock() {
        ItemStack item = itemWithLore();
        editor.setMaxItemHp(item, 10);
        editor.addItemHp(item, -4);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, false);
        item.setItemMeta(meta);

        assertEquals(ItemEditor.Result.DONE, editor.addItemHp(item, 2));

        assertEquals(8, read(item, ItemKeys.ITEM_HP));
    }

    @Test
    void removeItemHpLeavesTheCharges() {
        ItemStack item = itemWithLore();
        editor.setMaxItemHp(item, 10);
        editor.setMaxCharges(item, 5);

        editor.removeItemHp(item);

        assertFalse(item.getPersistentDataContainer().has(ItemKeys.ITEM_HP));
        assertFalse(item.getPersistentDataContainer().has(ItemKeys.MAX_ITEM_HP));
        assertEquals(List.of("charges"), ItemData.from(item.getItemMeta()).getTraits());
        assertEquals(5, read(item, ItemKeys.CHARGES));
    }

    @Test
    void anHpChangeFiresATagChangeEvent() {
        ItemStack item = itemWithLore();
        editor.setMaxItemHp(item, 10);
        RecordedItemEvents events = RecordedItemEvents.listenOn(server);

        editor.addItemHp(item, -3);

        ItemTagChangeEvent event = events.onlyTagChange();
        assertEquals("item_hp", event.getTagId());
        assertEquals(10, event.getOldValue());
        assertEquals(7, event.getNewValue());
        assertTrue(events.chargeChanges().isEmpty());
    }
}
