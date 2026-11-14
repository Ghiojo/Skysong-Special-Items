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
 * Characterisation tests for the traits loaded from the {@code properties}
 * section of config.yml. The bundled config.yml defines test, charges and spell.
 */
class TraitRegistryTest extends MockBukkitTest {
    private SkysongItems plugin;

    @BeforeEach
    void loadPlugin() {
        plugin = MockBukkit.load(SkysongItems.class);
    }

    private void reloadTraits() {
        plugin.getTraitRegistry().load(plugin.getConfig(), plugin.getLogger());
    }

    private Trait trait(String id) {
        return plugin.getTraitRegistry().get(id);
    }

    private boolean isTrait(String id) {
        return plugin.getTraitRegistry().contains(id);
    }

    private List<Trait> allTraits() {
        return plugin.getTraitRegistry().getAll();
    }

    private List<String> traitTypes() {
        return plugin.getTraitRegistry().getTraitTypes();
    }

    /** Replaces the traits section with a single trait of the given type. */
    private void configureOnlyTrait(String id, String type, String propType) {
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
    void bundledTraitsAreLoadedInConfigOrder() {
        assertEquals(List.of("test", "charges", "spell"), allTraits().stream().map(Trait::getId).toList());
    }

    @Test
    void baseTypeLoadsAPlainTrait() {
        Trait test = trait("test");

        assertSame(Trait.class, test.getClass());
        assertEquals("Weapon", test.getPropType());
        assertEquals("Test", test.getName());
        assertEquals("Description", test.getDescription());
    }

    @Test
    void printableTypeLoadsItsPrintingValueTagsAndWeight() {
        PrintableTrait charges = assertInstanceOf(PrintableTrait.class, trait("charges"));

        assertEquals(List.of("charges", "max_charges"), charges.getValuetags());
        assertEquals(0, charges.getWeight());
        assertEquals("<#9C7D2C>[<#E1BD32>Charges: <#E1BD32>2/5<#9C7D2C>]", charges.getParsedPrinting(chargedItem(2, 5)));
    }

    @Test
    void renameTypeLoadsItsNameTag() {
        RenameTrait spell = assertInstanceOf(RenameTrait.class, trait("spell"));

        assertEquals("<gray>[<gold>Spell<gray>]", spell.getTag());
    }

    @Test
    void breweryPrintableTypeLoadsABreweryPrintableTrait() {
        configureOnlyTrait("quality", "BreweryPrintable", "Brew");
        plugin.getConfig().set("properties.quality.printable", "Quality: ;!");
        plugin.getConfig().set("properties.quality.weight", 3);

        reloadTraits();

        BreweryPrintableTrait quality = assertInstanceOf(BreweryPrintableTrait.class, trait("quality"));
        assertEquals(3, quality.getWeight());
    }

    @Test
    void unknownTypeLoadsAPlainTrait() {
        configureOnlyTrait("odd", "not_a_type", "Misc");

        reloadTraits();

        assertSame(Trait.class, trait("odd").getClass());
    }

    @Test
    void lookupIgnoresCase() {
        assertTrue(isTrait("CHARGES"));
        assertEquals("spell", trait("Spell").getId());
    }

    @Test
    void unknownIdIsNotATrait() {
        assertFalse(isTrait("missing"));
        assertNull(trait("missing"));
    }

    @Test
    void eachTraitTypeIsListedOnce() {
        assertTrue(traitTypes().containsAll(List.of("Weapon", "Magic")));
        assertEquals(1, traitTypes().stream().filter("Magic"::equals).count());
    }

    @Test
    void reloadReplacesTheTraits() {
        configureOnlyTrait("fresh", "base", "Weapon");

        reloadTraits();

        assertEquals(List.of("fresh"), allTraits().stream().map(Trait::getId).toList());
        assertFalse(isTrait("charges"));
    }

    @Test
    void reloadForgetsTraitTypesRemovedFromTheConfig() {
        // Reproduces the registry quirk: Weapon and Magic used to stay listed until restart.
        configureOnlyTrait("fresh", "base", "Tool");

        reloadTraits();

        assertEquals(List.of("Tool"), traitTypes());
    }

    @Test
    void reloadWithAnEmptyTraitsSectionLeavesNoTraitTypes() {
        plugin.getConfig().createSection("properties");

        reloadTraits();

        assertTrue(allTraits().isEmpty());
        assertTrue(traitTypes().isEmpty());
    }
}
