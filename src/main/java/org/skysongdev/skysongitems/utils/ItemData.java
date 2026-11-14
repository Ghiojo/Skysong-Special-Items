package org.skysongdev.skysongitems.utils;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Reads and writes the lore and traits a Skysong item stores in its
 * persistent data container.
 * <p>
 * The storage format is fixed by the items already in circulation:
 * <ul>
 *     <li>Lore is one string, its sections joined by the section sign (§).
 *     Section 0 is the physical description, 1 the functional description
 *     and 2 the skill description.</li>
 *     <li>Deleted lines is a mask with one '0' or '1' per lore section, where
 *     '1' hides that section.</li>
 *     <li>Traits are one string of trait ids joined by ';', stored under
 *     the {@code properties} key.</li>
 * </ul>
 * Changes are written to the container straight away, but like any other
 * {@link ItemMeta} change they only reach the item once the caller calls
 * {@code item.setItemMeta(meta)}.
 */
public final class ItemData {
    private static final String LORE_SEPARATOR = "§";
    private static final String TRAIT_SEPARATOR = ";";
    private static final String NO_DELETED_LINES = "000";

    private final PersistentDataContainer container;

    public ItemData(PersistentDataContainer container) {
        this.container = Objects.requireNonNull(container, "container");
    }

    public static ItemData from(ItemMeta meta) {
        return new ItemData(meta.getPersistentDataContainer());
    }

    /**
     * Returns the stored lore sections as a list the caller may change.
     *
     * @throws NullPointerException if the item has no stored lore
     */
    public List<String> getLoreSections() {
        String lore = Objects.requireNonNull(container.get(ItemKeys.LORE, PersistentDataType.STRING), "item has no stored lore");
        return new ArrayList<>(Arrays.asList(lore.split(LORE_SEPARATOR)));
    }

    public void setLoreSections(List<String> sections) {
        container.set(ItemKeys.LORE, PersistentDataType.STRING, String.join(LORE_SEPARATOR, sections));
    }

    public boolean hasDeletedLines() {
        return container.has(ItemKeys.DELETED_LINES, PersistentDataType.STRING);
    }

    /** Returns the deleted lines mask, or {@code null} if no section was ever hidden. */
    public String getDeletedLines() {
        return container.get(ItemKeys.DELETED_LINES, PersistentDataType.STRING);
    }

    public void setDeletedLines(String deletedLines) {
        container.set(ItemKeys.DELETED_LINES, PersistentDataType.STRING, deletedLines);
    }

    /** Hides the given lore section if it is shown, or shows it if it is hidden. */
    public void toggleSectionDeleted(int section) {
        StringBuilder mask = new StringBuilder(hasDeletedLines() ? getDeletedLines() : NO_DELETED_LINES);
        mask.setCharAt(section, mask.charAt(section) == '0' ? '1' : '0');
        setDeletedLines(mask.toString());
    }

    public boolean hasTraits() {
        return container.has(ItemKeys.PROPERTIES);
    }

    /**
     * Returns the item's trait ids as a list the caller may change, empty if
     * it has none. Blank ids, left behind on items whose last trait was
     * removed by older versions of the plugin, are skipped.
     */
    public List<String> getTraits() {
        return parseTraits(getTraitsText());
    }

    /**
     * Splits traits as stored on an item into their ids, skipping blank ones like
     * {@link #getTraits()} does. Gives a list the caller may change, empty for null.
     */
    public static List<String> parseTraits(String traitsText) {
        List<String> traitIds = new ArrayList<>();
        if (traitsText == null) {
            return traitIds;
        }
        for (String traitId : traitsText.split(TRAIT_SEPARATOR)) {
            if (!traitId.isBlank()) {
                traitIds.add(traitId);
            }
        }
        return traitIds;
    }

    public boolean hasTrait(String traitId) {
        return getTraits().contains(traitId);
    }

    /** Appends a trait id. Does not check whether the item already has it. */
    public void addTrait(String traitId) {
        String traits = getTraitsText();
        setTraitsText(traits == null ? traitId : traits + TRAIT_SEPARATOR + traitId);
    }

    /**
     * Removes a trait id. Removing the last one removes the traits entry
     * altogether, so the item no longer counts as having traits.
     *
     * @return whether the item had the trait
     */
    public boolean removeTrait(String traitId) {
        List<String> traitIds = getTraits();
        if (!traitIds.remove(traitId)) {
            return false;
        }
        if (traitIds.isEmpty()) {
            clearTraits();
        } else {
            setTraits(traitIds);
        }
        return true;
    }

    public void setTraits(List<String> traitIds) {
        setTraitsText(String.join(TRAIT_SEPARATOR, traitIds));
    }

    public void clearTraits() {
        container.remove(ItemKeys.PROPERTIES);
    }

    /**
     * Returns the traits exactly as stored, or {@code null} if the item has
     * none. Meant for copying an item to and from its YAML template; prefer
     * {@link #getTraits()} everywhere else.
     */
    public String getTraitsText() {
        return container.get(ItemKeys.PROPERTIES, PersistentDataType.STRING);
    }

    /** Stores the traits exactly as given. Counterpart of {@link #getTraitsText()}. */
    public void setTraitsText(String traits) {
        container.set(ItemKeys.PROPERTIES, PersistentDataType.STRING, traits);
    }
}
