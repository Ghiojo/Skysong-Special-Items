package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterisation tests for the properties loaded from the {@code properties}
 * section of config.yml. The bundled config.yml defines test, charges and spell.
 */
class PropertyRegistryTest extends MockBukkitTest {
    private SkysongItems plugin;

    @BeforeEach
    void loadPlugin() {
        plugin = MockBukkit.load(SkysongItems.class);
    }

    private void reloadProperties() {
        plugin.getUtils().setupProperties();
    }

    private static Property property(String id) {
        return Utils.getProperty(id);
    }

    private static boolean isProperty(String id) {
        return Utils.isAProperty(id);
    }

    private static List<Property> allProperties() {
        return Constants.properties;
    }

    private static List<String> propertyTypes() {
        return Constants.propertyTypes;
    }

    /** Replaces the properties section with a single property of the given type. */
    private void configureOnlyProperty(String id, String type, String propType) {
        FileConfiguration config = plugin.getConfig();
        config.createSection("properties");
        config.set("properties." + id + ".type", type);
        config.set("properties." + id + ".proptype", propType);
        config.set("properties." + id + ".item", "STONE");
        config.set("properties." + id + ".name", "Name of " + id);
        config.set("properties." + id + ".description", "Description of " + id);
    }

    private static ItemStack chargedItem(int charges, int maxCharges) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges);
        meta.getPersistentDataContainer().set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, maxCharges);
        item.setItemMeta(meta);
        return item;
    }

    @Test
    void bundledPropertiesAreLoadedInConfigOrder() {
        assertEquals(List.of("test", "charges", "spell"), allProperties().stream().map(Property::getId).toList());
    }

    @Test
    void baseTypeLoadsAPlainProperty() {
        Property test = property("test");

        assertSame(Property.class, test.getClass());
        assertEquals("Weapon", test.getPropType());
        assertEquals("Test", test.getName());
        assertEquals("Description", test.getDescription());
    }

    @Test
    void printableTypeLoadsItsPrintingValueTagsAndWeight() {
        PrintableProperty charges = assertInstanceOf(PrintableProperty.class, property("charges"));

        assertEquals(List.of("charges", "max_charges"), charges.getValuetags());
        assertEquals(0, charges.getWeight());
        assertEquals("<#9C7D2C>[<#E1BD32>Charges: <#E1BD32>2/5<#9C7D2C>]", charges.getParsedPrinting(chargedItem(2, 5)));
    }

    @Test
    void renameTypeLoadsItsNameTag() {
        RenameProperty spell = assertInstanceOf(RenameProperty.class, property("spell"));

        assertEquals("<gray>[<gold>Spell<gray>]", spell.getTag());
    }

    @Test
    void breweryPrintableTypeLoadsABreweryPrintableProperty() {
        configureOnlyProperty("quality", "BreweryPrintable", "Brew");
        plugin.getConfig().set("properties.quality.printable", "Quality: ;!");
        plugin.getConfig().set("properties.quality.weight", 3);

        reloadProperties();

        BreweryPrintableProperty quality = assertInstanceOf(BreweryPrintableProperty.class, property("quality"));
        assertEquals(3, quality.getWeight());
    }

    @Test
    void unknownTypeLoadsAPlainProperty() {
        configureOnlyProperty("odd", "not_a_type", "Misc");

        reloadProperties();

        assertSame(Property.class, property("odd").getClass());
    }

    @Test
    void lookupIgnoresCase() {
        assertTrue(isProperty("CHARGES"));
        assertEquals("spell", property("Spell").getId());
    }

    @Test
    void unknownIdIsNotAProperty() {
        assertFalse(isProperty("missing"));
        assertNull(property("missing"));
    }

    @Test
    void eachPropertyTypeIsListedOnce() {
        assertTrue(propertyTypes().containsAll(List.of("Weapon", "Magic")));
        assertEquals(1, propertyTypes().stream().filter("Magic"::equals).count());
    }

    @Test
    void reloadReplacesTheProperties() {
        configureOnlyProperty("fresh", "base", "Weapon");

        reloadProperties();

        assertEquals(List.of("fresh"), allProperties().stream().map(Property::getId).toList());
        assertFalse(isProperty("charges"));
    }

    @Test
    void reloadKeepsPropertyTypesFromEarlierLoads() {
        configureOnlyProperty("fresh", "base", "Tool");

        reloadProperties();

        assertTrue(propertyTypes().containsAll(List.of("Weapon", "Magic", "Tool")));
    }
}
