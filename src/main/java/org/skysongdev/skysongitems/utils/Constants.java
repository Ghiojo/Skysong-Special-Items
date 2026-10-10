package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.SkysongItems;

import java.util.ArrayList;
import java.util.HashMap;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Constants {
    public static String pluginTag = "<dark_gray>[<gold>SkysongItems<dark_gray>] <gray>";
    public static NamespacedKey maxchargesKey = new NamespacedKey(getPlugin(), "max_charges");
    public static NamespacedKey chargesKey = new NamespacedKey(getPlugin(), "charges");
    public static NamespacedKey addablechargesKey = new NamespacedKey(getPlugin(), "can_add_charges");
    public static NamespacedKey versionKey = new NamespacedKey(getPlugin(), "version");
    public static NamespacedKey loreKey = new NamespacedKey(getPlugin(), "lore");
    public static NamespacedKey nameKey = new NamespacedKey(getPlugin(), "name");
    public static NamespacedKey boundKey = new NamespacedKey(SkysongItems.getPlugin(), "bound_book");
    public static NamespacedKey permissionKey = new NamespacedKey(SkysongItems.getPlugin(), "permission");
    public static NamespacedKey furnitureKey = new NamespacedKey("furniturelib", "model");
    public static NamespacedKey isShiftRightclickableKey = new NamespacedKey(getPlugin(), "is_shift_rightclickable");
    public static NamespacedKey materialIdKey = new NamespacedKey(getPlugin(), "material_id");
    public static NamespacedKey onConsumptionKey = new NamespacedKey(getPlugin(), "on_consumption");
    public static NamespacedKey dyeColorKey = new NamespacedKey(getPlugin(), "dye_color");
    public static String ITEM_VERSION = "1.1";
    public static NamespacedKey propertiesKey = new NamespacedKey(getPlugin(), "properties");
    public static HashMap<NamespacedKey, PersistentDataType> tagList = new HashMap<NamespacedKey,PersistentDataType>();
    public static ArrayList<Property> properties = new ArrayList<Property>();
    public static ArrayList<String> propertyTypes = new ArrayList<String>();
    public static NamespacedKey deletedKey = new NamespacedKey(getPlugin(), "deleted_lines");
    public static HashMap<String, String> materialPermissions = new HashMap<String, String>();
    public static ArrayList<String> itemFileNames = new ArrayList<>();
}
