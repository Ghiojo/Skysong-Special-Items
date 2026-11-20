package org.skysongdev.skysongitems.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Where a plugin's own lore lines go on an item, see {@link ItemLore}: right above or
 * below one of the item's {@link LoreSection}s, or after its trait lines. The lines are
 * drawn as a paragraph of their own, with a blank line between them and their neighbours.
 *
 * @since 1.17.0
 */
public final class ExtraLorePosition {
    private static final ExtraLorePosition AFTER_TRAITS = new ExtraLorePosition(null, false);

    private final LoreSection section;
    private final boolean above;

    private ExtraLorePosition(@Nullable LoreSection section, boolean above) {
        this.section = section;
        this.above = above;
    }

    /** Right above the section, even when the item hides it. */
    public static @NotNull ExtraLorePosition above(@NotNull LoreSection section) {
        return new ExtraLorePosition(Objects.requireNonNull(section, "section"), true);
    }

    /** Right below the section, even when the item hides it. */
    public static @NotNull ExtraLorePosition below(@NotNull LoreSection section) {
        return new ExtraLorePosition(Objects.requireNonNull(section, "section"), false);
    }

    /** At the end of the lore, after the trait lines. */
    public static @NotNull ExtraLorePosition afterTraits() {
        return AFTER_TRAITS;
    }

    /** The section the lines sit next to; null after the traits. */
    public @Nullable LoreSection section() {
        return section;
    }

    /** Whether the lines sit above their section rather than below it; false after the traits. */
    public boolean isAbove() {
        return above;
    }

    /** Whether the lines go at the end of the lore, after the trait lines. */
    public boolean isAfterTraits() {
        return section == null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExtraLorePosition position && section == position.section && above == position.above;
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, above);
    }

    @Override
    public String toString() {
        if (isAfterTraits()) {
            return "after traits";
        }
        return (above ? "above " : "below ") + section.name().toLowerCase(Locale.ROOT);
    }
}
