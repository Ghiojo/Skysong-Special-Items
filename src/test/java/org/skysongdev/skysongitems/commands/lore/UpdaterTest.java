package org.skysongdev.skysongitems.commands.lore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the migration of items made before item versions existed, which
 * rebuilds the stored lore sections from the item's visible lore.
 */
class UpdaterTest extends MockBukkitTest {
    private final Updater updater = new Updater();

    private static ItemStack legacyItem(String deletedLines, Component... lore) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(lore));
        if (deletedLines != null) {
            ItemData.from(meta).setDeletedLines(deletedLines);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> storedSections(ItemStack item) {
        return ItemData.from(item.getItemMeta()).getLoreSections();
    }

    @Test
    void storesEachLoreSectionInItsOwnSlot() {
        ItemStack item = legacyItem(null, Component.text("Hello", NamedTextColor.GRAY));

        ItemStack updated = updater.UpdateZero(item);

        assertEquals(List.of("<gray>Hello", "<#424242>[Not Set]", "<#81a871>[Not Set]"), storedSections(updated));
    }

    @Test
    void keepsASectionWithTheSameTextAsTheFirst() {
        // The physical section is hidden, so it keeps its "<gray>[Not Set]" placeholder,
        // and the visible lore reads back as a functional section with that same text.
        ItemStack item = legacyItem("100", Component.text("[Not Set]", NamedTextColor.GRAY));

        ItemStack updated = updater.UpdateZero(item);

        assertEquals(List.of("<gray>[Not Set]", "<gray>[Not Set]", "<#81a871>[Not Set]"), storedSections(updated));
    }

    @Test
    void marksTheItemAsVersionOnePointZero() {
        ItemStack item = legacyItem(null, Component.text("Hello", NamedTextColor.GRAY));

        ItemStack updated = updater.UpdateZero(item);

        assertEquals("1.0", updated.getItemMeta().getPersistentDataContainer().get(ItemKeys.VERSION, PersistentDataType.STRING));
    }
}
