package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Uses the properties configured in the bundled config.yml: "test" (base),
 * "charges" (printable, "[Charges: $1/$2]") and "spell" (rename, "[Spell]").
 */
class ItemLoreRendererTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of("physical", "functional", "skill");

    private SkysongItems plugin;

    @BeforeEach
    void loadPlugin() {
        plugin = MockBukkit.load(SkysongItems.class);
    }

    private void addPrintableProperty(String id, int weight) {
        FileConfiguration config = plugin.getConfig();
        config.set("properties." + id + ".type", "printable");
        config.set("properties." + id + ".proptype", "Magic");
        config.set("properties." + id + ".item", "STONE");
        config.set("properties." + id + ".printable", id);
        config.set("properties." + id + ".weight", weight);
        plugin.getPropertyRegistry().load(config, plugin.getLogger());
    }

    private static ItemStack itemWithProperties(String properties) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        data.setLoreSections(SECTIONS);
        data.setPropertiesText(properties);
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, "Sword");
        meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 3);
        meta.getPersistentDataContainer().set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, 5);
        meta.displayName(Component.text("Old name"));
        item.setItemMeta(meta);
        return item;
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static List<String> plainLore(ItemStack item) {
        return item.getItemMeta().lore().stream().map(ItemLoreRendererTest::plain).toList();
    }

    @Test
    void missingItemReturnsNull() {
        assertNull(ItemLoreRenderer.refresh(null));
        assertNull(ItemLoreRenderer.refresh(new ItemStack(Material.AIR)));
    }

    @Test
    void itemWithoutPropertiesKeepsItsLoreAndName() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        ItemData.from(meta).setLoreSections(SECTIONS);
        meta.lore(List.of(Component.text("Old lore")));
        meta.displayName(Component.text("Old name"));
        item.setItemMeta(meta);

        ItemStack refreshed = ItemLoreRenderer.refresh(item);

        assertEquals(List.of("Old lore"), plainLore(refreshed));
        assertEquals("Old name", plain(refreshed.getItemMeta().displayName()));
    }

    @Test
    void printablePropertiesFollowTheLoreAfterABlankLine() {
        ItemStack refreshed = ItemLoreRenderer.refresh(itemWithProperties("charges"));

        assertEquals(List.of("physical", "", "functional", "", "skill", "", "[Charges: 3/5]"), plainLore(refreshed));
    }

    @Test
    void storedNameReplacesTheDisplayName() {
        ItemStack refreshed = ItemLoreRenderer.refresh(itemWithProperties("test"));

        assertEquals("Sword", plain(refreshed.getItemMeta().displayName()));
    }

    @Test
    void renamePropertyPrefixesTheStoredName() {
        ItemStack refreshed = ItemLoreRenderer.refresh(itemWithProperties("spell"));

        assertEquals("[Spell] Sword", plain(refreshed.getItemMeta().displayName()));
    }

    @Test
    void refreshUpdatesTheGivenItem() {
        ItemStack item = itemWithProperties("charges");

        ItemStack refreshed = ItemLoreRenderer.refresh(item);

        assertSame(item, refreshed);
    }

    @Test
    void printablePropertiesAreOrderedHeaviestFirstKeepingTiesInOrder() {
        addPrintableProperty("light", 1);
        addPrintableProperty("heavy", 5);
        addPrintableProperty("also_heavy", 5);

        List<PrintableProperty> ordered = ItemLoreRenderer.printablePropertiesByWeight(
                List.of("light", "heavy", "test", "spell", "also_heavy", "unknown"));

        assertEquals(List.of("heavy", "also_heavy", "light"), ordered.stream().map(Property::getId).toList());
    }
}
