package org.skysongdev.skysongitems.api;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Lore lines other plugins add to items, such as "Holds Mira" or "Owner: Alex". SkysongItems
 * redraws an item's lore from what it stores whenever its traits or charges change, which
 * would wipe lines written straight into the lore; lines set here are stored on the item
 * and drawn again by every redraw, at the {@link ExtraLorePosition} their plugin chose.
 *
 * <p>Each plugin only sees and changes its own lines. When several plugins put lines at
 * the same position, they are drawn in alphabetical order of plugin name.
 *
 * <p>Lines don't survive a transform, and saved items and snapshots never carry them;
 * {@link ItemTemplates#rebuild(ItemStack)} keeps them. Set them again when they are needed.
 *
 * <p>Must be called on the server's main thread, and throws {@link IllegalStateException} otherwise.
 *
 * @since 1.17.0
 */
@ApiStatus.NonExtendable
public interface ItemLore {
    /** The most lines one plugin can put on one item. */
    int MAX_LINES = 10;

    /**
     * Replaces the plugin's lines on the item, and where they go, then redraws the item's lore.
     * An empty list removes them.
     *
     * @throws IllegalArgumentException with more than {@link #MAX_LINES} lines, or when the
     *                                  item isn't a SkysongItems item (outside detached mode)
     */
    void setExtra(@NotNull Plugin owner, @NotNull ItemStack item, @NotNull ExtraLorePosition position,
                  @NotNull List<Component> lines);

    /** The plugin's lines on the item, empty when it has none. */
    @NotNull List<Component> getExtra(@NotNull Plugin owner, @Nullable ItemStack item);

    /** Where the plugin's lines on the item go, empty when it has none. */
    @NotNull Optional<ExtraLorePosition> getExtraPosition(@NotNull Plugin owner, @Nullable ItemStack item);

    /**
     * Removes the plugin's lines from the item and redraws its lore.
     *
     * @return whether the item had any
     */
    boolean removeExtra(@NotNull Plugin owner, @NotNull ItemStack item);

    /** The names, lowercased, of the plugins with lines on the item, alphabetical. */
    @NotNull List<String> extraOwners(@Nullable ItemStack item);
}
