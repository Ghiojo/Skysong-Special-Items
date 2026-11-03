package org.skysongdev.skysongitems.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.api.event.ItemChargesChangeEvent;
import org.skysongdev.skysongitems.api.event.ItemTagChangeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Fires {@link ItemChargesChangeEvent} and {@link ItemTagChangeEvent} for an item edit.
 * {@link ItemEditor} records the item's tag values before it edits the item and hands
 * them back afterwards; every registered tag whose value differs then fires one event,
 * charges as an {@link ItemChargesChangeEvent} and every other tag as an
 * {@link ItemTagChangeEvent}. An item without charges counts as having 0.
 *
 * <p>Comparing values instead of reporting what each edit meant to do keeps one rule for
 * every edit: a change that writes the same value again fires nothing, and an edit that
 * also writes other tags, like adding a printable property, reports each of them.
 */
final class ItemChangeEvents {
    private final TagRegistry tags;

    ItemChangeEvents(TagRegistry tags) {
        this.tags = Objects.requireNonNull(tags, "tags");
    }

    /**
     * The item's charges and the values of its registered tags, read in place without
     * copying the item's meta.
     */
    Map<NamespacedKey, Object> snapshot(ItemStack item) {
        Map<NamespacedKey, Object> values = new HashMap<>();
        var data = item.getPersistentDataContainer();
        for (NamespacedKey key : data.getKeys()) {
            PersistentDataType<?, ?> type = ItemKeys.CHARGES.equals(key) ? PersistentDataType.INTEGER : tags.getType(key);
            if (type != null && data.has(key, type)) {
                values.put(key, data.get(key, type));
            }
        }
        return values;
    }

    /** Fires an event for every value that differs between {@code before} and the item as it is now. */
    void fireChanges(Player holder, ItemStack item, Map<NamespacedKey, Object> before, ItemChangeCause cause) {
        Map<NamespacedKey, Object> after = snapshot(item);
        Set<NamespacedKey> keys = new HashSet<>(before.keySet());
        keys.addAll(after.keySet());
        for (NamespacedKey key : keys) {
            Object oldValue = before.get(key);
            Object newValue = after.get(key);
            if (!Objects.equals(oldValue, newValue)) {
                fire(holder, item, key, oldValue, newValue, cause);
            }
        }
    }

    private void fire(Player holder, ItemStack item, NamespacedKey key, Object oldValue, Object newValue, ItemChangeCause cause) {
        if (ItemKeys.CHARGES.equals(key)) {
            fireCharges(holder, item, chargesOf(oldValue), chargesOf(newValue), cause);
            return;
        }
        TagType type = TagTypes.of(tags.getType(key));
        if (type != null) {
            Bukkit.getPluginManager().callEvent(new ItemTagChangeEvent(holder, item, key.getKey(), type, oldValue, newValue, cause));
        }
    }

    /** Fires the charges event, unless the count stays the same, as when an item without charges gets 0. */
    private static void fireCharges(Player holder, ItemStack item, int oldCharges, int newCharges, ItemChangeCause cause) {
        if (oldCharges != newCharges) {
            Bukkit.getPluginManager().callEvent(new ItemChargesChangeEvent(holder, item, oldCharges, newCharges, cause));
        }
    }

    /** The stored charges, where an item without charges counts as 0. */
    private static int chargesOf(Object value) {
        return value instanceof Integer charges ? charges : 0;
    }
}
