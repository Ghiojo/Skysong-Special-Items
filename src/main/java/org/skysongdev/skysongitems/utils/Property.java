package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
public class Property {
    private String id;
    private String type;
    private ItemStack item;
    private String name;
    private String description;

    public Property(String id, String type, String item, String name, String description) {
        this.id = id;
        this.type = type;
        Material mat = Material.getMaterial(item);
        if (mat != null) {
            this.item = new ItemStack(mat);
        } else {
            this.item = new ItemStack(Material.BARRIER);
        }
        this.name = name;
        this.description = description;
    }
    public String getId() {
        return id;
    }
    public String getPropType() {
        return type;
    }
    public ItemStack getItem() {
        return item;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

    public String getType(){
        return "base";
    }
}
