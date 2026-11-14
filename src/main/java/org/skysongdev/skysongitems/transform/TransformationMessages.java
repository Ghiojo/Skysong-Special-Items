package org.skysongdev.skysongitems.transform;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * The lines {@code /transform} sends, in MiniMessage, from the {@code messages} section of
 * transformations.yml. A line missing there keeps its default.
 *
 * @param transformed      the item transformed, unless its rule has a message of its own
 * @param noTransformation the item has no transformation
 * @param notHolding       the player's main hand is empty
 * @param noPermission     the item's transformation needs a permission the player lacks
 */
public record TransformationMessages(String transformed, String noTransformation, String notHolding, String noPermission) {
    public static final TransformationMessages DEFAULTS = new TransformationMessages(
            "<green>Your item transforms.",
            "<gray>This item doesn't transform.",
            "<red>Hold the item you want to transform.",
            "<red>You can't transform this item.");

    /** The messages in this section, with the defaults for the ones it leaves out or when there is no section. */
    public static TransformationMessages from(@Nullable ConfigurationSection section) {
        if (section == null) {
            return DEFAULTS;
        }
        return new TransformationMessages(
                section.getString("transformed", DEFAULTS.transformed),
                section.getString("no-transformation", DEFAULTS.noTransformation),
                section.getString("not-holding", DEFAULTS.notHolding),
                section.getString("no-permission", DEFAULTS.noPermission));
    }
}
