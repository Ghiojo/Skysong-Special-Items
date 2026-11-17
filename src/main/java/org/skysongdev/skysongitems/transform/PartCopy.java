package org.skysongdev.skysongitems.transform;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.TagRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Copies single parts of the new form onto the item being transformed, for the
 * {@link ItemPart} kinds. A part the new form doesn't have is removed from the item, so
 * the item always matches the new form in the parts listed.
 */
final class PartCopy {
    /** Tags other parts take care of, which the {@code tags} part leaves alone. */
    private static final Set<NamespacedKey> NOT_PLAIN_TAGS = Set.of(ItemKeys.MATERIAL_ID, ItemKeys.CHARGES,
            ItemKeys.MAX_CHARGES, ItemKeys.CAN_ADD_CHARGES, ItemKeys.ITEM_HP, ItemKeys.MAX_ITEM_HP, ItemKeys.PERMISSION, ItemKeys.NAME, ItemKeys.LORE,
            ItemKeys.VERSION, ItemKeys.DELETED_LINES, ItemKeys.BOUND_BOOK);
    private static final int LORE_SECTIONS = 3;

    private final ItemMeta meta;
    private final ItemMeta targetMeta;
    private final PersistentDataContainer to;
    private final PersistentDataContainerView from;
    private final TagRegistry tags;

    /**
     * @param meta   the meta of the item being transformed, which the copies change
     * @param target the new form, built from its saved item
     */
    PartCopy(ItemMeta meta, ItemStack target, TagRegistry tags) {
        this.meta = meta;
        this.targetMeta = target.getItemMeta();
        this.to = meta.getPersistentDataContainer();
        this.from = target.getPersistentDataContainer();
        this.tags = tags;
    }

    /** The item turned into the given material, keeping everything it stores. */
    static ItemStack withMaterial(ItemStack item, Material material) {
        ItemStack changed = new ItemStack(material, item.getAmount());
        changed.setItemMeta(Bukkit.getItemFactory().asMetaFor(item.getItemMeta(), material));
        return changed;
    }

    /** Copies the value stored under this key, or removes it when the new form has none. */
    void copyKey(NamespacedKey key) {
        PersistentDataType<?, ?> type = typeOf(key);
        if (from.has(key, type)) {
            copy(key, type);
        } else {
            to.remove(key);
        }
    }

    private PersistentDataType<?, ?> typeOf(NamespacedKey key) {
        PersistentDataType<?, ?> type = tags.getType(key);
        return type != null ? type : PersistentDataType.STRING;
    }

    private <P, C> void copy(NamespacedKey key, PersistentDataType<P, C> type) {
        C value = from.get(key, type);
        if (value != null) {
            to.set(key, type, value);
        }
    }

    void copyLoreSection(int section) {
        List<String> sections = loreSections(meta);
        sections.set(section, loreSections(targetMeta).get(section));
        ItemData.from(meta).setLoreSections(sections);
    }

    /** The stored lore sections, at least one per section, the missing ones empty. */
    private static List<String> loreSections(ItemMeta meta) {
        List<String> sections = meta.getPersistentDataContainer().has(ItemKeys.LORE, PersistentDataType.STRING)
                ? ItemData.from(meta).getLoreSections() : new ArrayList<>();
        while (sections.size() < LORE_SECTIONS) {
            sections.add("");
        }
        return sections;
    }

    void copyTraits() {
        String traits = ItemData.readTraitsText(from);
        ItemData data = new ItemData(to);
        if (traits == null || traits.isEmpty()) {
            data.clearTraits();
        } else {
            data.setTraitsText(traits);
        }
    }

    /** Drops the item's own tags, keeping other plugins' ones, then copies the new form's tags. */
    void copyTags() {
        for (NamespacedKey key : List.copyOf(to.getKeys())) {
            if (isPlainTag(key) && tags.ownerOf(key) == null) {
                to.remove(key);
            }
        }
        for (NamespacedKey key : from.getKeys()) {
            if (isPlainTag(key)) {
                copy(key, tags.getType(key));
            }
        }
    }

    private boolean isPlainTag(NamespacedKey key) {
        return tags.contains(key) && !NOT_PLAIN_TAGS.contains(key);
    }

    void copyTag(String tagId) {
        NamespacedKey key = tags.getKey(tagId);
        if (key != null) {
            copyKey(key);
        }
    }

    /** Takes the new form's maximum, keeping the charges left up to it; no maximum means no charges. */
    void copyCharges() {
        if (copyMax(ItemKeys.CHARGES, ItemKeys.MAX_CHARGES)) {
            copyKey(ItemKeys.CAN_ADD_CHARGES);
        } else {
            to.remove(ItemKeys.CAN_ADD_CHARGES);
        }
    }

    /** Takes the new form's maximum HP, keeping the HP left up to it; no maximum means no HP. */
    void copyItemHp() {
        copyMax(ItemKeys.ITEM_HP, ItemKeys.MAX_ITEM_HP);
    }

    /**
     * Takes the new form's maximum, keeping the value left up to it. An item without a
     * value gets the new form's, or the full maximum. Returns false when the new form has
     * no maximum, after taking the value and maximum off the item.
     */
    private boolean copyMax(NamespacedKey current, NamespacedKey max) {
        Integer newMax = from.get(max, PersistentDataType.INTEGER);
        if (newMax == null) {
            to.remove(current);
            to.remove(max);
            return false;
        }
        Integer kept = to.get(current, PersistentDataType.INTEGER);
        Integer value = kept != null ? kept : from.get(current, PersistentDataType.INTEGER);
        to.set(max, PersistentDataType.INTEGER, newMax);
        to.set(current, PersistentDataType.INTEGER, value == null ? newMax : Math.min(value, newMax));
        return true;
    }

    void copyItemModel() {
        meta.setItemModel(targetMeta.getItemModel());
    }

    /**
     * Copies the custom model data component, or removes it when the new form has none.
     * Done on the stacks, after the meta is applied, as the plugin writes it as a component.
     */
    static void copyCustomModelData(ItemStack target, ItemStack item) {
        CustomModelData data = target.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (data == null) {
            item.unsetData(DataComponentTypes.CUSTOM_MODEL_DATA);
        } else {
            item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, data);
        }
    }
}
