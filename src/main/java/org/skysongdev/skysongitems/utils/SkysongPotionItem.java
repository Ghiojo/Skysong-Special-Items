package org.skysongdev.skysongitems.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class SkysongPotionItem extends SkysongItem{
    Color color;

    public SkysongPotionItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, Map<NamespacedKey, String> tagValues, Color color) {
        super(name, lore, type, itemModel, deleted, properties, tagValues);
        this.color = color;
    }

    public SkysongPotionItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, Map<NamespacedKey, String> tagValues, CustomModelData customModelData, Color color) {
        super(name, lore, type, itemModel, deleted, properties, tagValues, customModelData);
        this.color = color;
    }

    public SkysongPotionItem(ItemStack item) {
        super(item);

        PotionContents contents = item.getData(DataComponentTypes.POTION_CONTENTS);
        if(contents != null){
            this.color = contents.customColor();
        }
    }

    public Color getColor() {
        return color;
    }
}
