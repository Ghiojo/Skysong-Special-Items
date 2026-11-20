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
 * Uses the traits configured in the bundled config.yml: "test" (base),
 * "charges" (printable, "[Charges: $1/$2]") and "spell" (rename, "[Spell]").
 */
class ItemLoreRendererTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of("physical", "functional", "skill");

    private SkysongItems plugin;

    @BeforeEach
    void loadPlugin() {
        plugin = MockBukkit.load(SkysongItems.class);
    }

    private void addPrintableTrait(String id, int weight) {
        FileConfiguration config = plugin.getConfig();
        config.set("traits." + id + ".type", "printable");
        config.set("traits." + id + ".traittype", "Magic");
        config.set("traits." + id + ".item", "STONE");
        config.set("traits." + id + ".printable", id);
        config.set("traits." + id + ".weight", weight);
        plugin.getTraitRegistry().load(config, plugin.getLogger());
    }

    private static ItemStack itemWithTraits(String traits) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        data.setLoreSections(SECTIONS);
        data.setTraitsText(traits);
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
    void renderSectionsDrawsTheGivenSectionsLeavingOutTheHiddenOnes() {
        ItemStack item = itemWithTraits("charges");
        ItemMeta meta = item.getItemMeta();
        ItemData.from(meta).setDeletedLines("010");

        ItemLoreRenderer.renderSections(meta, List.of("new physical", "functional", "new skill"));
        item.setItemMeta(meta);

        assertEquals(List.of("new physical", "", "new skill"), plainLore(item), "no trait lines either");
    }

    @Test
    void missingItemReturnsNull() {
        assertNull(ItemLoreRenderer.refresh(null));
        assertNull(ItemLoreRenderer.refresh(new ItemStack(Material.AIR)));
    }

    @Test
    void itemWithoutTraitsKeepsItsLoreAndName() {
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
    void printableTraitsFollowTheLoreAfterABlankLine() {
        ItemStack refreshed = ItemLoreRenderer.refresh(itemWithTraits("charges"));

        assertEquals(List.of("physical", "", "functional", "", "skill", "", "[Charges: 3/5]"), plainLore(refreshed));
    }

    @Test
    void renderingWithoutTraitsDropsTraitLinesAndTags() {
        ItemStack item = ItemLoreRenderer.refresh(itemWithTraits("charges;spell"));
        ItemMeta meta = item.getItemMeta();

        ItemLoreRenderer.renderWithoutTraits(meta);
        item.setItemMeta(meta);

        assertEquals(List.of("physical", "", "functional", "", "skill"), plainLore(item));
        assertEquals("Sword", plain(item.getItemMeta().displayName()));
    }

    @Test
    void storedNameReplacesTheDisplayName() {
        ItemStack refreshed = ItemLoreRenderer.refresh(itemWithTraits("test"));

        assertEquals("Sword", plain(refreshed.getItemMeta().displayName()));
    }

    @Test
    void renameTraitPrefixesTheStoredName() {
        ItemStack refreshed = ItemLoreRenderer.refresh(itemWithTraits("spell"));

        assertEquals("[Spell] Sword", plain(refreshed.getItemMeta().displayName()));
    }

    @Test
    void refreshUpdatesTheGivenItem() {
        ItemStack item = itemWithTraits("charges");

        ItemStack refreshed = ItemLoreRenderer.refresh(item);

        assertSame(item, refreshed);
    }

    @Test
    void printableTraitsAreOrderedHeaviestFirstKeepingTiesInOrder() {
        addPrintableTrait("light", 1);
        addPrintableTrait("heavy", 5);
        addPrintableTrait("also_heavy", 5);

        List<PrintableTrait> ordered = ItemLoreRenderer.printableTraitsByWeight(
                List.of("light", "heavy", "test", "spell", "also_heavy", "unknown"));

        assertEquals(List.of("heavy", "also_heavy", "light"), ordered.stream().map(Trait::getId).toList());
    }
}
