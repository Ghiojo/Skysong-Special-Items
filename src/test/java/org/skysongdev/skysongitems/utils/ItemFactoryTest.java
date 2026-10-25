package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MockBukkit cannot set Paper data components, so {@link ItemFactory#create} itself
 * cannot run here. These tests cover everything it writes into the item meta.
 */
class ItemFactoryTest extends MockBukkitTest {
    private static final NamespacedKey WEIGHT = ItemKeys.named("weight");

    @BeforeEach
    void loadPluginWithAFloatTag() {
        SkysongItems plugin = MockBukkit.load(SkysongItems.class);
        FileConfiguration config = plugin.getConfig();
        config.set("tags.weight.namespace", "weight");
        config.set("tags.weight.type", "float");
        plugin.getTagRegistry().load(config);
    }

    private static SkysongItem template(String deleted, String properties, Map<NamespacedKey, String> tagValues) {
        return new SkysongItem("Sword", new ArrayList<>(List.of("physical", "functional", "skill")),
                "IRON_SWORD", null, deleted, properties, tagValues);
    }

    private static ItemMeta applied(SkysongItem template) {
        ItemMeta meta = new ItemStack(Material.IRON_SWORD).getItemMeta();
        ItemFactory.applyTemplate(meta, template);
        return meta;
    }

    private static List<String> plain(List<Component> lines) {
        return lines.stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
    }

    @Test
    void materialNameIsMatchedIgnoringCase() {
        assertEquals(Material.DIAMOND_SWORD, ItemFactory.materialOf("diamond_sword"));
    }

    @Test
    void unknownMaterialBecomesABarrier() {
        assertEquals(Material.BARRIER, ItemFactory.materialOf("not_a_material"));
    }

    @Test
    void templateNameIsShownAndStored() {
        ItemMeta meta = applied(template("", "", Map.of()));

        assertEquals("Sword", PlainTextComponentSerializer.plainText().serialize(meta.displayName()));
        assertEquals("Sword", meta.getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING));
    }

    @Test
    void loreSectionsAreStoredAndShown() {
        ItemMeta meta = applied(template("", "", Map.of()));

        assertEquals(List.of("physical", "functional", "skill"), ItemData.from(meta).getLoreSections());
        assertEquals(List.of("physical", "", "functional", "", "skill"), plain(meta.lore()));
    }

    @Test
    void deletedSectionsAreStoredAndHidden() {
        ItemMeta meta = applied(template("010", "", Map.of()));

        assertEquals("010", ItemData.from(meta).getDeletedLines());
        assertEquals(List.of("physical", "", "skill"), plain(meta.lore()));
    }

    @Test
    void emptyDeletedLinesAndPropertiesAreNotStored() {
        ItemMeta meta = applied(template("", "", Map.of()));

        assertFalse(ItemData.from(meta).hasDeletedLines());
        assertFalse(ItemData.from(meta).hasProperties());
    }

    @Test
    void propertiesAreStored() {
        ItemMeta meta = applied(template("", "charges;spell", Map.of()));

        assertEquals(List.of("charges", "spell"), ItemData.from(meta).getProperties());
    }

    @Test
    void itemIsStampedWithTheCurrentVersion() {
        ItemMeta meta = applied(template("", "", Map.of()));

        assertEquals(Constants.ITEM_VERSION, meta.getPersistentDataContainer().get(ItemKeys.VERSION, PersistentDataType.STRING));
    }

    @Test
    void tagValuesAreStoredAsTheirConfiguredType() {
        Map<NamespacedKey, String> tagValues = new LinkedHashMap<>();
        tagValues.put(ItemKeys.named("arcrot"), "2");
        tagValues.put(ItemKeys.CAN_ADD_CHARGES, "true");
        tagValues.put(ItemKeys.PERMISSION, "skysongitems.test");
        tagValues.put(WEIGHT, "1.5");

        PersistentDataContainer container = applied(template("", "", tagValues)).getPersistentDataContainer();

        assertEquals(2, container.get(ItemKeys.named("arcrot"), PersistentDataType.INTEGER));
        assertEquals(Boolean.TRUE, container.get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN));
        assertEquals("skysongitems.test", container.get(ItemKeys.PERMISSION, PersistentDataType.STRING));
        assertEquals(1.5f, container.get(WEIGHT, PersistentDataType.FLOAT));
    }

    @Test
    void tagsMissingFromTheConfigAreSkipped() {
        NamespacedKey retired = ItemKeys.named("retired_tag");

        ItemMeta meta = applied(template("", "", Map.of(retired, "1")));

        assertFalse(meta.getPersistentDataContainer().has(retired));
    }

    @Test
    void itemWithTheVersionTagIsASkysongItem() {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        item.setItemMeta(meta);

        assertTrue(ItemFactory.isSkysongItem(item));
    }

    @Test
    void plainItemIsNotASkysongItem() {
        assertFalse(ItemFactory.isSkysongItem(new ItemStack(Material.STONE)));
    }

    @Test
    void missingItemIsNotASkysongItem() {
        assertFalse(ItemFactory.isSkysongItem(null));
        assertFalse(ItemFactory.isSkysongItem(new ItemStack(Material.AIR)));
    }
}
