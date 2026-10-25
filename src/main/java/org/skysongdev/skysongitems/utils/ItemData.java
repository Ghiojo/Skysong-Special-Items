package org.skysongdev.skysongitems.utils;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Reads and writes the lore and properties a Skysong item stores in its
 * persistent data container.
 * <p>
 * The storage format is fixed by the items already in circulation:
 * <ul>
 *     <li>Lore is one string, its sections joined by the section sign (§).
 *     Section 0 is the physical description, 1 the functional description
 *     and 2 the skill description.</li>
 *     <li>Deleted lines is a mask with one '0' or '1' per lore section, where
 *     '1' hides that section.</li>
 *     <li>Properties is one string of property ids joined by ';'.</li>
 * </ul>
 * Changes are written to the container straight away, but like any other
 * {@link ItemMeta} change they only reach the item once the caller calls
 * {@code item.setItemMeta(meta)}.
 */
public final class ItemData {
    private static final String LORE_SEPARATOR = "§";
    private static final String PROPERTY_SEPARATOR = ";";
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

    public boolean hasProperties() {
        return container.has(ItemKeys.PROPERTIES);
    }

    /**
     * Returns the item's property ids as a list the caller may change, empty if
     * it has none. Blank ids, left behind on items whose last property was
     * removed by older versions of the plugin, are skipped.
     */
    public List<String> getProperties() {
        String properties = getPropertiesText();
        List<String> propertyIds = new ArrayList<>();
        if (properties == null) {
            return propertyIds;
        }
        for (String propertyId : properties.split(PROPERTY_SEPARATOR)) {
            if (!propertyId.isBlank()) {
                propertyIds.add(propertyId);
            }
        }
        return propertyIds;
    }

    public boolean hasProperty(String propertyId) {
        return getProperties().contains(propertyId);
    }

    /** Appends a property id. Does not check whether the item already has it. */
    public void addProperty(String propertyId) {
        String properties = getPropertiesText();
        setPropertiesText(properties == null ? propertyId : properties + PROPERTY_SEPARATOR + propertyId);
    }

    /**
     * Removes a property id. Removing the last one removes the properties entry
     * altogether, so the item no longer counts as having properties.
     *
     * @return whether the item had the property
     */
    public boolean removeProperty(String propertyId) {
        List<String> propertyIds = getProperties();
        if (!propertyIds.remove(propertyId)) {
            return false;
        }
        if (propertyIds.isEmpty()) {
            clearProperties();
        } else {
            setProperties(propertyIds);
        }
        return true;
    }

    public void setProperties(List<String> propertyIds) {
        setPropertiesText(String.join(PROPERTY_SEPARATOR, propertyIds));
    }

    public void clearProperties() {
        container.remove(ItemKeys.PROPERTIES);
    }

    /**
     * Returns the properties exactly as stored, or {@code null} if the item has
     * none. Meant for copying an item to and from its YAML template; prefer
     * {@link #getProperties()} everywhere else.
     */
    public String getPropertiesText() {
        return container.get(ItemKeys.PROPERTIES, PersistentDataType.STRING);
    }

    /** Stores the properties exactly as given. Counterpart of {@link #getPropertiesText()}. */
    public void setPropertiesText(String properties) {
        container.set(ItemKeys.PROPERTIES, PersistentDataType.STRING, properties);
    }
}
