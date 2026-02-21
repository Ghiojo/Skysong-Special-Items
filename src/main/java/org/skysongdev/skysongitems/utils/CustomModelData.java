package org.skysongdev.skysongitems.utils;

import org.bukkit.Color;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class CustomModelData {
    private List<Float> floats;
    private List<Boolean> flags;
    private List<String> strings;
    private List<Color> colors;

    public CustomModelData(List<Float> floats, List<Boolean> flags, List<String> strings, List<Color> colors) {
        this.floats = floats;
        this.flags = flags;
        this.strings = strings;
        this.colors = colors;
    }

    public CustomModelData(CustomModelDataComponent component){
        this.floats = component.getFloats();
        this.flags = component.getFlags();
        this.strings = component.getStrings();
        this.colors = component.getColors();
    }

    public List<Float> getFloats() {
        return floats;
    }
    public List<Boolean> getFlags() {
        return flags;
    }
    public List<String> getStrings() {
        return strings;
    }
    public List<Color> getColors() {
        return colors;
    }
}
