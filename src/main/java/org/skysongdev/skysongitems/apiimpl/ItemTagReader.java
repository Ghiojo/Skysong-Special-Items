package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.utils.TagRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry.RegisteredTag;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/** Reads tag values from items, resolving tag ids through the plugin's {@link TagRegistry}. */
final class ItemTagReader implements ItemTags {
    private final TagRegistry registry;
    private final ItemCharges charges;

    ItemTagReader(TagRegistry registry, ItemCharges charges) {
        this.registry = registry;
        this.charges = charges;
    }

    @Override
    public OptionalInt getInt(ItemStack item, String tagId) {
        Integer value = read(item, tagId, PersistentDataType.INTEGER);
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    @Override
    public Optional<Float> getFloat(ItemStack item, String tagId) {
        return Optional.ofNullable(read(item, tagId, PersistentDataType.FLOAT));
    }

    @Override
    public Optional<Boolean> getBool(ItemStack item, String tagId) {
        return Optional.ofNullable(read(item, tagId, PersistentDataType.BOOLEAN));
    }

    @Override
    public Optional<String> getString(ItemStack item, String tagId) {
        return Optional.ofNullable(read(item, tagId, PersistentDataType.STRING));
    }

    @Override
    public int sumInt(Iterable<ItemStack> items, String tagId, boolean onlyWithChargesLeft) {
        StoredValues.requireMainThread();
        NamespacedKey key = keyOfType(tagId, PersistentDataType.INTEGER);
        if (key == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack item : items) {
            total += countOf(item, key, onlyWithChargesLeft);
        }
        return total;
    }

    /** The tag's value times the stack's amount, or 0 when the item does not count. */
    private int countOf(ItemStack item, NamespacedKey key, boolean onlyWithChargesLeft) {
        Integer value = StoredValues.read(item, key, PersistentDataType.INTEGER);
        if (value == null) {
            return 0;
        }
        if (onlyWithChargesLeft && !charges.hasLeft(item)) {
            return 0;
        }
        return value * item.getAmount();
    }

    @Override
    public boolean exists(String tagId) {
        return registry.contains(tagId);
    }

    @Override
    public Optional<TagType> type(String tagId) {
        RegisteredTag tag = registry.find(tagId);
        return tag == null ? Optional.empty() : Optional.ofNullable(TagTypes.of(tag.type()));
    }

    @Override
    public Set<String> ids() {
        return Set.copyOf(registry.getIds());
    }

    /** The tag's value on the item, or null when either is missing or the tag holds another type. */
    private <P, C> C read(ItemStack item, String tagId, PersistentDataType<P, C> type) {
        StoredValues.requireMainThread();
        NamespacedKey key = keyOfType(tagId, type);
        return key == null ? null : StoredValues.read(item, key, type);
    }

    /** The key of the tag with this id, or null when there is none or it holds another type. */
    private NamespacedKey keyOfType(String tagId, PersistentDataType<?, ?> type) {
        RegisteredTag tag = registry.find(tagId);
        return tag != null && type.equals(tag.type()) ? tag.key() : null;
    }
}
