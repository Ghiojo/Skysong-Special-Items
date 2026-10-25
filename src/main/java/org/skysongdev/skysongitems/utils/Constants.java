package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants {
    public static String pluginTag = "<dark_gray>[<gold>SkysongItems<dark_gray>] <gray>";
    public static String ITEM_VERSION = "1.1";
    public static HashMap<NamespacedKey, PersistentDataType> tagList = new HashMap<NamespacedKey,PersistentDataType>();
    public static ArrayList<Property> properties = new ArrayList<Property>();
    public static ArrayList<String> propertyTypes = new ArrayList<String>();
    public static HashMap<String, String> materialPermissions = new HashMap<String, String>();
    public static ArrayList<String> itemFileNames = new ArrayList<>();
}
