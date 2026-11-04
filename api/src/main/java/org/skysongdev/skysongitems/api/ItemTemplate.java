package org.skysongdev.skysongitems.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A parsed description of a SkysongItems item, which creates items from it as often as
 * needed. A template is read once, from a snapshot of an item
 * ({@link ItemTemplates#snapshot(ItemStack)}) or from a template file, and each
 * {@link #create()} builds a new item from it, so a plugin can parse at load time and
 * generate at use time.
 *
 * <p>A template holds only what SkysongItems stores: the material, the stored name and
 * physical description, the deleted lines, the properties text, the item model, the
 * registered tags with their values, and the custom model data. Anything else an item
 * carried, such as enchantments or durability, is not part of it.
 *
 * <p>A template keeps the values it was read with, so it is not affected by later changes
 * to the item or file it came from. Tags are the one exception: only tags that are
 * registered are written, both when the template is read and when it creates an item, so a
 * tag that a {@code /si reload} takes out of the config stops being written.
 *
 * <p>SkysongItems implements this interface; other plugins must not.
 */
@ApiStatus.NonExtendable
public interface ItemTemplate {
    /**
     * The material of the items this template creates, {@link Material#BARRIER} when its
     * {@code type} matches no material.
     *
     * @return the material, never null
     */
    @NotNull Material material();

    /**
     * A new item built from this template, like {@code /si loaditem} builds a saved one.
     *
     * @return a new item, never the same one twice
     * @throws IllegalStateException when called off the server's main thread
     */
    @NotNull ItemStack create();
}
