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
 * lore sections, its printable traits and its rename traits.
 */
public final class ItemLoreRenderer {
    private ItemLoreRenderer() {
    }

    /**
     * Refreshes the lore and name of the item in place and returns it. The lore
     * is only rewritten when the item has traits. Returns null for a missing item.
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
        List<Component> lore = sectionLines(meta, data.getLoreSections());
        if (data.hasTraits()) {
            List<String> traitIds = data.getTraits();
            lore.add(getPlugin().getMiniMessage().deserialize(""));
            addPrintableTraits(lore, item, traitIds);
            applyRenameTraits(meta, item, traitIds);
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Rewrites the lore from the stored lore sections and the display name from
     * the stored name, without any trait lines or tags. {@link #refresh}
     * leaves items without traits alone, so call this when an item loses
     * its last trait.
     */
    public static void renderWithoutTraits(ItemMeta meta) {
        renderSections(meta, ItemData.from(meta).getLoreSections());
        applyStoredName(meta);
    }

    /**
     * Rewrites the lore from these lore sections, leaving out the sections the item
     * hides, without trait lines. Every place that writes an item's lore goes through
     * here or {@link #refresh}, so the lore is always laid out the same way.
     */
    public static void renderSections(ItemMeta meta, List<String> sections) {
        meta.lore(sectionLines(meta, sections));
    }

    /** The lines these lore sections show on the item, its hidden sections left out. */
    private static List<Component> sectionLines(ItemMeta meta, List<String> sections) {
        return TextParser.TranslateToLore(sections, ItemData.from(meta).getDeletedLines());
    }

    /** Adds one lore line per printable trait, heaviest first. */
    private static void addPrintableTraits(List<Component> lore, ItemStack item, List<String> traitIds) {
        for (PrintableTrait trait : printableTraitsByWeight(traitIds)) {
            if (trait instanceof BreweryPrintableTrait brewTrait) {
                addBrewedPrinting(lore, item, brewTrait);
            } else {
                lore.add(getPlugin().getMiniMessage().deserialize(trait.getParsedPrinting(item))
                        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
            }
        }
    }

    /** The item's printable traits, heaviest first; traits of equal weight keep their order. */
    static List<PrintableTrait> printableTraitsByWeight(List<String> traitIds) {
        List<PrintableTrait> printable = new ArrayList<>();
        for (String traitId : traitIds) {
            if (getPlugin().getTraitRegistry().get(traitId) instanceof PrintableTrait trait) {
                printable.add(trait);
            }
        }
        printable.sort(Comparator.comparingInt(PrintableTrait::getWeight).reversed());
        return printable;
    }

    private static void addBrewedPrinting(List<Component> lore, ItemStack item, BreweryPrintableTrait trait) {
        // Brew scores come from TheBrewingProject; without it there is nothing to print.
        if (getPlugin().getBrewingProject().isEmpty()) {
            return;
        }
        if (trait.getBrewedPrinting(item) == null) {
            Bukkit.getLogger().warning("BreweryPrintableTrait " + trait.getId() + " returned null for brewed printing. Skipping.");
            return;
        }
        Bukkit.getLogger().warning("Adding lore for BreweryPrintableTrait " + trait.getId() + ": " + trait.getBrewedPrinting(item));
        lore.add(trait.getBrewedPrinting(item));
    }

    /** Rebuilds the display name from the stored name, prefixed by the tag of each rename trait. */
    private static void applyRenameTraits(ItemMeta meta, ItemStack item, List<String> traitIds) {
        applyStoredName(meta);
        for (String traitId : traitIds) {
            if (getPlugin().getTraitRegistry().get(traitId) instanceof RenameTrait trait && item.getItemMeta().displayName() != null) {
                String newName = meta.getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING);
                newName = TextParser.translateHexColorCodes("&#", "", newName);
                newName = TextParser.translateColorCodes(newName);
                meta.displayName(nameComponent(trait.getTag() + " " + newName));
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
