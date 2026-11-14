package org.skysongdev.skysongitems.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The transformations in SkysongItems' transformations.yml, which {@code /transform} uses
 * to turn a held item into its next form. An item's form is its {@code material_id} tag.
 *
 * <p>Must be called on the server's main thread, and throws {@link IllegalStateException} otherwise.
 */
@ApiStatus.NonExtendable
public interface ItemTransformations {
    /**
     * The saved item this item becomes when it transforms, by the id {@code /si loaditem}
     * and {@link ItemTemplates#create(String)} take, such as {@code weapons/night_blade}.
     *
     * @param item the item, may be null or air
     * @return the saved item's id, empty when the item's form has no transformation
     */
    @NotNull Optional<String> targetOf(@Nullable ItemStack item);

    /**
     * Transforms the item in the player's main hand, exactly as {@code /transform} does but
     * without sending the player a message. Fires
     * {@link org.skysongdev.skysongitems.api.event.ItemTransformEvent} first.
     *
     * @param player the player holding the item
     * @return how it ended
     */
    @NotNull TransformResult transform(@NotNull Player player);
}
