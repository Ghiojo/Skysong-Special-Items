package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkysongItem {
    private String name;
    private ArrayList<String> lore;
    private String type;
    private NamespacedKey itemModel;
    private String deleted;
    private String properties;
    // Each tag is stored with its own value so the two can never get out of step.
    private LinkedHashMap<NamespacedKey, String> tagValues;
    private CustomModelData customModelData;

    public SkysongItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, Map<NamespacedKey, String> tagValues) {
        this.name = name;
        this.lore = lore;
        this.type = type;
        this.itemModel = itemModel;
        this.deleted = deleted;
        this.properties = properties;
        this.tagValues = new LinkedHashMap<>(tagValues);
        this.customModelData = null;
    }

    public SkysongItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, Map<NamespacedKey, String> tagValues, CustomModelData customModelData) {
        this.name = name;
        this.lore = lore;
        this.type = type;
        this.itemModel = itemModel;
        this.deleted = deleted;
        this.properties = properties;
        this.tagValues = new LinkedHashMap<>(tagValues);
        this.customModelData = customModelData;
    }

    public SkysongItem(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        ItemData data = new ItemData(dataContainer);

        this.name = dataContainer.get(ItemKeys.NAME, PersistentDataType.STRING);
        this.lore = new ArrayList<String>(data.getLoreSections());
        this.type = item.getType().name();
        this.itemModel = meta.getItemModel();
        this.deleted = data.getDeletedLines();
        this.properties = data.getPropertiesText();
        this.tagValues = new LinkedHashMap<>();
        for(NamespacedKey tag : Utils.getAllTagsInItem(item)) {
            Object returnValue = dataContainer.get(tag, Constants.tagList.get(tag));
            if (returnValue instanceof String || returnValue instanceof Integer
                    || returnValue instanceof Float || returnValue instanceof Boolean) {
                tagValues.put(tag, String.valueOf(returnValue));
            }
        }

        this.customModelData = meta.hasCustomModelDataComponent() ? new CustomModelData(meta.getCustomModelDataComponent()) : null;
    }

    public String getName() {
        return name;
    }

    public String getFormattedName(){
        // Remove Minecraft color codes (&#HEX and &LETTER)
        if(name == null) return "no-name";
        String sanitized = MiniMessage.miniMessage().stripTags(name)
                .replaceAll("(?i)&#[a-f0-9]{6}", "") // Remove &#HEX codes
                .replaceAll("&[a-z0-9]", "");       // Remove &LETTER codes

        // Remove any character that isn't a letter or space
        sanitized = sanitized.replaceAll("[^a-zA-Z\\s]", "");

        // Replace spaces with underscores and convert to lowercase
        return sanitized.replace(" ", "_").toLowerCase();
    }

    public ArrayList<String> getLore() {
        return lore;
    }

    public String getType() {
        return type;
    }

    public NamespacedKey getItemModel() {
        return itemModel;
    }

    public String getDeleted() {
        return deleted;
    }

    public String getProperties() {
        return properties;
    }

    /**
     * Pairs the tag ids and values read from an item file by position. Tags
     * that are not configured in config.yml are skipped together with their
     * value, and a tag without a value is skipped.
     */
    public static LinkedHashMap<NamespacedKey, String> pairTagValues(List<String> tagIds, List<String> values) {
        LinkedHashMap<NamespacedKey, String> pairs = new LinkedHashMap<>();
        for (int i = 0; i < tagIds.size() && i < values.size(); i++) {
            NamespacedKey tag = Utils.getTag(tagIds.get(i));
            if (tag != null) {
                pairs.put(tag, values.get(i));
            }
        }
        return pairs;
    }

    /** The item's tags and their values, in the order they were added. */
    public Map<NamespacedKey, String> getTagValues() {
        return Collections.unmodifiableMap(tagValues);
    }

    public ArrayList<NamespacedKey> getTags() {
        return new ArrayList<>(tagValues.keySet());
    }

    public ArrayList<String> getTagsAsStrings(){
        ArrayList<String> tagStrings = new ArrayList<>();
        for (NamespacedKey tag : tagValues.keySet()) {
            tagStrings.add(tag.value());
        }
        return tagStrings;
    }

    /** The tag values, in the same order as {@link #getTags()}. */
    public ArrayList<String> getValues() {
        return new ArrayList<>(tagValues.values());
    }

    public CustomModelData getCustomModelData() {
        return customModelData;
    }
}
