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
    private PersistentDataContainer container;
    private ItemData data;

    @BeforeEach
    void createItemData() {
        ItemMeta meta = new ItemStack(Material.STONE).getItemMeta();
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
    void anItemWithoutTraitsHasAnEmptyList() {
        assertFalse(data.hasTraits());
        assertEquals(List.of(), data.getTraits());
        assertNull(data.getTraitsText());
    }

    @Test
    void addingTraitsAppendsThemSeparatedBySemicolons() {
        data.addTrait("charges");
        assertEquals("charges", storedString(ItemKeys.TRAITS));

        data.addTrait("spell");
        assertEquals("charges;spell", storedString(ItemKeys.TRAITS));
        assertEquals(List.of("charges", "spell"), data.getTraits());
    }

    @Test
    void hasTraitMatchesWholeIdsOnly() {
        data.setTraitsText("charges;spell");

        assertTrue(data.hasTrait("spell"));
        assertFalse(data.hasTrait("charge"));
    }

    @Test
    void setTraitsReplacesTheStoredList() {
        data.setTraitsText("charges;spell");

        data.setTraits(List.of("test"));

        assertEquals("test", storedString(ItemKeys.TRAITS));
    }

    @Test
    void settingAnEmptyListKeepsAnEmptyEntry() {
        data.setTraits(List.of());

        assertTrue(data.hasTraits());
        assertEquals("", data.getTraitsText());
    }

    @Test
    void blankIdsLeftByOlderVersionsAreSkipped() {
        data.setTraitsText(";charges;;spell");

        assertEquals(List.of("charges", "spell"), data.getTraits());
    }

    @Test
    void anEmptyEntryCountsAsNoTraitIds() {
        data.setTraitsText("");

        assertEquals(List.of(), data.getTraits());
    }

    @Test
    void removeTraitKeepsTheOtherIds() {
        data.setTraitsText("charges;spell;test");

        assertTrue(data.removeTrait("spell"));

        assertEquals("charges;test", storedString(ItemKeys.TRAITS));
    }

    @Test
    void removingTheLastTraitRemovesTheEntry() {
        data.setTraitsText("charges");

        assertTrue(data.removeTrait("charges"));

        assertFalse(data.hasTraits());
        assertFalse(container.has(ItemKeys.TRAITS));
    }

    @Test
    void removingAMissingTraitChangesNothing() {
        data.setTraitsText("charges");

        assertFalse(data.removeTrait("spell"));

        assertEquals("charges", storedString(ItemKeys.TRAITS));
    }

    @Test
    void clearTraitsRemovesTheEntry() {
        data.setTraitsText("charges");

        data.clearTraits();

        assertFalse(container.has(ItemKeys.TRAITS));
    }

    @Test
    void traitsOfAnItemNotYetUpdatedAreReadFromTheOldEntry() {
        container.set(ItemKeys.LEGACY_TRAITS, PersistentDataType.STRING, "charges;spell");

        assertTrue(data.hasTraits());
        assertEquals(List.of("charges", "spell"), data.getTraits());
    }

    @Test
    void theTraitsEntryWinsOverTheOldOne() {
        container.set(ItemKeys.LEGACY_TRAITS, PersistentDataType.STRING, "spell");
        container.set(ItemKeys.TRAITS, PersistentDataType.STRING, "charges");

        assertEquals(List.of("charges"), data.getTraits());
    }

    @Test
    void editingTheTraitsOfAnItemNotYetUpdatedMovesThemToTheTraitsEntry() {
        container.set(ItemKeys.LEGACY_TRAITS, PersistentDataType.STRING, "charges");

        data.addTrait("spell");

        assertEquals("charges;spell", storedString(ItemKeys.TRAITS));
        assertFalse(container.has(ItemKeys.LEGACY_TRAITS));
    }

    @Test
    void removingTheLastTraitOfAnItemNotYetUpdatedRemovesTheOldEntry() {
        container.set(ItemKeys.LEGACY_TRAITS, PersistentDataType.STRING, "charges");

        assertTrue(data.removeTrait("charges"));

        assertFalse(data.hasTraits());
        assertFalse(container.has(ItemKeys.LEGACY_TRAITS));
    }

    @Test
    void changesReachTheItemOnceTheMetaIsApplied() {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta itemMeta = item.getItemMeta();
        ItemData.from(itemMeta).setLoreSections(List.of("physical", "functional", "skill"));
        ItemData.from(itemMeta).addTrait("charges");
        item.setItemMeta(itemMeta);

        ItemData stored = ItemData.from(item.getItemMeta());
        assertEquals(List.of("physical", "functional", "skill"), stored.getLoreSections());
        assertEquals(List.of("charges"), stored.getTraits());
    }

    @Test
    void parseTraitsSplitsStoredTextSkippingBlanks() {
        assertEquals(List.of("charges", "spell"), ItemData.parseTraits("charges;;spell;"));
        assertTrue(ItemData.parseTraits(null).isEmpty());
    }
}
