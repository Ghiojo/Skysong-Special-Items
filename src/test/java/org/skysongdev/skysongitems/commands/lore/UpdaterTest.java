package org.skysongdev.skysongitems.commands.lore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests the item version steps of {@code /si update}: items made before item
 * versions existed get their stored lore sections rebuilt from the visible lore,
 * and items before version 1.2 get their traits moved to the {@code traits} entry.
 */
class UpdaterTest extends MockBukkitTest {
    private final Updater updater = new Updater();

    private static ItemStack legacyItem(String deletedLines, Component... lore) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(lore));
        if (deletedLines != null) {
            ItemData.from(meta).setDeletedLines(deletedLines);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack versionedItem(String version, String legacyTraits) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(ItemKeys.VERSION, PersistentDataType.STRING, version);
        if (legacyTraits != null) {
            data.set(ItemKeys.LEGACY_TRAITS, PersistentDataType.STRING, legacyTraits);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static PersistentDataContainer dataOf(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer();
    }

    private static List<String> storedSections(ItemStack item) {
        return ItemData.from(item.getItemMeta()).getLoreSections();
    }

    @Test
    void storesEachLoreSectionInItsOwnSlot() {
        ItemStack item = legacyItem(null, Component.text("Hello", NamedTextColor.GRAY));

        ItemStack updated = updater.UpdateZero(item);

        assertEquals(List.of("<gray>Hello", "<#424242>[Not Set]", "<#81a871>[Not Set]"), storedSections(updated));
    }

    @Test
    void keepsASectionWithTheSameTextAsTheFirst() {
        // The physical section is hidden, so it keeps its "<gray>[Not Set]" placeholder,
        // and the visible lore reads back as a functional section with that same text.
        ItemStack item = legacyItem("100", Component.text("[Not Set]", NamedTextColor.GRAY));

        ItemStack updated = updater.UpdateZero(item);

        assertEquals(List.of("<gray>[Not Set]", "<gray>[Not Set]", "<#81a871>[Not Set]"), storedSections(updated));
    }

    @Test
    void marksTheItemAsVersionOnePointZero() {
        ItemStack item = legacyItem(null, Component.text("Hello", NamedTextColor.GRAY));

        ItemStack updated = updater.UpdateZero(item);

        assertEquals("1.0", updated.getItemMeta().getPersistentDataContainer().get(ItemKeys.VERSION, PersistentDataType.STRING));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0", "1.1"})
    void traitsMoveFromThePropertiesEntryToTheTraitsEntry(String version) {
        ItemStack updated = updater.upgrade(versionedItem(version, "charges;spell"));

        PersistentDataContainer data = dataOf(updated);
        assertEquals("charges;spell", data.get(ItemKeys.TRAITS, PersistentDataType.STRING));
        assertFalse(data.has(ItemKeys.LEGACY_TRAITS));
        assertEquals(Constants.ITEM_VERSION, data.get(ItemKeys.VERSION, PersistentDataType.STRING));
    }

    @Test
    void anItemWithoutTraitsGetsNoTraitsEntry() {
        ItemStack updated = updater.upgrade(versionedItem("1.1", null));

        assertFalse(dataOf(updated).has(ItemKeys.TRAITS));
        assertEquals(Constants.ITEM_VERSION, dataOf(updated).get(ItemKeys.VERSION, PersistentDataType.STRING));
    }

    @Test
    void traitsAlreadyMovedByAnEditAreKept() {
        ItemStack item = versionedItem("1.1", "spell");
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.TRAITS, PersistentDataType.STRING, "charges");
        item.setItemMeta(meta);

        PersistentDataContainer data = dataOf(updater.upgrade(item));

        assertEquals("charges", data.get(ItemKeys.TRAITS, PersistentDataType.STRING));
        assertFalse(data.has(ItemKeys.LEGACY_TRAITS));
    }

    @Test
    void aCurrentItemIsLeftAsItIs() {
        ItemStack item = versionedItem(Constants.ITEM_VERSION, "spell");

        assertFalse(dataOf(updater.upgrade(item)).has(ItemKeys.TRAITS));
    }
}
