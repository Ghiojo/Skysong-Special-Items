package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Utils {
    public static String pluginTag = "<dark_gray>[<gold>SkysongItems<dark_gray>] <gray>";
    public static NamespacedKey maxchargesKey = new NamespacedKey(getPlugin(), "max_charges");
    public static NamespacedKey chargesKey = new NamespacedKey(getPlugin(), "charges");
    public static NamespacedKey addablechargesKey = new NamespacedKey(getPlugin(), "can_add_charges");
    public static NamespacedKey versionKey = new NamespacedKey(getPlugin(), "version");
    public static NamespacedKey loreKey = new NamespacedKey(getPlugin(), "lore");
    public static NamespacedKey nameKey = new NamespacedKey(getPlugin(), "name");

    public static String ITEM_VERSION = "1.0";

    public static NamespacedKey propertiesKey = new NamespacedKey(getPlugin(), "properties");
    public static HashMap<NamespacedKey,PersistentDataType> tagList = new HashMap<NamespacedKey,PersistentDataType>();

    public static ArrayList<Property> properties = new ArrayList<Property>();
    public static ArrayList<String> propertyTypes = new ArrayList<String>();

    public void setupProperties(){
        properties.clear();
        Logger logger = getPlugin().getLogger();
        if (getPlugin().getConfig().getConfigurationSection("properties") == null) {
            logger.warning("No properties section found in the configuration.");
            return;
        }
        List<String> set = getPlugin().getConfig().getConfigurationSection("properties").getKeys(false).stream().toList();
        for (String property : set) {
            logger.warning("Adding property: " + property);
            String type = getPlugin().getConfig().getString("properties." + property + ".type");
            String proptype = getPlugin().getConfig().getString("properties." + property + ".proptype");
            String item = getPlugin().getConfig().getString("properties." + property + ".item");
            String name = getPlugin().getConfig().getString("properties." + property + ".name");
            String description = getPlugin().getConfig().getString("properties." + property + ".description");

            if (type.equalsIgnoreCase("rename")) {
                String tag = getPlugin().getConfig().getString("properties." + property + ".tag");
                properties.add(new RenameProperty(property, proptype, item, name, description, tag));
                logger.info("Added RenameProperty: " + property);
            } else if (type.equalsIgnoreCase("printable")) {
                String printable = getPlugin().getConfig().getString("properties." + property + ".printable");
                ArrayList<String> tags = new ArrayList<>(getPlugin().getConfig().getStringList("properties." + property + ".valuetags"));
                int weight = getPlugin().getConfig().getInt("properties." + property + ".weight");
                properties.add(new PrintableProperty(property, proptype, item, name, description, printable, tags, weight));
                logger.info("Added PrintableProperty: " + property);
            } else {
                properties.add(new Property(property, proptype, item, name, description));
                logger.info("Added Property: " + property);
            }
            if(!propertyTypes.contains(proptype)){
                propertyTypes.add(proptype);
            }
        }
        logger.info("Total properties added: " + properties.size());
    }

    public void setupTags(){
        tagList.clear();
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

    public static boolean isATag(String id){
        for(NamespacedKey key : getPlugin().getUtils().tagList.keySet()){
            if(key.getKey().equalsIgnoreCase(id)){
                return true;
            }
        }
        return false;
    }

    public static NamespacedKey getTag(String id){
        for(NamespacedKey key : getPlugin().getUtils().tagList.keySet()){
            if(key.getKey().equalsIgnoreCase(id)){
                return key;
            }
        }
        return null;
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
