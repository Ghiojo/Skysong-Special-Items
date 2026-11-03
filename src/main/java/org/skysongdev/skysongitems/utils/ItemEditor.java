package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

/**
 * The one place that changes an item's charges, tags and properties. The
 * commands and consumption all go through it, so a change is validated,
 * written and drawn into the lore the same way wherever it comes from.
 * <p>
 * Every method changes the given item in place: it copies the item's meta
 * once, makes the change and writes the meta back. Charges and property
 * changes then redraw the lore with {@link ItemLoreRenderer#refresh}; tag
 * changes leave the lore alone. An item without stored lore is never redrawn,
 * as there is nothing to draw it from.
 * <p>
 * Methods report what they did with a {@link Result}, and the caller decides
 * how to tell the player. When the result is anything other than
 * {@link Result#DONE}, the item is left unchanged.
 */
public final class ItemEditor {
    /** The property every charged item carries, so its lore shows the charges. */
    public static final String CHARGES_PROPERTY = "charges";

    /** What an edit did. */
    public enum Result {
        /** The change was made. */
        DONE,
        /** The item doesn't have both charges and max charges. */
        NOT_CHARGED,
        /** The item doesn't allow adding charges. */
        ADDING_LOCKED,
        /** The amount is out of range: negative charges, or max charges under 1. */
        INVALID_AMOUNT,
        /** No tag is registered with this id. */
        UNKNOWN_TAG,
        /** The tag holds numbers and the value isn't one. */
        NOT_A_NUMBER,
        /** The tag holds true or false and the value is neither. */
        NOT_A_BOOLEAN,
        /** No property is registered with this id. */
        UNKNOWN_PROPERTY,
        /** The item already has the property. */
        ALREADY_PRESENT,
        /** The item doesn't have the tag or property. */
        MISSING,
        /** The item has no properties at all. */
        NO_PROPERTIES
    }

    private final TagRegistry tags;
    private final PropertyRegistry properties;

