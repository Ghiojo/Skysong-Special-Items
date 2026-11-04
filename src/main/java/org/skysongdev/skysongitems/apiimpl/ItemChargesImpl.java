package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.OptionalInt;

/** Reads the charges stored on items, and changes them through the {@link ItemEditor}. */
final class ItemChargesImpl implements ItemCharges {
    private final ItemEditor editor;
    private final ItemWrites.ItemCheck check;

    ItemChargesImpl(ItemEditor editor, ItemWrites.ItemCheck check) {
        this.editor = editor;
        this.check = check;
    }

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

    @Override
    public int set(Player holder, ItemStack item, int charges) {
        check.require(item);
        ItemWrites.requireDone(editorFor(holder).setCharges(item, charges), "charges " + charges);
        return get(item).orElse(0);
    }

    /** Like {@code /si charges add} by a player without the permission to add to locked items. */
    @Override
    public int add(Player holder, ItemStack item, int amount) {
        check.require(item);
        ItemWrites.requireDone(editorFor(holder).addCharges(item, amount, false), "charges " + amount);
        return get(item).orElse(0);
    }

    @Override
    public void setMax(Player holder, ItemStack item, int maxCharges) {
        check.require(item);
        ItemWrites.requireDone(editorFor(holder).setMaxCharges(item, maxCharges), "max charges " + maxCharges);
    }

    private ItemEditor editorFor(Player holder) {
        return editor.editedBy(holder, ItemChangeCause.API);
    }

    private static OptionalInt readInt(ItemStack item, NamespacedKey key) {
        StoredValues.requireMainThread();
        Integer value = StoredValues.read(item, key, PersistentDataType.INTEGER);
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }
}
