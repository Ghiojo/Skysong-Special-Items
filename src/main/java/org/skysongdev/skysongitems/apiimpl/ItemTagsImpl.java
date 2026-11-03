package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.TagRegistry;
import org.skysongdev.skysongitems.utils.TagTypes;
import org.skysongdev.skysongitems.utils.TagRegistry.RegisteredTag;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Reads tag values from items, resolving tag ids through the plugin's {@link TagRegistry},
 * and writes them through the {@link ItemEditor}.
 */
final class ItemTagsImpl implements ItemTags {
    /**
     * Tags the API never writes: {@code permission} grants permissions to whoever holds
     * the item, and the others are SkysongItems' own bookkeeping, written by its own code.
     */
    private static final Set<NamespacedKey> PROTECTED = Set.of(ItemKeys.PERMISSION, ItemKeys.VERSION, ItemKeys.LORE, ItemKeys.NAME);

    /** The Java type of the values each tag type takes; a float tag takes any number. */
    private static final Map<TagType, Class<?>> VALUE_CLASSES = new EnumMap<>(Map.of(
            TagType.INT, Integer.class,
            TagType.FLOAT, Number.class,
            TagType.BOOL, Boolean.class,
            TagType.STRING, String.class));

    private final TagRegistry registry;
    private final ItemCharges charges;
    private final ItemEditor editor;

    ItemTagsImpl(TagRegistry registry, ItemCharges charges, ItemEditor editor) {
        this.registry = registry;
        this.charges = charges;
        this.editor = editor;
    }

    @Override
    public void set(Player holder, ItemStack item, String tagId, Object value) {
        ItemWrites.requireSkysongItem(item);
        RegisteredTag tag = writableTag(tagId);
        String text = asText(TagTypes.of(tag.type()), value, tagId);
        ItemWrites.requireDone(editor.editedBy(holder, ItemChangeCause.API).setTag(item, tagId, text), "tag " + tagId);
    }

    @Override
    public boolean remove(Player holder, ItemStack item, String tagId) {
        ItemWrites.requireSkysongItem(item);
        writableTag(tagId);
        return ItemEditor.Result.DONE.equals(editor.editedBy(holder, ItemChangeCause.API).removeTag(item, tagId));
    }

    /** The tag with this id, when it exists and the API may write it. */
    private RegisteredTag writableTag(String tagId) {
        RegisteredTag tag = registry.find(tagId);
        if (tag == null) {
            throw new IllegalArgumentException("There is no tag " + tagId);
        }
        if (PROTECTED.contains(tag.key())) {
            throw new IllegalArgumentException("The " + tagId + " tag can't be written through the API");
        }
        return tag;
    }

    /**
     * The value as the text {@code /si tag set} takes, once it is checked to be the
     * Java type the tag's type stores.
     */
    private static String asText(TagType type, Object value, String tagId) {
        Class<?> expected = type == null ? null : VALUE_CLASSES.get(type);
        if (expected == null || !expected.isInstance(value)) {
            throw new IllegalArgumentException("The " + tagId + " tag holds " + type + " values and can't hold " + value);
        }
        if (TagType.FLOAT.equals(type)) {
            return Float.toString(((Number) value).floatValue());
        }
        return value.toString();
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
