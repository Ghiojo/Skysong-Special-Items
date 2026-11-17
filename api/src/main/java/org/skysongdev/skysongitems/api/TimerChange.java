package org.skysongdev.skysongitems.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A change a timer makes to its item once it is due, the same change the matching
 * {@code /si} command makes. Make one with the factory methods and hand it to
 * {@link ItemTimers#after} or {@link ItemTimers#every}.
 *
 * @param action the action id timers store, as {@link ItemTimer#action()} reads it
 * @param target the tag or trait it is about, or null for changes without one, such as HP and charges
 * @param value  the value or amount it takes, as the command would read it, or null
 * @since 1.10.0
 */
public record TimerChange(@NotNull String action, @Nullable String target, @Nullable String value) {
    public TimerChange {
        Objects.requireNonNull(action, "action");
    }

    /** Sets a tag, like {@code /si tag set}; the value as {@link ItemTags#set} takes it. */
    public static TimerChange setTag(String tagId, Object value) {
        return new TimerChange("set_tag", tagId, String.valueOf(value));
    }

    /** Removes a tag, like {@code /si tag remove}. */
    public static TimerChange removeTag(String tagId) {
        return new TimerChange("remove_tag", tagId, null);
    }

    /**
     * Adds to a number tag, like {@code /si tag add}; a negative amount takes off, like
     * {@code /si tag reduce}. A repeating timer adds it once for each time it came due.
     */
    public static TimerChange addToTag(String tagId, Number amount) {
        return new TimerChange("add_tag", tagId, String.valueOf(amount));
    }

    /** Adds a trait, like {@code /si trait add}. */
    public static TimerChange addTrait(String traitId) {
        return new TimerChange("add_trait", traitId, null);
    }

    /** Removes a trait, like {@code /si trait remove}. */
    public static TimerChange removeTrait(String traitId) {
        return new TimerChange("remove_trait", traitId, null);
    }

    /**
     * Transforms the item into its next form, like {@code /si transform after|every}: as
     * {@code /transform} does, without its permission check or messages. A repeating timer
     * that came due several times moves the item on that many forms, round its cycle.
     *
     * @since 1.11.0
     */
    public static TimerChange transform() {
        return new TimerChange("transform", null, null);
    }

    /**
     * Sets the item's HP, like {@code /si hp set}: capped at its max HP.
     *
     * @since 1.14.0
     */
    public static TimerChange setHp(int hp) {
        return new TimerChange("set_hp", null, String.valueOf(hp));
    }

    /**
     * Sets the item's max HP, like {@code /si hp setmax}.
     *
     * @since 1.14.0
     */
    public static TimerChange setMaxHp(int maxHp) {
        return new TimerChange("set_max_hp", null, String.valueOf(maxHp));
    }

    /**
     * Adds HP to the item, like {@code /si hp add}; a negative amount takes it off, like
     * {@code /si hp reduce}. The HP stays between 0 and the max HP. A repeating timer adds
     * it once for each time it came due.
     *
     * @since 1.14.0
     */
    public static TimerChange addHp(int amount) {
        return new TimerChange("add_hp", null, String.valueOf(amount));
    }

    /**
     * Takes the item's HP off it, like {@code /si hp remove}.
     *
     * @since 1.14.0
     */
    public static TimerChange removeHp() {
        return new TimerChange("remove_hp", null, null);
    }

    /**
     * Sets the item's charges, like {@code /si charges set}: capped at its max charges.
     *
     * @since 1.14.0
     */
    public static TimerChange setCharges(int charges) {
        return new TimerChange("set_charges", null, String.valueOf(charges));
    }

    /**
     * Sets the item's max charges, like {@code /si charges setmax}.
     *
     * @since 1.14.0
     */
    public static TimerChange setMaxCharges(int maxCharges) {
        return new TimerChange("set_max_charges", null, String.valueOf(maxCharges));
    }

    /**
     * Adds charges to the item, like {@code /si charges add}; a negative amount takes them
     * off, like {@code /si charges reduce}. The charges stay between 0 and the max charges,
     * and are added even to an item that doesn't allow adding charges, as staff can. A
     * repeating timer adds them once for each time it came due.
     *
     * @since 1.14.0
     */
    public static TimerChange addCharges(int amount) {
        return new TimerChange("add_charges", null, String.valueOf(amount));
    }

    /**
     * Takes the item's charges off it, like {@code /si charges remove}.
     *
     * @since 1.14.0
     */
    public static TimerChange removeCharges() {
        return new TimerChange("remove_charges", null, null);
    }
}
