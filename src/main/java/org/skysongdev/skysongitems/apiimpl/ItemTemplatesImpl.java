package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.ItemTemplate;
import org.skysongdev.skysongitems.api.ItemTemplates;
import org.skysongdev.skysongitems.commands.lore.Updater;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemFactory;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/** Creates items from the saved templates in {@code GeneratedItems/} or from any template file. */
final class ItemTemplatesImpl implements ItemTemplates {
    private final Updater updater;

    ItemTemplatesImpl(Updater updater) {
        this.updater = updater;
    }

    @Override
    public Set<String> ids() {
        StoredValues.requireMainThread();
        return Set.copyOf(Constants.itemFileNames);
    }

    @Override
    public Optional<ItemStack> create(String templateId) {
        StoredValues.requireMainThread();
        // Only ids of templates found in GeneratedItems/ are accepted, so an id like
        // "../config" can never point outside that folder.
        if (templateId == null || !Constants.itemFileNames.contains(templateId)) {
            return Optional.empty();
        }
        File file = ItemTemplateRepository.savedItemFile(templateId);
        return Optional.of(ItemFactory.create(ItemTemplateRepository.load(file)));
    }

    @Override
    public ItemStack create(Path file) {
        StoredValues.requireMainThread();
        if (file == null || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException("No item template file at " + file);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        if (yaml.getString("name") == null || yaml.getString("type") == null) {
            throw new IllegalArgumentException("Not a valid item template, it needs a name and a type: " + file);
        }
        return ItemFactory.create(ItemTemplateRepository.load(file.toFile()));
    }

    /** Runs the same version steps as {@code /si update}, so a plain item counts as version 0. */
    @Override
    public ItemStack toSkysongItem(ItemStack item) {
        ItemWrites.requireItem(item);
        return updater.upgrade(item);
    }

    @Override
    public boolean isSkysongItem(ItemStack item) {
        StoredValues.requireMainThread();
        return ItemFactory.isSkysongItem(item);
    }

    /**
     * Reads what the item stores into a template. The stored name and physical description
     * are what the item is rebuilt from, so an item missing either is refused here instead
     * of failing while it is generated.
     */
    @Override
    public @NotNull ItemTemplate snapshot(ItemStack item) {
        ItemWrites.requireSkysongItem(item);
        requireStored(item, ItemKeys.NAME, "name");
        requireStored(item, ItemKeys.LORE, "physical description");
        return new SkysongItemTemplate(new SkysongItem(item));
    }

    /** The snapshot's new item, given back the lore lines other plugins put on the item. */
    @Override
    public @NotNull ItemStack rebuild(ItemStack item) {
        ItemStack rebuilt = snapshot(item).create();
        PersistentDataContainer extras = item.getPersistentDataContainer()
                .get(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER);
        if (extras == null) {
            return rebuilt;
        }
        ItemMeta meta = rebuilt.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER, extras);
        rebuilt.setItemMeta(meta);
        return ItemLoreRenderer.refresh(rebuilt);
    }

    private static void requireStored(ItemStack item, NamespacedKey key, String what) {
        if (!item.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            throw new IllegalArgumentException("The item has no stored " + what + ", so it can't be rebuilt."
                    + " Items made before SkysongItems stored it are updated by /si update.");
        }
    }
}
