package org.skysongdev.skysongitems.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The tags, charges, HP, traits and timers of items, read and written one way.
 *
 * <p>{@link SkysongItemsApi} itself is the way items a player holds are edited: a write is
 * checked the way the {@code /si} commands check it, shows in the item's lore, fires the
 * change events in {@code org.skysongdev.skysongitems.api.event}, and only accepts
 * SkysongItems items. {@link SkysongItemsApi#detached()} is the same three interfaces for
 * items nobody holds, such as a copy a plugin is building: it does none of that.
 *
 * <p>SkysongItems implements this interface; other plugins must not.
 */
@ApiStatus.NonExtendable
public interface ItemAccess {

    /** Reads and writes the tags stored on items, and tells which tags exist. */
    @NotNull ItemTags tags();

    /** Reads and writes the charges stored on items. */
    @NotNull ItemCharges charges();

    /**
     * Reads and writes the HP stored on items.
     *
     * @since 1.6.0
     */
    @NotNull ItemHp itemHp();

    /**
     * Reads and cancels the timers stored on items.
     *
     * @since 1.7.0
     */
    @NotNull ItemTimers timers();

    /** Reads and writes the traits stored on items, and tells which traits exist. */
    @NotNull ItemTraits traits();

    /**
     * The same object as {@link #traits()}, under its name from before properties were
     * renamed to traits.
     *
     * @deprecated use {@link #traits()}. This method is removed in the next major version of the API.
     */
    @Deprecated(since = "1.4.0", forRemoval = true)
    @NotNull ItemProperties properties();
}
