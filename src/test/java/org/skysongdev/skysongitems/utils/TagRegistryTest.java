package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterisation tests for the item tags: the built-in ones every item can carry
 * and the ones configured in the {@code tags} section of config.yml. The bundled
 * config.yml defines arcrot, charged_arcrot and fictis, all of type int.
 */
class TagRegistryTest extends MockBukkitTest {
    private SkysongItems plugin;

    @BeforeEach
    void loadPlugin() {
        plugin = MockBukkit.load(SkysongItems.class);
    }

    private void reloadTags() {
        plugin.getTagRegistry().load(plugin.getConfig());
    }

    private NamespacedKey tag(String id) {
        return plugin.getTagRegistry().getKey(id);
    }

    private boolean isTag(String id) {
        return plugin.getTagRegistry().contains(id);
    }

    private PersistentDataType<?, ?> typeOf(NamespacedKey key) {
        return plugin.getTagRegistry().getType(key);
    }

    private boolean isRegistered(NamespacedKey key) {
        return plugin.getTagRegistry().contains(key);
    }

    private List<NamespacedKey> tagsIn(ItemStack item) {
        return plugin.getTagRegistry().getTagsIn(item);
    }

    private void configureOnlyTag(String id, String type) {
        FileConfiguration config = plugin.getConfig();
        config.createSection("tags");
        config.set("tags." + id + ".namespace", id);
        config.set("tags." + id + ".type", type);
    }

    @Test
    void configuredTagsAreRegisteredInThePluginNamespace() {
        assertEquals(ItemKeys.named("arcrot"), tag("arcrot"));
        assertEquals(PersistentDataType.INTEGER, typeOf(ItemKeys.named("charged_arcrot")));
        assertTrue(isTag("fictis"));
    }

    @Test
    void builtInTagsAreRegisteredWithTheirTypes() {
        assertEquals(PersistentDataType.INTEGER, typeOf(ItemKeys.CHARGES));
        assertEquals(PersistentDataType.INTEGER, typeOf(ItemKeys.MAX_CHARGES));
        assertEquals(PersistentDataType.BOOLEAN, typeOf(ItemKeys.CAN_ADD_CHARGES));
        assertEquals(PersistentDataType.STRING, typeOf(ItemKeys.VERSION));
        assertEquals(PersistentDataType.STRING, typeOf(ItemKeys.PERMISSION));
        assertEquals(PersistentDataType.STRING, typeOf(ItemKeys.FURNITURE_MODEL));
    }

    @Test
    void itemDataKeysAreNotTags() {
        assertFalse(isRegistered(ItemKeys.PROPERTIES));
        assertFalse(isRegistered(ItemKeys.DELETED_LINES));
    }

    @Test
    void lookupMatchesTheKeyIgnoringCaseAndNamespace() {
        assertEquals(ItemKeys.named("arcrot"), tag("ARCROT"));
        assertEquals(ItemKeys.FURNITURE_MODEL, tag("model"));
    }

    @Test
    void unknownIdIsNotATag() {
        assertFalse(isTag("missing"));
        assertNull(tag("missing"));
    }

    @Test
    void eachConfiguredTypeNameMapsToItsDataType() {
        configureOnlyTag("text", "String");
        plugin.getConfig().set("tags.decimal.namespace", "decimal");
        plugin.getConfig().set("tags.decimal.type", "float");
        plugin.getConfig().set("tags.flag.namespace", "flag");
        plugin.getConfig().set("tags.flag.type", "bool");

        reloadTags();

        assertEquals(PersistentDataType.STRING, typeOf(tag("text")));
        assertEquals(PersistentDataType.FLOAT, typeOf(tag("decimal")));
        assertEquals(PersistentDataType.BOOLEAN, typeOf(tag("flag")));
    }

    @Test
    void unknownTypeNameRegistersTheTagWithoutAType() {
        configureOnlyTag("odd", "list");

        reloadTags();

        assertTrue(isTag("odd"));
        assertNull(typeOf(tag("odd")));
    }

    @Test
    void reloadReplacesConfiguredTagsAndKeepsBuiltInOnes() {
        configureOnlyTag("fresh", "int");

        reloadTags();

        assertTrue(isTag("fresh"));
        assertFalse(isTag("arcrot"));
        assertTrue(isRegistered(ItemKeys.CHARGES));
    }

    @Test
    void tagsInAnItemAreOnlyTheRegisteredOnes() {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.named("arcrot"), PersistentDataType.INTEGER, 2);
        meta.getPersistentDataContainer().set(ItemKeys.named("not_a_tag"), PersistentDataType.INTEGER, 2);
        item.setItemMeta(meta);

        assertEquals(List.of(ItemKeys.named("arcrot")), tagsIn(item));
    }

    @Test
    void tagsInNoItemAreEmpty() {
        assertTrue(tagsIn(null).isEmpty());
        assertTrue(tagsIn(new ItemStack(Material.AIR)).isEmpty());
    }
}
