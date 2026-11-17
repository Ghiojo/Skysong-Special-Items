package org.skysongdev.skysongitems.timer.action;

import org.skysongdev.skysongitems.timer.TimerActions;

import java.util.List;

/**
 * The timer actions SkysongItems itself offers. Each makes the same change as the
 * {@code /si} command it is named after. Setting, adding and removing don't depend on how
 * many times a timer came due, so they are applied once; adding to a number tag is applied
 * once per time it came due, in a single edit.
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

    private BuiltInTimerActions() {
    }

    /** Registers every built-in action. */
    public static void registerAll(TimerActions actions) {
        List.of(SET_TAG, REMOVE_TAG, ADD_TAG, ADD_TRAIT, REMOVE_TRAIT).forEach(actions::register);
    }
}
