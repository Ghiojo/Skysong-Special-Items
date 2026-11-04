package org.skysongdev.skysongitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Creates items from SkysongItems templates, the YAML files {@code /si saveitem} writes.
 *
 * <p>Must be called on the server's main thread, and throws {@link IllegalStateException} otherwise.
 */
@ApiStatus.NonExtendable
public interface ItemTemplates {
    /**
     * The ids of the saved templates, the names {@code /si loaditem} takes, such as
     * {@code sword} or {@code weapons/sword} for one saved into a folder. Immutable.
     */
    Set<String> ids();

    /**
     * Creates the item a saved template describes, like {@code /si loaditem} does.
     * Empty when no saved template has this id.
     */
    Optional<ItemStack> create(String templateId);

    /**
     * Creates the item a template file describes, for example one in the calling
     * plugin's own data folder, in the format {@code /si saveitem} writes.
     *
     * @throws IllegalArgumentException when the file does not exist or is not a valid template
     */
    ItemStack create(Path file);

    /** Whether the item was made by SkysongItems, which marks every item it makes with a version. */
    boolean isSkysongItem(@Nullable ItemStack item);

    /**
     * A template of everything SkysongItems stores on the item, which
     * {@link ItemTemplate#create()} turns back into a fresh item. A plugin that changes an
     * item's tags or properties and then wants a clean item of the result rebuilds it
     * with {@link #rebuild(ItemStack)}.
     *
     * <p>The snapshot is taken when this method is called, so later changes to the item
     * don't show in items the template creates.
     *
     * @param item a SkysongItems item with a stored name and physical description
     * @throws IllegalArgumentException when the item is null, air, not a SkysongItems item,
     *                                  or has no stored name or physical description
     */
    @NotNull ItemTemplate snapshot(ItemStack item);

    /**
     * A new item built from what SkysongItems stores on this one: its material, stored
     * name and physical description, deleted lines, properties text, item model, registered
     * tags with their values, and custom model data. Everything else the item carried is
     * left behind, such as enchantments, durability and tags that are no longer registered.
     *
     * <p>The item itself is not changed. Same as {@code snapshot(item).create()}.
     *
     * @param item a SkysongItems item with a stored name and physical description
     * @return a new item, rebuilt from the stored data
     * @throws IllegalArgumentException when the item is null, air, not a SkysongItems item,
     *                                  or has no stored name or physical description
     */
    default @NotNull ItemStack rebuild(ItemStack item) {
        return snapshot(item).create();
    }

    /**
     * Turns the item into a SkysongItems item of the current version, in place, so the write
     * methods accept it. A plain item's custom name and lore become its stored name and
     * physical description, like {@code /si update} does for items made before SkysongItems
     * stored them. An item from an older SkysongItems version is upgraded, and a current one
     * is left as it is.
     *
     * @return the same item, for chaining
     * @throws IllegalArgumentException when the item is null or air
     */
    ItemStack toSkysongItem(ItemStack item);
}
