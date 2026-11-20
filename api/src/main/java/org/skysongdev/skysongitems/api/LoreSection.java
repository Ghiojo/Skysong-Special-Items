package org.skysongdev.skysongitems.api;

/**
 * The three sections an item's lore is written in, in the order the item shows them:
 * the ones staff edit with {@code /si physdesc}, {@code /si funcdesc} and {@code /si skilldesc}.
 *
 * @since 1.17.0
 */
public enum LoreSection {
    /** What the item looks like. */
    PHYSICAL,
    /** What the item does. */
    FUNCTIONAL,
    /** The skill the item grants. */
    SKILL
}
