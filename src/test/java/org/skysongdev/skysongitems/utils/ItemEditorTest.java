package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ItemEditor} against the tags and properties in the bundled
 * config.yml: the "arcrot" int tag, and the properties "test" (base),
 * "charges" (printable, "[Charges: $1/$2]", value tags charges and max_charges)
 * and "spell" (rename).
 */
class ItemEditorTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of("physical", "functional", "skill");
    private static final List<String> LORE_WITHOUT_PROPERTIES = List.of("physical", "", "functional", "", "skill");
    private static final NamespacedKey ARCROT = ItemKeys.named("arcrot");

    private ItemEditor editor;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        editor = SkysongItems.getPlugin().getItemEditor();
    }

    /** An item with stored lore and, unless null, the given properties text. */
    private static ItemStack item(String properties) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData data = ItemData.from(meta);
        data.setLoreSections(SECTIONS);
        if (properties != null) {
            data.setPropertiesText(properties);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack chargedItem(int charges, int maxCharges) {
        ItemStack item = item("charges");
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges);
        meta.getPersistentDataContainer().set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, maxCharges);
        item.setItemMeta(meta);
        return item;
    }

    private static void setAddable(ItemStack item, boolean addable) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, addable);
        item.setItemMeta(meta);
    }

    private static PersistentDataContainer data(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer();
    }

    private static Integer charges(ItemStack item) {
        return data(item).get(ItemKeys.CHARGES, PersistentDataType.INTEGER);
    }

    private static Integer maxCharges(ItemStack item) {
        return data(item).get(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
    }

    private static List<String> properties(ItemStack item) {
        return ItemData.from(item.getItemMeta()).getProperties();
    }

    private static List<String> lore(ItemStack item) {
        return item.getItemMeta().lore().stream().map(ItemEditorTest::plain).toList();
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void setChargesIsCappedAtTheMax() {
        ItemStack item = chargedItem(1, 5);

        assertEquals(ItemEditor.Result.DONE, editor.setCharges(item, 9));

        assertEquals(5, charges(item));
    }

    @Test
    void setChargesOnAnUnchargedItemAlsoSetsTheMaxAndTheProperty() {
        ItemStack item = item(null);

        editor.setCharges(item, 3);

        assertEquals(3, charges(item));
        assertEquals(3, maxCharges(item));
        assertEquals(List.of("charges"), properties(item));
    }

    @Test
    void setChargesRefusesANegativeAmount() {
        ItemStack item = chargedItem(1, 5);

        assertEquals(ItemEditor.Result.INVALID_AMOUNT, editor.setCharges(item, -1));

        assertEquals(1, charges(item));
    }

    @Test
    void setChargesRedrawsTheChargesLine() {
        ItemStack item = chargedItem(1, 5);

        editor.setCharges(item, 4);

        assertEquals("[Charges: 4/5]", lore(item).getLast());
    }

    @Test
    void setMaxChargesKeepsChargesAboveTheNewMax() {
        ItemStack item = chargedItem(4, 5);

        assertEquals(ItemEditor.Result.DONE, editor.setMaxCharges(item, 2));

        assertEquals(4, charges(item));
        assertEquals(2, maxCharges(item));
    }

    @Test
    void setMaxChargesOnAnUnchargedItemAlsoSetsTheChargesAndTheProperty() {
        ItemStack item = item("test");

        editor.setMaxCharges(item, 6);

        assertEquals(6, charges(item));
        assertEquals(List.of("test", "charges"), properties(item));
    }

    @Test
    void setMaxChargesRefusesZero() {
        ItemStack item = chargedItem(1, 5);

        assertEquals(ItemEditor.Result.INVALID_AMOUNT, editor.setMaxCharges(item, 0));

        assertEquals(5, maxCharges(item));
    }

    @Test
    void addChargesIsCappedAtTheMax() {
        ItemStack item = chargedItem(4, 5);

        assertEquals(ItemEditor.Result.DONE, editor.addCharges(item, 3, false));

        assertEquals(5, charges(item));
    }

    @Test
    void takingChargesStopsAtZero() {
        ItemStack item = chargedItem(2, 5);

        editor.addCharges(item, -3, false);

        assertEquals(0, charges(item));
    }

    @Test
    void addChargesNeedsAChargedItem() {
        ItemStack item = item(null);

        assertEquals(ItemEditor.Result.NOT_CHARGED, editor.addCharges(item, 1, false));

        assertNull(charges(item));
    }

    @Test
    void addingToALockedItemIsRefused() {
        ItemStack item = chargedItem(1, 5);
        setAddable(item, false);

        assertEquals(ItemEditor.Result.ADDING_LOCKED, editor.addCharges(item, 1, false));

        assertEquals(1, charges(item));
    }

    @Test
    void addingToALockedItemWorksWhenIgnoringTheLock() {
        ItemStack item = chargedItem(1, 5);
        setAddable(item, false);

        assertEquals(ItemEditor.Result.DONE, editor.addCharges(item, 1, true));

        assertEquals(2, charges(item));
    }

    @Test
    void takingChargesFromALockedItemIsAllowed() {
        ItemStack item = chargedItem(3, 5);
        setAddable(item, false);

        assertEquals(ItemEditor.Result.DONE, editor.addCharges(item, -1, false));

        assertEquals(2, charges(item));
    }

    @Test
    void setChargesLeftIsNotCappedAndRedrawsTheLore() {
        ItemStack item = chargedItem(3, 5);

        editor.setChargesLeft(item, 7);

        assertEquals(7, charges(item));
        assertEquals("[Charges: 7/5]", lore(item).getLast());
    }

    @Test
    void removeChargesClearsTheValuesThePropertyAndItsLine() {
        ItemStack item = chargedItem(3, 5);

        editor.removeCharges(item);

        assertNull(charges(item));
        assertNull(maxCharges(item));
        assertFalse(data(item).has(ItemKeys.PROPERTIES));
        assertEquals(LORE_WITHOUT_PROPERTIES, lore(item));
    }

    @Test
    void toggleChargeAddingTurnsAnUnsetItemOn() {
        ItemStack item = chargedItem(1, 5);

        assertEquals(ItemEditor.Result.DONE, editor.toggleChargeAdding(item));

        assertTrue(editor.canAddCharges(item));
    }

    @Test
    void toggleChargeAddingTurnsAnEnabledItemOff() {
        ItemStack item = chargedItem(1, 5);
        setAddable(item, true);

        editor.toggleChargeAdding(item);

        assertFalse(editor.canAddCharges(item));
    }

    @Test
    void toggleChargeAddingNeedsAChargedItem() {
        assertEquals(ItemEditor.Result.NOT_CHARGED, editor.toggleChargeAdding(item(null)));
    }

    @Test
    void setTagParsesTheValueAsTheTagType() {
        ItemStack item = item(null);

        assertEquals(ItemEditor.Result.DONE, editor.setTag(item, "ARCROT", "12"));

        assertEquals(12, data(item).get(ARCROT, PersistentDataType.INTEGER));
    }

    @Test
    void setTagRefusesAValueOfTheWrongType() {
        ItemStack item = item(null);

        assertEquals(ItemEditor.Result.NOT_A_NUMBER, editor.setTag(item, "arcrot", "lots"));

        assertFalse(data(item).has(ARCROT));
    }

    @Test
    void setTagRefusesSomethingOtherThanTrueOrFalseForABooleanTag() {
        ItemStack item = item(null);

        assertEquals(ItemEditor.Result.NOT_A_BOOLEAN, editor.setTag(item, "can_add_charges", "yes"));
    }

    @Test
    void setTagStoresABooleanTag() {
        ItemStack item = item(null);

        editor.setTag(item, "can_add_charges", "TRUE");

        assertTrue(editor.canAddCharges(item));
    }

    @Test
    void setTagRefusesAnUnknownTag() {
        assertEquals(ItemEditor.Result.UNKNOWN_TAG, editor.setTag(item(null), "nope", "1"));
    }

    @Test
    void removeTagRemovesIt() {
        ItemStack item = item(null);
        editor.setTag(item, "arcrot", "3");

        assertEquals(ItemEditor.Result.DONE, editor.removeTag(item, "arcrot"));

        assertFalse(data(item).has(ARCROT));
    }

    @Test
    void removeTagReportsAMissingTag() {
        assertEquals(ItemEditor.Result.MISSING, editor.removeTag(item(null), "arcrot"));
    }

    @Test
    void addPropertyGivesAPrintablePropertyEmptyValuesAndDrawsIt() {
        ItemStack item = item("test");

        assertEquals(ItemEditor.Result.DONE, editor.addProperty(item, "charges"));

        assertEquals(List.of("test", "charges"), properties(item));
        assertEquals(0, charges(item));
        assertEquals(0, maxCharges(item));
        assertEquals("[Charges: 0/0]", lore(item).getLast());
    }

    @Test
    void addPropertyRefusesAPropertyTheItemHas() {
        assertEquals(ItemEditor.Result.ALREADY_PRESENT, editor.addProperty(item("test"), "test"));
    }

    @Test
    void addPropertyRefusesAnUnknownProperty() {
        ItemStack item = item(null);

        assertEquals(ItemEditor.Result.UNKNOWN_PROPERTY, editor.addProperty(item, "nope"));

        assertFalse(data(item).has(ItemKeys.PROPERTIES));
    }

    @Test
    void removePropertyKeepsTheOthers() {
        ItemStack item = item("test;spell");

        assertEquals(ItemEditor.Result.DONE, editor.removeProperty(item, "test"));

        assertEquals(List.of("spell"), properties(item));
    }

    @Test
    void removingTheLastPropertyRedrawsTheLoreWithoutProperties() {
        ItemStack item = chargedItem(3, 5);

        editor.removeProperty(item, "charges");

        assertFalse(data(item).has(ItemKeys.PROPERTIES));
        assertEquals(LORE_WITHOUT_PROPERTIES, lore(item));
    }

    @Test
    void removePropertyReportsAnItemWithoutProperties() {
        assertEquals(ItemEditor.Result.NO_PROPERTIES, editor.removeProperty(item(null), "test"));
    }

    @Test
    void removePropertyReportsAMissingProperty() {
        assertEquals(ItemEditor.Result.MISSING, editor.removeProperty(item("test"), "spell"));
    }

    @Test
    void anItemWithoutStoredLoreIsEditedButNotRedrawn() {
        ItemStack item = new ItemStack(Material.STONE);

        editor.setCharges(item, 2);

        assertEquals(2, charges(item));
        assertFalse(item.getItemMeta().hasLore());
    }
}
