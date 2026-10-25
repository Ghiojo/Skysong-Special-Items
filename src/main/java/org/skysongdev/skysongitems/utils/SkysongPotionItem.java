package org.skysongdev.skysongitems.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;

public class SkysongPotionItem extends SkysongItem{
    Color color;

    /** Adds the potion colour to an item loaded from a file. */
    public SkysongPotionItem(SkysongItem base, Color color) {
        super(base);
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
