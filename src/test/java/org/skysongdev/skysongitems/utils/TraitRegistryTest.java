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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterisation tests for the traits loaded from the {@code traits}
 * section of config.yml. The bundled config.yml defines test, charges, item_hp and spell.
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
        config.createSection("traits");
        config.set("traits." + id + ".type", type);
        config.set("traits." + id + ".traittype", propType);
        config.set("traits." + id + ".item", "STONE");
        config.set("traits." + id + ".name", "Name of " + id);
        config.set("traits." + id + ".description", "Description of " + id);
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
        assertEquals(List.of("test", "charges", "item_hp", "spell"), allTraits().stream().map(Trait::getId).toList());
    }

    @Test
    void aConfigWithoutTheItemHpTraitGetsTheBundledOne() {
        plugin.getConfig().set("traits.item_hp", null);

        reloadTraits();

        PrintableTrait itemHp = assertInstanceOf(PrintableTrait.class, trait("item_hp"));
        assertEquals(List.of("item_hp", "max_item_hp"), itemHp.getValuetags());
        assertEquals("item_hp", allTraits().getLast().getId());
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
    void aConfiguredTraitCanBeMarkedToStayOnTransform() {
        configureOnlyTrait("cursed", "base", "Magic");
        plugin.getConfig().set("traits.cursed.keep_on_transform", true);

        reloadTraits();

        assertTrue(trait("cursed").isKeptOnTransform());
        assertFalse(trait(ItemEditor.ITEM_HP_TRAIT).isKeptOnTransform(), "traits are dropped unless marked");
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
        plugin.getConfig().set("traits.quality.printable", "Quality: ;!");
        plugin.getConfig().set("traits.quality.weight", 3);

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

        assertEquals(List.of("fresh", "item_hp"), allTraits().stream().map(Trait::getId).toList());
        assertFalse(isTrait("charges"));
    }

    @Test
    void reloadForgetsTraitTypesRemovedFromTheConfig() {
        // Reproduces the registry quirk: Weapon and Magic used to stay listed until restart.
        configureOnlyTrait("fresh", "base", "Tool");

        reloadTraits();

        // Magic stays because the built-in item_hp trait uses it.
        assertEquals(List.of("Tool", "Magic"), traitTypes());
    }

    @Test
    void reloadWithAnEmptyTraitsSectionLeavesOnlyTheBuiltInTraitType() {
        plugin.getConfig().createSection("traits");

        reloadTraits();

        assertEquals(List.of("item_hp"), allTraits().stream().map(Trait::getId).toList());
        assertEquals(List.of("Magic"), traitTypes());
    }

    /** Loads the traits with a logger that keeps the warnings it gets, and returns them. */
    private List<String> reloadTraitsCollectingWarnings() {
        List<String> warnings = new ArrayList<>();
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                if (Level.WARNING.equals(logRecord.getLevel())) {
                    warnings.add(logRecord.getMessage());
                }
            }

            @Override
            public void flush() {
                // Nothing buffered.
            }

            @Override
            public void close() {
                // Nothing to release.
            }
        });
        plugin.getTraitRegistry().load(plugin.getConfig(), logger);
        return warnings;
    }

    /** Replaces the config's traits with an old-style properties section holding one trait. */
    private void configureLegacySection(String id) {
        FileConfiguration config = plugin.getConfig();
        config.set("traits", null);
        config.set("properties." + id + ".type", "base");
        config.set("properties." + id + ".proptype", "Old");
        config.set("properties." + id + ".item", "STONE");
    }

    private static boolean anyMentions(List<String> messages, String text) {
        return messages.stream().anyMatch(message -> message.contains(text));
    }

    @Test
    void oldPropertiesSectionStillLoadsWithAWarning() {
        configureLegacySection("legacy");

        List<String> warnings = reloadTraitsCollectingWarnings();

        assertEquals(List.of("legacy", "item_hp"), allTraits().stream().map(Trait::getId).toList());
        assertEquals("Old", trait("legacy").getPropType());
        assertTrue(anyMentions(warnings, "old 'properties' section"));
    }

    @Test
    void traitsSectionWinsOverAnOldPropertiesSection() {
        configureOnlyTrait("fresh", "base", "Weapon");
        plugin.getConfig().set("properties.legacy.type", "base");

        List<String> warnings = reloadTraitsCollectingWarnings();

        assertEquals(List.of("fresh", "item_hp"), allTraits().stream().map(Trait::getId).toList());
        assertTrue(anyMentions(warnings, "only 'traits' is loaded"));
    }

    @Test
    void bundledConfigLogsNoRenameWarnings() {
        List<String> warnings = reloadTraitsCollectingWarnings();

        assertFalse(anyMentions(warnings, "rename"));
        assertFalse(anyMentions(warnings, "only 'traits' is loaded"));
    }

    @Test
    void oldProptypeKeyStillSetsTheTraitTypeWithAWarning() {
        configureOnlyTrait("fresh", "base", "Weapon");
        plugin.getConfig().set("traits.fresh.traittype", null);
        plugin.getConfig().set("traits.fresh.proptype", "Tool");

        List<String> warnings = reloadTraitsCollectingWarnings();

        assertEquals("Tool", trait("fresh").getPropType());
        assertTrue(anyMentions(warnings, "old 'proptype' key; rename it to 'traittype': fresh"));
    }

    @Test
    void traittypeWinsOverAnOldProptypeKey() {
        configureOnlyTrait("fresh", "base", "Weapon");
        plugin.getConfig().set("traits.fresh.proptype", "Tool");

        reloadTraits();

        assertEquals("Weapon", trait("fresh").getPropType());
    }
}
