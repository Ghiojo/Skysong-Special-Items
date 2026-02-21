package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;

public class SkysongItem {
    private String name;
    private ArrayList<String> lore;
    private String type;
    private NamespacedKey itemModel;
    private String deleted;
    private String properties;
    private ArrayList<NamespacedKey> tags;
    private ArrayList<String> values;
    private CustomModelData customModelData;

    public SkysongItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, ArrayList<NamespacedKey> tags, ArrayList<String> values) {
        this.name = name;
        this.lore = lore;
        this.type = type;
        this.itemModel = itemModel;
        this.deleted = deleted;
        this.properties = properties;
        this.tags = tags;
        this.values = values;
        this.customModelData = null;
    }

    public SkysongItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, ArrayList<NamespacedKey> tags, ArrayList<String> values, CustomModelData customModelData) {
        this.name = name;
        this.lore = lore;
        this.type = type;
        this.itemModel = itemModel;
        this.deleted = deleted;
        this.properties = properties;
        this.tags = tags;
        this.values = values;
        this.customModelData = customModelData;
    }

    public SkysongItem(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        this.name = dataContainer.get(Utils.nameKey, PersistentDataType.STRING);
        this.lore = new ArrayList<String>(Arrays.asList(dataContainer.get(Utils.loreKey, PersistentDataType.STRING).split(String.valueOf(ChatColor.COLOR_CHAR))));
        this.type = item.getType().name();
        this.itemModel = meta.getItemModel();
        this.deleted = dataContainer.get(Utils.deletedKey, PersistentDataType.STRING);
        this.properties = dataContainer.get(Utils.propertiesKey, PersistentDataType.STRING);
        this.tags = new ArrayList<NamespacedKey>(Utils.getAllTagsInItem(item));

        ArrayList<String> valuesList = new ArrayList<>();

        for(NamespacedKey tag : tags) {
            Object returnValue = dataContainer.get(tag, Utils.tagList.get(tag));
            if (returnValue instanceof String) {
                valuesList.add((String) returnValue);
            } else if (returnValue instanceof Integer) {
                valuesList.add(String.valueOf(returnValue));
            } else if (returnValue instanceof Float) {
                valuesList.add(String.valueOf(returnValue));
            } else if (returnValue instanceof Boolean) {
                valuesList.add(String.valueOf(returnValue));
            }
        }
        this.values = valuesList;

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

    public ArrayList<NamespacedKey> getTags() {
        return tags;
    }

    public ArrayList<String> getTagsAsStrings(){
        ArrayList<String> tagStrings = new ArrayList<>();
        for (NamespacedKey tag : tags) {
            tagStrings.add(tag.value());
        }
        return tagStrings;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public CustomModelData getCustomModelData() {
        return customModelData;
    }
}
