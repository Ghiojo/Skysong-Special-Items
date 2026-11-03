package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.OptionalInt;

/** Reads the charges stored on items. */
final class ItemChargeReader implements ItemCharges {
    @Override
    public OptionalInt get(ItemStack item) {
        return readInt(item, ItemKeys.CHARGES);
    }

    @Override
    public OptionalInt getMax(ItemStack item) {
        return readInt(item, ItemKeys.MAX_CHARGES);
    }

    @Override
    public boolean hasLeft(ItemStack item) {
        return get(item).orElse(0) > 0;
    }

    private static OptionalInt readInt(ItemStack item, NamespacedKey key) {
        StoredValues.requireMainThread();
        Integer value = StoredValues.read(item, key, PersistentDataType.INTEGER);
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }
}
