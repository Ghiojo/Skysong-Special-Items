package org.skysongdev.skysongitems.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * The old name of {@link ItemTraits}, from before properties were renamed to traits.
 * It has the same methods and returns the same results.
 *
 * @deprecated use {@link ItemTraits}, reached through {@link ItemAccess#traits()}.
 *             This name is removed in the next major version of the API.
 */
@Deprecated(since = "1.4.0", forRemoval = true)
@ApiStatus.NonExtendable
public interface ItemProperties extends ItemTraits {
}
