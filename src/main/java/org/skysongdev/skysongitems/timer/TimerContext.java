package org.skysongdev.skysongitems.timer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.ItemEditor;

/**
 * What a {@link TimerAction} works with.
 *
 * @param editor makes the change, firing its events with the cause TIMER and the holder
 * @param holder the player holding the item, or null for an item in a container
 */
public record TimerContext(ItemEditor editor, @Nullable Player holder) {
}
