package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Utils {
    public static String pluginTag = "<dark_gray>[<gold>SkysongItems<dark_gray>] ";
    public static NamespacedKey maxchargesKey = new NamespacedKey(getPlugin(), "max_charges");
    public static NamespacedKey chargesKey = new NamespacedKey(getPlugin(), "charges");
    public static NamespacedKey addablechargesKey = new NamespacedKey(getPlugin(), "can_add_charges");
    public static NamespacedKey versionKey = new NamespacedKey(getPlugin(), "version");
    public static NamespacedKey loreKey = new NamespacedKey(getPlugin(), "lore");

    public static String ITEM_VERSION = "1.0";

    public static NamespacedKey propertiesKey = new NamespacedKey(getPlugin(), "properties");
    public HashMap<NamespacedKey,PersistentDataType> tagList = new HashMap<NamespacedKey,PersistentDataType>();

    public static ArrayList<Property> properties = new ArrayList<Property>();

    public void setupProperties(){
        List<String> set = getPlugin().getConfig().getConfigurationSection("properties").getKeys(false).stream().toList();
        for(String property : set){
            if(getPlugin().getConfig().getString("properties."+property+".type").equalsIgnoreCase("rename")){
                properties.add(new RenameProperty(property,
                        getPlugin().getConfig().getString("properties."+property+".type"),
                        getPlugin().getConfig().getString("properties."+property+".item"),
                        getPlugin().getConfig().getString("properties."+property+".name"),
                        getPlugin().getConfig().getString("properties."+property+".description"),
                        getPlugin().getConfig().getString("properties."+property+".tag")));
            }else if(getPlugin().getConfig().getString("properties."+property+".type").equalsIgnoreCase("printable")){
                ArrayList<String> tags = new ArrayList<String>();
                for(String tag : getPlugin().getConfig().getStringList("properties."+property+".tags")){
                    tags.add(tag);
                }
                properties.add(new PrintableProperty(property,
                        getPlugin().getConfig().getString("properties."+property+".type"),
                        getPlugin().getConfig().getString("properties."+property+".item"),
                        getPlugin().getConfig().getString("properties."+property+".name"),
                        getPlugin().getConfig().getString("properties."+property+".description"),
                        getPlugin().getConfig().getString("properties."+property+".printable"),
                        tags,
                        getPlugin().getConfig().getInt("properties."+property+".weight")));
            }else{
                properties.add(new Property(property,
                        getPlugin().getConfig().getString("properties."+property+".type"),
                        getPlugin().getConfig().getString("properties."+property+".item"),
                        getPlugin().getConfig().getString("properties."+property+".name"),
                        getPlugin().getConfig().getString("properties."+property+".description")));
            }
        }
    }

    public void setupTags(){
        List<String> set = getPlugin().getConfig().getConfigurationSection("tags").getKeys(false).stream().toList();
        for(String tag : set){
            PersistentDataType type = null;
            switch(getPlugin().getConfig().getString("tags."+tag+".type").toLowerCase()){
                case "int":
                    type = PersistentDataType.INTEGER;
                    break;
                case "string":
                    type = PersistentDataType.STRING;
                    break;
                case "float":
                    type = PersistentDataType.FLOAT;
                    break;
                case "bool":
                    type = PersistentDataType.BOOLEAN;
                    break;
            }
            tagList.put(new NamespacedKey(getPlugin(), getPlugin().getConfig().getString("tags."+tag+".namespace")), type);
        }
    }

    public static boolean isAProperty(String id){
        for(Property property : properties){
            if(property.getId().equalsIgnoreCase(id)){
                return true;
            }
        }
        return false;
    }

    public static Property getProperty(String id){
        for(Property property : properties){
            if(property.getId().equalsIgnoreCase(id)){
                return property;
            }
        }
        return null;
    }
}
