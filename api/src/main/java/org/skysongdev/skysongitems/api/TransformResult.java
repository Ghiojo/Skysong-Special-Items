package org.skysongdev.skysongitems.api;

/** How an attempt to transform a held item ended. See {@link ItemTransformations#transform}. */
public enum TransformResult {
    /** The item is now in its next form. */
    TRANSFORMED,
    /** The player's main hand is empty. */
    NOT_HOLDING,
    /** The held item has no {@code material_id}, or no transformation starts from its form. */
    NO_TRANSFORMATION,
    /** The transformation needs a permission the player lacks. */
    NO_PERMISSION,
    /** A listener cancelled the {@link org.skysongdev.skysongitems.api.event.ItemTransformEvent}. */
    CANCELLED
}