    public ItemEditor(TagRegistry tags, PropertyRegistry properties) {
        this.tags = Objects.requireNonNull(tags, "tags");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /**
     * Sets the item's charges, capped at its max charges. An item without max
     * charges gets this amount as its max and the charges property.
     *
     * @return {@link Result#INVALID_AMOUNT} for a negative amount, otherwise {@link Result#DONE}
     */
    public Result setCharges(ItemStack item, int charges) {
        if (charges < 0) {
            return Result.INVALID_AMOUNT;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(ItemKeys.MAX_CHARGES)) {
            container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, charges);
            addChargesProperty(container);
        }
        int maxCharges = container.get(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, Math.min(charges, maxCharges));
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Sets the item's max charges. Charges the item already has are kept, even
     * above the new max. An item without charges gets this amount as its charges
     * and the charges property.
     *
     * @return {@link Result#INVALID_AMOUNT} for an amount under 1, otherwise {@link Result#DONE}
     */
    public Result setMaxCharges(ItemStack item, int maxCharges) {
        if (maxCharges < 1) {
            return Result.INVALID_AMOUNT;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(ItemKeys.CHARGES)) {
            container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, maxCharges);
            addChargesProperty(container);
        }
        container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, maxCharges);
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Adds charges to the item, or takes them off for a negative amount. The
     * result is kept between 0 and the item's max charges.
     *
     * @param ignoreLock whether to add charges even to an item that doesn't allow it
     * @return {@link Result#NOT_CHARGED} for an item without charges,
     *         {@link Result#ADDING_LOCKED} when adding to an item that doesn't allow it,
     *         otherwise {@link Result#DONE}
     */
    public Result addCharges(ItemStack item, int amount, boolean ignoreLock) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!isCharged(container)) {
            return Result.NOT_CHARGED;
        }
        if (amount > 0 && !ignoreLock && isAddingLocked(container)) {
            return Result.ADDING_LOCKED;
        }
        int charges = container.get(ItemKeys.CHARGES, PersistentDataType.INTEGER) + amount;
        int maxCharges = container.get(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges > maxCharges ? maxCharges : Math.max(charges, 0));
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Stores the charges an item has left after being used, exactly as given.
     * Unlike {@link #setCharges}, the amount isn't checked against the max.
     */
    public void setChargesLeft(ItemStack item, int charges) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges);
        saveAndRedraw(item, meta);
    }

    /** Takes the item's charges, max charges and charges property off it. */
    public void removeCharges(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(ItemKeys.CHARGES);
        container.remove(ItemKeys.MAX_CHARGES);
        // The lore redraw skips items without properties, so clear the charges
        // line here when charges was the last property.
        ItemData data = new ItemData(container);
        if (data.removeProperty(CHARGES_PROPERTY) && !data.hasProperties()) {
            ItemLoreRenderer.renderWithoutProperties(meta);
        }
        saveAndRedraw(item, meta);
    }

    /**
     * Turns adding charges to the item off if it is on, or on if it is off.
     * Read the new setting with {@link #canAddCharges}.
     *
     * @return {@link Result#NOT_CHARGED} for an item without charges, otherwise {@link Result#DONE}
     */
    public Result toggleChargeAdding(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!isCharged(container)) {
            return Result.NOT_CHARGED;
        }
        boolean canAdd = Boolean.TRUE.equals(container.get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN));
        container.set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, !canAdd);
        item.setItemMeta(meta);
        return Result.DONE;
    }

    /** Whether adding charges to the item was explicitly turned on. */
    public boolean canAddCharges(ItemStack item) {
        return Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer()
                .get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN));
    }

    /**
     * Sets a registered tag on the item, parsing the value as the tag's type.
     * The id matches a tag's key name, ignoring case.
     *
     * @return {@link Result#UNKNOWN_TAG}, {@link Result#NOT_A_NUMBER} or
     *         {@link Result#NOT_A_BOOLEAN} when the tag or value is invalid,
     *         otherwise {@link Result#DONE}
     */
    public Result setTag(ItemStack item, String tagId, String value) {
        TagRegistry.RegisteredTag tag = tags.find(tagId);
        if (tag == null) {
            return Result.UNKNOWN_TAG;
        }
        ItemMeta meta = item.getItemMeta();
        Result result = TagValues.write(meta.getPersistentDataContainer(), tag.key(), tag.type(), value);
        if (Result.DONE.equals(result)) {
            item.setItemMeta(meta);
        }
        return result;
    }

    /**
     * Removes a registered tag from the item. The id matches a tag's key name,
     * ignoring case.
     *
     * @return {@link Result#UNKNOWN_TAG} for an unregistered tag, {@link Result#MISSING}
     *         when the item doesn't have it, otherwise {@link Result#DONE}
     */
    public Result removeTag(ItemStack item, String tagId) {
        TagRegistry.RegisteredTag tag = tags.find(tagId);
        if (tag == null) {
            return Result.UNKNOWN_TAG;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(tag.key(), tag.type())) {
            return Result.MISSING;
        }
        container.remove(tag.key());
        item.setItemMeta(meta);
        return Result.DONE;
    }

    /**
     * Adds a registered property to the item. For a printable property, each of
     * its value tags is set to its type's empty value (0, 0.0, false or an empty
     * text), replacing any value already there.
     *
     * @return {@link Result#UNKNOWN_PROPERTY} for an unregistered property,
     *         {@link Result#ALREADY_PRESENT} when the item has it, otherwise {@link Result#DONE}
     */
    public Result addProperty(ItemStack item, String propertyId) {
        if (!properties.contains(propertyId)) {
            return Result.UNKNOWN_PROPERTY;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        ItemData data = new ItemData(container);
        if (data.hasProperty(propertyId)) {
            return Result.ALREADY_PRESENT;
        }
        data.addProperty(propertyId);
        if (properties.get(propertyId) instanceof PrintableProperty printable) {
            for (String valueTag : printable.getValuetags()) {
                NamespacedKey key = ItemKeys.named(valueTag);
                TagValues.writeEmpty(container, key, tags.getType(key));
            }
        }
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Removes a property from the item, whether or not it is still registered.
     * Removing the last one also takes the property lines and tags out of the
     * lore and name.
     *
     * @return {@link Result#NO_PROPERTIES} for an item without properties,
     *         {@link Result#MISSING} when the item doesn't have this one,
     *         otherwise {@link Result#DONE}
     */
    public Result removeProperty(ItemStack item, String propertyId) {
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        if (!data.hasProperties()) {
            return Result.NO_PROPERTIES;
        }
        if (!data.removeProperty(propertyId)) {
            return Result.MISSING;
        }
        if (!data.hasProperties()) {
            ItemLoreRenderer.renderWithoutProperties(meta);
        }
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    private static boolean isCharged(PersistentDataContainer container) {
        return container.has(ItemKeys.MAX_CHARGES) && container.has(ItemKeys.CHARGES);
    }

    /** Whether adding charges was explicitly turned off; items that never set it allow it. */
    private static boolean isAddingLocked(PersistentDataContainer container) {
        return Boolean.FALSE.equals(container.get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN));
    }

    private static void addChargesProperty(PersistentDataContainer container) {
        ItemData data = new ItemData(container);
        if (!data.hasProperty(CHARGES_PROPERTY)) {
            data.addProperty(CHARGES_PROPERTY);
        }
    }

    private static void saveAndRedraw(ItemStack item, ItemMeta meta) {
        boolean hasStoredLore = meta.getPersistentDataContainer().has(ItemKeys.LORE, PersistentDataType.STRING);
        item.setItemMeta(meta);
        if (hasStoredLore) {
            ItemLoreRenderer.refresh(item);
        }
    }
}
