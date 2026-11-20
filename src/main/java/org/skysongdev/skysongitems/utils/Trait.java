package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
public class Trait {
    private String id;
    private String type;
    private ItemStack item;
    private String name;
    private String description;
    private boolean keptOnTransform;

    public Trait(String id, String type, String item, String name, String description) {
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

    /** Whether the trait stays on an item when the item transforms into another form. */
    public boolean isKeptOnTransform() {
        return keptOnTransform;
    }

    public void setKeptOnTransform(boolean keptOnTransform) {
        this.keptOnTransform = keptOnTransform;
    }
}
