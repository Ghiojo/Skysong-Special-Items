package org.skysongdev.skysongitems.timer.action;

import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.TimerActions;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.List;
import java.util.function.IntFunction;

/**
 * The timer actions SkysongItems itself offers. Each makes the same change as the
 * {@code /si} command it is named after. Setting and removing don't depend on how many
 * times a timer came due, so they are applied once; adding to a number tag, HP or charges is
 * applied once per time it came due, in a single edit. HP and charges amounts are whole
 * numbers; a timer whose value isn't one does nothing.
 */
public final class BuiltInTimerActions {
    /** Sets a tag to the timer's value, as {@code /si tag set}. */
    public static final EditorTimerAction SET_TAG = new EditorTimerAction("set_tag", "tag set",
            (editor, item, timer, times) -> editor.setTag(item, timer.target(), timer.value()));
    /** Removes a tag, as {@code /si tag remove}. */
    public static final EditorTimerAction REMOVE_TAG = new EditorTimerAction("remove_tag", "tag remove",
            (editor, item, timer, times) -> editor.removeTag(item, timer.target()));
    /**
     * Adds the timer's value to a number tag, as {@code /si tag add}, once for every time the
     * timer came due; a negative value takes it off, as {@code /si tag reduce}.
     */
    public static final EditorTimerAction ADD_TAG = new EditorTimerAction("add_tag", "tag add",
            (editor, item, timer, times) -> editor.addTag(item, timer.target(), timer.value(), times));
    /** Adds a trait, as {@code /si trait add}. */
    public static final EditorTimerAction ADD_TRAIT = new EditorTimerAction("add_trait", "trait add",
            (editor, item, timer, times) -> editor.addTrait(item, timer.target()));
    /** Removes a trait, as {@code /si trait remove}. */
    public static final EditorTimerAction REMOVE_TRAIT = new EditorTimerAction("remove_trait", "trait remove",
            (editor, item, timer, times) -> editor.removeTrait(item, timer.target()));

    /** Sets the item's HP, as {@code /si hp set}. */
    public static final EditorTimerAction SET_HP = new EditorTimerAction("set_hp", "hp set",
            (editor, item, timer, times) -> withAmount(timer, amount -> editor.setItemHp(item, amount)));
    /** Sets the item's max HP, as {@code /si hp setmax}. */
    public static final EditorTimerAction SET_MAX_HP = new EditorTimerAction("set_max_hp", "hp setmax",
            (editor, item, timer, times) -> withAmount(timer, amount -> editor.setMaxItemHp(item, amount)));
    /**
     * Adds HP to the item, as {@code /si hp add}, once for every time the timer came due; a
     * negative value takes it off, as {@code /si hp reduce}.
     */
    public static final EditorTimerAction ADD_HP = new EditorTimerAction("add_hp", "hp add",
            (editor, item, timer, times) -> withAmount(timer, amount -> editor.addItemHp(item, total(amount, times))));
    /** Takes the item's HP off it, as {@code /si hp remove}. */
    public static final EditorTimerAction REMOVE_HP = new EditorTimerAction("remove_hp", "hp remove",
            (editor, item, timer, times) -> {
                editor.removeItemHp(item);
                return ItemEditor.Result.DONE;
            });
    /** Sets the item's charges, as {@code /si charges set}. */
    public static final EditorTimerAction SET_CHARGES = new EditorTimerAction("set_charges", "charges set",
            (editor, item, timer, times) -> withAmount(timer, amount -> editor.setCharges(item, amount)));
    /** Sets the item's max charges, as {@code /si charges setmax}. */
    public static final EditorTimerAction SET_MAX_CHARGES = new EditorTimerAction("set_max_charges", "charges setmax",
            (editor, item, timer, times) -> withAmount(timer, amount -> editor.setMaxCharges(item, amount)));
    /**
     * Adds charges to the item, as {@code /si charges add}, once for every time the timer came
     * due; a negative value takes them off, as {@code /si charges reduce}. Only staff set
     * timers, so like staff it adds charges even to an item that doesn't allow adding them.
     */
    public static final EditorTimerAction ADD_CHARGES = new EditorTimerAction("add_charges", "charges add",
            (editor, item, timer, times) -> withAmount(timer, amount -> editor.addCharges(item, total(amount, times), true)));
    /** Takes the item's charges off it, as {@code /si charges remove}. */
    public static final EditorTimerAction REMOVE_CHARGES = new EditorTimerAction("remove_charges", "charges remove",
            (editor, item, timer, times) -> {
                editor.removeCharges(item);
                return ItemEditor.Result.DONE;
            });

    private BuiltInTimerActions() {
    }

    /** Registers every built-in action. */
    public static void registerAll(TimerActions actions) {
        List.of(SET_TAG, REMOVE_TAG, ADD_TAG, ADD_TRAIT, REMOVE_TRAIT, SET_HP, SET_MAX_HP, ADD_HP, REMOVE_HP,
                SET_CHARGES, SET_MAX_CHARGES, ADD_CHARGES, REMOVE_CHARGES).forEach(actions::register);
    }

    /** Makes the edit with the timer's value read as a whole number, or refuses a value that isn't one. */
    private static ItemEditor.Result withAmount(ItemTimer timer, IntFunction<ItemEditor.Result> edit) {
        try {
            return edit.apply(Integer.parseInt(String.valueOf(timer.value())));
        } catch (NumberFormatException e) {
            return ItemEditor.Result.INVALID_AMOUNT;
        }
    }

    /** The amount added as many times as the timer came due, kept to what an int holds. */
    private static int total(int amount, long times) {
        return Math.clamp(amount * Math.min(times, Integer.MAX_VALUE), Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
