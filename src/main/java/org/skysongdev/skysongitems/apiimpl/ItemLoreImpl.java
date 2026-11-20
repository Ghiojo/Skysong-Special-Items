package org.skysongdev.skysongitems.apiimpl;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.api.ExtraLorePosition;
import org.skysongdev.skysongitems.api.ItemLore;
import org.skysongdev.skysongitems.utils.ExtraLore;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores other plugins' lore lines on items through {@link ExtraLore}. Items players hold
 * have their lore redrawn straight away; detached items only get the lines stored.
 */
final class ItemLoreImpl implements ItemLore {
    private final ItemWrites.ItemCheck check;

    ItemLoreImpl(ItemWrites.ItemCheck check) {
        this.check = check;
    }

    @Override
    public void setExtra(@NotNull Plugin owner, @NotNull ItemStack item, @NotNull ExtraLorePosition position,
                         @NotNull List<Component> lines) {
        Objects.requireNonNull(position, "position");
        List<Component> copied = List.copyOf(lines);
        if (copied.size() > MAX_LINES) {
            throw new IllegalArgumentException(owner.getName() + " gave " + copied.size()
                    + " lore lines; a plugin can put at most " + MAX_LINES + " on an item");
        }
        check.require(item);
        ItemMeta meta = item.getItemMeta();
        ExtraLore.write(meta.getPersistentDataContainer(), ownerOf(owner), position, copied);
        item.setItemMeta(meta);
        redraw(item);
    }

    @Override
    public @NotNull List<Component> getExtra(@NotNull Plugin owner, @Nullable ItemStack item) {
        return entry(owner, item).map(ExtraLore.Entry::lines).orElse(List.of());
    }

    @Override
    public @NotNull Optional<ExtraLorePosition> getExtraPosition(@NotNull Plugin owner, @Nullable ItemStack item) {
        return entry(owner, item).map(ExtraLore.Entry::position);
    }

    @Override
    public boolean removeExtra(@NotNull Plugin owner, @NotNull ItemStack item) {
        check.require(item);
        ItemMeta meta = item.getItemMeta();
        if (!ExtraLore.remove(meta.getPersistentDataContainer(), ownerOf(owner))) {
            return false;
        }
        item.setItemMeta(meta);
        redraw(item);
        return true;
    }

    @Override
    public @NotNull List<String> extraOwners(@Nullable ItemStack item) {
        return stored(item).entries().stream().map(ExtraLore.Entry::owner).toList();
    }

    private Optional<ExtraLore.Entry> entry(Plugin owner, ItemStack item) {
        return stored(item).of(ownerOf(owner));
    }

    private static ExtraLore stored(@Nullable ItemStack item) {
        StoredValues.requireMainThread();
        if (item == null || item.getType().isAir()) {
            return ExtraLore.none();
        }
        return ExtraLore.read(item.getPersistentDataContainer());
    }

    /** The owner as stored: the plugin's name, lowercased, as Bukkit puts it in its keys. */
    private static String ownerOf(Plugin owner) {
        return new NamespacedKey(Objects.requireNonNull(owner, "owner"), "lines").getNamespace();
    }

    /** Redraws the item's lore, unless it is detached. */
    private void redraw(ItemStack item) {
        if (check == ItemWrites.ItemCheck.SKYSONG_ITEM) {
            ItemLoreRenderer.redraw(item);
        }
    }
}
