package org.skysongdev.skysongitems.api;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Entry point of the SkysongItems API, the supported way for other plugins to work with
 * SkysongItems' items. Classes outside the {@code org.skysongdev.skysongitems.api} package
 * are internal and may change without notice.
 *
 * <p>SkysongItems registers this API with Bukkit's {@link org.bukkit.plugin.ServicesManager}
 * at the end of its {@code onEnable} and removes it in {@code onDisable}. Plugins that use it
 * add {@code softdepend: [Skysong_Items]} (or {@code depend}) to their plugin.yml, so they
 * enable after SkysongItems, and get it with {@link #get()}:
 *
 * <pre>{@code
 * SkysongItemsApi items = SkysongItemsApi.get();
 * }</pre>
 *
 * <p>SkysongItems implements this interface; other plugins must not.
 */
@ApiStatus.NonExtendable
public interface SkysongItemsApi {

    /**
     * The API registered by SkysongItems.
     *
     * @return the API
     * @throws IllegalStateException when SkysongItems is not installed or not enabled yet
     */
    static @NotNull SkysongItemsApi get() {
        RegisteredServiceProvider<SkysongItemsApi> registration =
                Bukkit.getServicesManager().getRegistration(SkysongItemsApi.class);
        if (registration == null) {
            throw new IllegalStateException("The SkysongItems API is not available: SkysongItems is not enabled."
                    + " Add 'softdepend: [Skysong_Items]' to your plugin.yml and check that SkysongItems is installed.");
        }
        return registration.getProvider();
    }

    /**
     * The version of the API that the installed SkysongItems implements, as
     * {@code major.minor.patch}. A plugin can compare it with the version it was built
     * against and log a clear message instead of failing with {@link NoSuchMethodError}.
     * Adding methods raises the minor version; removing or changing one raises the major.
     *
     * @return the API version, for example {@code "1.0.0"}
     */
    @NotNull String apiVersion();

    /** Reads and writes the tags stored on items, and tells which tags exist. */
    @NotNull ItemTags tags();

    /** Reads and writes the charges stored on items. */
    @NotNull ItemCharges charges();

    /** Reads and writes the properties stored on items, and tells which properties exist. */
    @NotNull ItemProperties properties();

    /** Creates items from templates, recognises SkysongItems items and converts plain ones. */
    @NotNull ItemTemplates templates();

    /**
     * The value of an int tag on one item, for example {@code getIntTag(item, "arcrot")}.
     * Same as {@code tags().getInt(item, tagId)}.
     *
     * @param item  the item, may be null or air
     * @param tagId the tag's id as {@code /si tag set} takes it, ignoring case
     * @return the value, empty when the item doesn't have the tag, the tag doesn't exist
     *         or isn't an int tag
     * @throws IllegalStateException when called off the server's main thread
     */
    default @NotNull OptionalInt getIntTag(@Nullable ItemStack item, @NotNull String tagId) {
        return tags().getInt(item, tagId);
    }

    /**
     * The charges left on an item.
     *
     * @param item the item, may be null or air
     * @return the charges, 0 when the item has none stored
     * @throws IllegalStateException when called off the server's main thread
     */
    default int getCharges(@Nullable ItemStack item) {
        return charges().get(item).orElse(0);
    }

    /**
     * Whether an item has charges left, the check behind SkysongStats'
     * {@code requires-charges: true}.
     *
     * @param item the item, may be null or air
     * @return true when the item has more than 0 charges
     * @throws IllegalStateException when called off the server's main thread
     */
    default boolean hasCharges(@Nullable ItemStack item) {
        return charges().hasLeft(item);
    }

    /**
     * Adds up an int tag over many items, each value multiplied by its stack's amount,
     * for example over a player's inventory. Same as {@code tags().sumInt(...)}.
     *
     * @param items           the items, which may include nulls and air
     * @param tagId           the tag's id as {@code /si tag set} takes it, ignoring case
     * @param onlyWithCharges when true, only items with more than 0 charges count
     * @return the total, 0 when the tag doesn't exist or isn't an int tag
     * @throws IllegalStateException when called off the server's main thread
     */
    default int sumIntTag(@NotNull Iterable<ItemStack> items, @NotNull String tagId, boolean onlyWithCharges) {
        return tags().sumInt(items, tagId, onlyWithCharges);
    }

    /**
     * Whether SkysongItems has a tag with this id. Safe from any thread.
     *
     * @param tagId the tag's id as {@code /si tag set} takes it, ignoring case
     * @return true when the tag exists
     */
    default boolean isTag(@NotNull String tagId) {
        return tags().exists(tagId);
    }

    /**
     * The type of a tag, to check for example that a configured tag holds whole numbers.
     * Safe from any thread.
     *
     * @param tagId the tag's id as {@code /si tag set} takes it, ignoring case
     * @return the type, empty when there is no tag with this id
     */
    default @NotNull Optional<TagType> getTagType(@NotNull String tagId) {
        return tags().type(tagId);
    }
}
