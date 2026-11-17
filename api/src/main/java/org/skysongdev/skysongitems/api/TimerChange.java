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
 * @param target the tag or trait it is about, or null for changes without one
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
}
