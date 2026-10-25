package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemDataTest extends MockBukkitTest {
    private ItemMeta meta;
    private PersistentDataContainer container;
    private ItemData data;

    @BeforeEach
    void createItemData() {
        meta = new ItemStack(Material.STONE).getItemMeta();
        container = meta.getPersistentDataContainer();
        data = ItemData.from(meta);
    }

    private String storedString(NamespacedKey key) {
        return container.get(key, PersistentDataType.STRING);
    }

    @Test
    void loreSectionsAreStoredJoinedBySectionSign() {
        data.setLoreSections(List.of("physical", "functional", "skill"));

        assertEquals("physical§functional§skill", storedString(ItemKeys.LORE));
    }

    @Test
    void loreSectionsAreReadFromTheStoredFormat() {
        container.set(ItemKeys.LORE, PersistentDataType.STRING, "physical\nsecond line§functional§skill");

        assertEquals(List.of("physical\nsecond line", "functional", "skill"), data.getLoreSections());
    }

    @Test
    void loreSectionsCanBeChangedByTheCaller() {
        data.setLoreSections(List.of("physical", "functional", "skill"));

        List<String> sections = data.getLoreSections();
        sections.set(1, "changed");

        assertEquals(List.of("physical", "changed", "skill"), sections);
    }

    @Test
    void readingLoreFailsWhenTheItemHasNone() {
        assertThrows(NullPointerException.class, data::getLoreSections);
    }

    @Test
    void deletedLinesAreNullUntilASectionIsHidden() {
        assertFalse(data.hasDeletedLines());
        assertNull(data.getDeletedLines());
    }

    @ParameterizedTest
    @CsvSource({"0, 100", "1, 010", "2, 001"})
    void togglingWithoutAMaskHidesOnlyThatSection(int section, String expectedMask) {
        data.toggleSectionDeleted(section);

        assertEquals(expectedMask, data.getDeletedLines());
    }

    @Test
    void togglingFlipsOnlyThatSectionOfAnExistingMask() {
        data.setDeletedLines("101");

        data.toggleSectionDeleted(0);
        assertEquals("001", storedString(ItemKeys.DELETED_LINES));

        data.toggleSectionDeleted(1);
        assertEquals("011", storedString(ItemKeys.DELETED_LINES));
    }

    @Test
    void anItemWithoutPropertiesHasAnEmptyList() {
        assertFalse(data.hasProperties());
        assertEquals(List.of(), data.getProperties());
        assertNull(data.getPropertiesText());
    }

    @Test
    void addingPropertiesAppendsThemSeparatedBySemicolons() {
        data.addProperty("charges");
        assertEquals("charges", storedString(ItemKeys.PROPERTIES));

        data.addProperty("spell");
        assertEquals("charges;spell", storedString(ItemKeys.PROPERTIES));
        assertEquals(List.of("charges", "spell"), data.getProperties());
    }

    @Test
    void hasPropertyMatchesWholeIdsOnly() {
        data.setPropertiesText("charges;spell");

        assertTrue(data.hasProperty("spell"));
        assertFalse(data.hasProperty("charge"));
    }

    @Test
    void setPropertiesReplacesTheStoredList() {
        data.setPropertiesText("charges;spell");

        data.setProperties(List.of("test"));

        assertEquals("test", storedString(ItemKeys.PROPERTIES));
    }

    @Test
    void settingAnEmptyListKeepsAnEmptyEntry() {
        data.setProperties(List.of());

        assertTrue(data.hasProperties());
        assertEquals("", data.getPropertiesText());
    }

    @Test
    void clearPropertiesRemovesTheEntry() {
        data.setPropertiesText("charges");

        data.clearProperties();

        assertFalse(container.has(ItemKeys.PROPERTIES));
    }

    @Test
    void changesReachTheItemOnceTheMetaIsApplied() {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta itemMeta = item.getItemMeta();
        ItemData.from(itemMeta).setLoreSections(List.of("physical", "functional", "skill"));
        ItemData.from(itemMeta).addProperty("charges");
        item.setItemMeta(itemMeta);

        ItemData stored = ItemData.from(item.getItemMeta());
        assertEquals(List.of("physical", "functional", "skill"), stored.getLoreSections());
        assertEquals(List.of("charges"), stored.getProperties());
    }
}
