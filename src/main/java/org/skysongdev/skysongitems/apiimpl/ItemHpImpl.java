package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ItemHp;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.timer.Timing;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.OptionalInt;

/** Reads the HP stored on items, and changes it through the {@link ItemEditor}. */
final class ItemHpImpl implements ItemHp {
    private final ItemEditor editor;
    private final ItemWrites.ItemCheck check;
    private final TimedWrites timed;

    ItemHpImpl(ItemEditor editor, ItemWrites.ItemCheck check, TimedWrites timed) {
        this.editor = editor;
        this.check = check;
        this.timed = timed;
    }

    @Override
    public OptionalInt get(ItemStack item) {
        return readInt(item, ItemKeys.ITEM_HP);
    }

    @Override
    public OptionalInt getMax(ItemStack item) {
        return readInt(item, ItemKeys.MAX_ITEM_HP);
    }

    @Override
    public boolean hasLeft(ItemStack item) {
        return get(item).orElse(0) > 0;
    }

    @Override
    public int set(Player holder, ItemStack item, int hp) {
        check.require(item);
        ItemWrites.requireDone(editorFor(holder).setItemHp(item, hp), "item HP " + hp);
        return get(item).orElse(0);
    }

    @Override
    public int addFor(Player holder, ItemStack item, int amount, String time) {
        Timing timing = timed.timing(time);
        int now = add(holder, item, amount);
        timed.undoLater(item, timing, BuiltInTimerActions.ADD_HP.id(), null, String.valueOf(-amount));
        return now;
    }

    @Override
    public int add(Player holder, ItemStack item, int amount) {
        check.require(item);
        ItemWrites.requireDone(editorFor(holder).addItemHp(item, amount), "item HP " + amount);
        return get(item).orElse(0);
    }

    @Override
    public void setMax(Player holder, ItemStack item, int maxHp) {
        check.require(item);
        ItemWrites.requireDone(editorFor(holder).setMaxItemHp(item, maxHp), "max item HP " + maxHp);
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
