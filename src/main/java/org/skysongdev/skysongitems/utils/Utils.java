package org.skysongdev.skysongitems.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

    public static String ITEM_VERSION = "1.1";

    public static NamespacedKey propertiesKey = new NamespacedKey(getPlugin(), "properties");
    public static HashMap<NamespacedKey,PersistentDataType> tagList = new HashMap<NamespacedKey,PersistentDataType>();

    public static ArrayList<Property> properties = new ArrayList<Property>();
    public static ArrayList<String> propertyTypes = new ArrayList<String>();
    public static NamespacedKey deletedKey = new NamespacedKey(getPlugin(), "deleted_lines");

    public static ArrayList<String> itemFileNames = new ArrayList<>();

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

    public static ArrayList<NamespacedKey> getAllTagsInItem(ItemStack item){
        ArrayList<NamespacedKey> tags = new ArrayList<>();
        if(item == null || item.getType() == Material.AIR){
            return tags;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta == null){
            return tags;
        }
        for(NamespacedKey key : meta.getPersistentDataContainer().getKeys()){
            if(getPlugin().getUtils().tagList.containsKey(key)){
                tags.add(key);
            }
        }
        return tags;
    }

    public static Property getProperty(String id){
        for(Property property : properties){
            if(property.getId().equalsIgnoreCase(id)){
                return property;
            }
        }
        return null;
    }

    public ItemStack GenerateSkysongItem(SkysongItem item){
        if(Objects.equals(item.getType(), Material.PLAYER_HEAD.toString())) {
            SkysongSkullItem skullItem = (SkysongSkullItem) item;
            getPlugin().getLogger().info("Generating SkysongSkullItem with ID: " + skullItem.getId() + ", Player Name: " + skullItem.getPlayerName() + ", Texture: " + skullItem.getTexture());
            return GenerateSkysongItem(skullItem.getName(), skullItem.getLore(), skullItem.getType(), skullItem.getItemModel(), skullItem.getDeleted(), skullItem.getProperties(), skullItem.getTagsAsStrings(), skullItem.getValues(), skullItem.getId(), skullItem.getPlayerName(), skullItem.getTexture());
        }

        return GenerateSkysongItem(item.getName(), item.getLore(), item.getType(), item.getItemModel(), item.getDeleted(), item.getProperties(), item.getTagsAsStrings(), item.getValues(), null, null, null);
    }

    public ItemStack GenerateSkysongItem(String name, ArrayList<String> lore, String type, NamespacedKey custommodeldata, String deleted, String properties, ArrayList<String> tags, ArrayList<String> values, String id, String playerName, String texture) {
        Material mat = Material.getMaterial(type.toUpperCase());
        if (mat == null){
            mat = Material.BARRIER;
        }
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getPlugin().getMiniMessage().deserialize(TextParser.translateColorCodes(TextParser.translateHexColorCodes("&#", "", name))).decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, name);

        String bufferString = "";
        for(String string : lore){
            bufferString += ChatColor.COLOR_CHAR + string;
        }

        meta.getPersistentDataContainer().set(loreKey, PersistentDataType.STRING, bufferString.substring(1));

        meta.lore(TextParser.TranslateToLore(lore, deleted));

        if(deleted != null && !deleted.isEmpty()){
            meta.getPersistentDataContainer().set(deletedKey, PersistentDataType.STRING, deleted);
        }

        if(custommodeldata != null)
            meta.setItemModel(custommodeldata);

        getPlugin().getLogger().warning("custommodeldata: " + custommodeldata);

        if(properties != null && !properties.isEmpty())
            meta.getPersistentDataContainer().set(propertiesKey, PersistentDataType.STRING, properties);

        meta.getPersistentDataContainer().set(versionKey, PersistentDataType.STRING, ITEM_VERSION);

        if(!tags.isEmpty())
            for(int i = 0; i < tags.size(); i++){
                if(!isATag(tags.get(i))){
                    continue;
                }
                if(getPlugin().getUtils().tagList.get(getTag(tags.get(i))) == PersistentDataType.BOOLEAN){
                    boolean value = Boolean.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), getPlugin().getUtils().tagList.get(getTag(tags.get(i))), value);
                }
                else if(getPlugin().getUtils().tagList.get(getTag(tags.get(i))) == PersistentDataType.INTEGER){
                    int value = Integer.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), getPlugin().getUtils().tagList.get(getTag(tags.get(i))), value);
                }
                else if(getPlugin().getUtils().tagList.get(getTag(tags.get(i))) == PersistentDataType.FLOAT){
                    float value = Float.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), getPlugin().getUtils().tagList.get(getTag(tags.get(i))), value);
                }
                else if(getPlugin().getUtils().tagList.get(getTag(tags.get(i))) == PersistentDataType.STRING){
                    String value = values.get(i);
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), getPlugin().getUtils().tagList.get(getTag(tags.get(i))), value);
                } else {
                    getPlugin().getLogger().warning("Unknown tag type for tag: " + tags.get(i));
                }
            }

        if(id != null && !id.isEmpty()) {
            SkullMeta skullMeta = (SkullMeta) meta;
            PlayerProfile profile = getPlugin().getServer().createProfile(UUID.fromString(id), playerName);

            profile.setProperty(new ProfileProperty("textures", texture));

            skullMeta.setPlayerProfile(profile);

            item.setItemMeta(skullMeta);
            return item;
        }
        item.setItemMeta(meta);
        return item;
    }

    public static void GenerateItemFile(SkysongItem item){
        // Define the output directory
        File outputDir = getOrGenerateOutputFile();
        // Define the file for the specific item
        File itemFile = new File(outputDir, item.getFormattedName() + ".yml");

        // Create a YamlConfiguration object
        YamlConfiguration yaml = new YamlConfiguration();

        // Store the SkysongItem data in the YAML file
        yaml.set("name", item.getName());
        yaml.set("lore", item.getLore());
        yaml.set("type", item.getType());
        yaml.set("itemModel", item.getItemModel());
        yaml.set("deleted", item.getDeleted());
        yaml.set("properties", item.getProperties());
        yaml.set("tags", item.getTagsAsStrings());
        yaml.set("values", item.getValues());

        if(item instanceof SkysongSkullItem) {
            SkysongSkullItem skullItem = (SkysongSkullItem) item;
            yaml.set("id", skullItem.getId());
            yaml.set("playername", skullItem.getPlayerName());
            yaml.set("texture", skullItem.getTexture());
        }

        // Save the YAML file
        try {
            yaml.save(itemFile);
            getPlugin().getLogger().info("Item file saved: " + itemFile.getPath());
        } catch (IOException e) {
            getPlugin().getLogger().severe("Failed to save item file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static SkysongItem LoadSkysongItem(File file){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String name = yaml.getString("name");
        ArrayList<String> lore = new ArrayList<>(yaml.getStringList("lore"));
        String type = yaml.getString("type");
        NamespacedKey customModelData = (new NamespacedKey("skysongitems", yaml.getString("itemModel", "none")));
        if(customModelData.getKey().equals("none")){
            customModelData = null; // If no custom model data is specified, set it to null
        }
        String deleted = yaml.getString("deleted", "");
        String properties = yaml.getString("properties", "");
        ArrayList<String> tags = new ArrayList<>(yaml.getStringList("tags"));
        ArrayList<NamespacedKey> finalTags = new ArrayList<>();
        for (String tag : tags) {
            if (isATag(tag)) {
                finalTags.add(getTag(tag));
            }
        }
        ArrayList<String> values = new ArrayList<>(yaml.getStringList("values"));

        if(type.equals(Material.PLAYER_HEAD.toString())) {
            String id = yaml.getString("id", "");
            String playerName = yaml.getString("playername", "");
            String texture = yaml.getString("texture", "");
            return new SkysongSkullItem(name, lore, type, customModelData, deleted, properties, finalTags, values, id, playerName, texture);
        }

        return new SkysongItem(name, lore, type, customModelData, deleted, properties, finalTags, values);
    }

    public static File getOrGenerateOutputFile(){
        File outputDir = new File(getPlugin().getDataFolder(), "GeneratedItems");
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create the directory if it doesn't exist
        }
        return outputDir;
    }
}
