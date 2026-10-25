package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * Rebuilds an item's lore and display name from the data stored on it: its
 * lore sections, its printable properties and its rename properties.
 */
public final class ItemLoreRenderer {
    private ItemLoreRenderer() {
    }

    /**
     * Refreshes the lore and name of the item in place and returns it. The lore
     * is only rewritten when the item has properties. Returns null for a missing item.
     */
    public static ItemStack refresh(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        ItemData data = ItemData.from(meta);
        List<Component> lore = TextParser.TranslateToLore(data.getLoreSections(), data.getDeletedLines());
        if (data.hasProperties()) {
            List<String> propertyIds = data.getProperties();
            lore.add(getPlugin().getMiniMessage().deserialize(""));
            addPrintableProperties(lore, item, propertyIds);
            applyRenameProperties(meta, item, propertyIds);
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Rewrites the lore from the stored lore sections and the display name from
     * the stored name, without any property lines or tags. {@link #refresh}
     * leaves items without properties alone, so call this when an item loses
     * its last property.
     */
    public static void renderWithoutProperties(ItemMeta meta) {
        ItemData data = ItemData.from(meta);
        meta.lore(TextParser.TranslateToLore(data.getLoreSections(), data.getDeletedLines()));
        applyStoredName(meta);
    }

    /** Adds one lore line per printable property, heaviest first. */
    private static void addPrintableProperties(List<Component> lore, ItemStack item, List<String> propertyIds) {
        for (PrintableProperty property : printablePropertiesByWeight(propertyIds)) {
            if (property instanceof BreweryPrintableProperty brewProperty) {
                addBrewedPrinting(lore, item, brewProperty);
            } else {
                lore.add(getPlugin().getMiniMessage().deserialize(property.getParsedPrinting(item))
                        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
            }
        }
    }

    /** The item's printable properties, heaviest first; properties of equal weight keep their order. */
    static List<PrintableProperty> printablePropertiesByWeight(List<String> propertyIds) {
        List<PrintableProperty> printable = new ArrayList<>();
        for (String propertyId : propertyIds) {
            if (getPlugin().getPropertyRegistry().get(propertyId) instanceof PrintableProperty property) {
                printable.add(property);
            }
        }
        printable.sort(Comparator.comparingInt(PrintableProperty::getWeight).reversed());
        return printable;
    }

    private static void addBrewedPrinting(List<Component> lore, ItemStack item, BreweryPrintableProperty property) {
        // Brew scores come from TheBrewingProject; without it there is nothing to print.
        if (getPlugin().getBrewingProject().isEmpty()) {
            return;
        }
        if (property.getBrewedPrinting(item) == null) {
            Bukkit.getLogger().warning("BreweryPrintableProperty " + property.getId() + " returned null for brewed printing. Skipping.");
            return;
        }
        Bukkit.getLogger().warning("Adding lore for BreweryPrintableProperty " + property.getId() + ": " + property.getBrewedPrinting(item));
        lore.add(property.getBrewedPrinting(item));
    }

    /** Rebuilds the display name from the stored name, prefixed by the tag of each rename property. */
    private static void applyRenameProperties(ItemMeta meta, ItemStack item, List<String> propertyIds) {
        applyStoredName(meta);
        for (String propertyId : propertyIds) {
            if (getPlugin().getPropertyRegistry().get(propertyId) instanceof RenameProperty property && item.getItemMeta().displayName() != null) {
                String newName = meta.getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING);
                newName = TextParser.translateHexColorCodes("&#", "", newName);
                newName = TextParser.translateColorCodes(newName);
                meta.displayName(nameComponent(property.getTag() + " " + newName));
            }
        }
    }

    /** Sets the display name to the stored name, if the item has one. */
    private static void applyStoredName(ItemMeta meta) {
        String storedName = meta.getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING);
        if (storedName != null) {
            meta.displayName(nameComponent(TextParser.translateColorCodes(TextParser.translateHexColorCodes("&#", "", storedName))));
        }
    }

    private static Component nameComponent(String miniMessage) {
        return getPlugin().getMiniMessage().deserialize(miniMessage)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false));
    }
}
