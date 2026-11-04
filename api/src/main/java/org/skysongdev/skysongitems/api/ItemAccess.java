package org.skysongdev.skysongitems.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The tags, charges and properties of items, read and written one way.
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

    /** Reads and writes the properties stored on items, and tells which properties exist. */
    @NotNull ItemProperties properties();
}
