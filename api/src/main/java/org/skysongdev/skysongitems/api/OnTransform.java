package org.skysongdev.skysongitems.api;

/**
 * What happens to a tag or trait when its item transforms into another form. A transform
 * is full: the new form brings its own tags and traits, so by default the held item's
 * ones are dropped. A tag or trait marked {@link #KEEP} stays on the item through every
 * form instead, such as a {@code broken} tag that should survive the change.
 *
 * <p>A kept tag keeps the held item's value even when the new form sets it too; a kept
 * trait keeps the tags whose values it prints along with it.
 *
 * @since 1.16.0
 */
public enum OnTransform {
    /** The tag or trait stays on the item, with the held item's value. */
    KEEP,
    /** The tag or trait comes from the new form; the held item's one is dropped. */
    DROP
}
