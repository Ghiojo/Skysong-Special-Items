package org.skysongdev.skysongitems.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Utils {
    public void setupProperties(){
        Constants.properties.clear();
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

            switch(type.toLowerCase()){
                case "rename":
                    String tag = getPlugin().getConfig().getString("properties." + property + ".tag");
                    Constants.properties.add(new RenameProperty(property, proptype, item, name, description, tag));
                    logger.info("Added RenameProperty: " + property);
                    break;
                case "printable":
                    String printable = getPlugin().getConfig().getString("properties." + property + ".printable");
                    ArrayList<String> tags = new ArrayList<>(getPlugin().getConfig().getStringList("properties." + property + ".valuetags"));
                    int weight = getPlugin().getConfig().getInt("properties." + property + ".weight");
                    Constants.properties.add(new PrintableProperty(property, proptype, item, name, description, printable, tags, weight));
                    logger.info("Added PrintableProperty: " + property);
                    break;
                case "breweryprintable":
                    String brewPrintable = getPlugin().getConfig().getString("properties." + property + ".printable");
                    ArrayList<String> brewTags = new ArrayList<>(getPlugin().getConfig().getStringList("properties." + property + ".valuetags"));
                    int brewWeight = getPlugin().getConfig().getInt("properties." + property + ".weight");
                    Constants.properties.add(new BreweryPrintableProperty(property, proptype, item, name, description, brewPrintable, brewTags, brewWeight));
                    logger.info("Added BreweryPrintableProperty: " + property);
                    break;
                default:
                    Constants.properties.add(new Property(property, proptype, item, name, description));
                    logger.info("Added Property: " + property);

            }

            if(!Constants.propertyTypes.contains(proptype)){
                Constants.propertyTypes.add(proptype);
            }
        }
        logger.info("Total properties added: " + Constants.properties.size());
    }

    public void setupTags(){
        Constants.tagList.clear();
        List<String> set = getPlugin().getConfig().getConfigurationSection("tags").getKeys(false).stream().toList();
        addStaticTags();
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
            Constants.tagList.put(ItemKeys.named(getPlugin().getConfig().getString("tags."+tag+".namespace")), type);
        }
    }

    public void addStaticTags(){
        Constants.tagList.put(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        Constants.tagList.put(ItemKeys.CHARGES, PersistentDataType.INTEGER);
        Constants.tagList.put(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN);
        Constants.tagList.put(ItemKeys.VERSION, PersistentDataType.STRING);
        Constants.tagList.put(ItemKeys.LORE, PersistentDataType.STRING);
        Constants.tagList.put(ItemKeys.NAME, PersistentDataType.STRING);
        Constants.tagList.put(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER);
        Constants.tagList.put(ItemKeys.PERMISSION, PersistentDataType.STRING);
        Constants.tagList.put(ItemKeys.FURNITURE_MODEL, PersistentDataType.STRING);
        Constants.tagList.put(ItemKeys.SHIFT_RIGHT_CLICKABLE, PersistentDataType.BOOLEAN);
        Constants.tagList.put(ItemKeys.MATERIAL_ID, PersistentDataType.STRING);
        Constants.tagList.put(ItemKeys.ON_CONSUMPTION, PersistentDataType.STRING);
        Constants.tagList.put(ItemKeys.DYE_COLOR, PersistentDataType.INTEGER);
    }

    public static boolean isAProperty(String id){
        for(Property property : Constants.properties){
            if(property.getId().equalsIgnoreCase(id)){
                return true;
            }
        }
        return false;
    }

    public static boolean isATag(String id){
        for(NamespacedKey key : Constants.tagList.keySet()){
            if(key.getKey().equalsIgnoreCase(id)){
                return true;
            }
        }
        return false;
    }

    public static NamespacedKey getTag(String id){
        for(NamespacedKey key : Constants.tagList.keySet()){
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
            if(Constants.tagList.containsKey(key)){
                tags.add(key);
            }
        }
        return tags;
    }

    public static Property getProperty(String id){
        for(Property property : Constants.properties){
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
            return GenerateSkysongItem(skullItem.getName(), skullItem.getLore(), skullItem.getType(), skullItem.getItemModel(), skullItem.getDeleted(), skullItem.getProperties(), skullItem.getTagsAsStrings(), skullItem.getValues(), skullItem.getId(), skullItem.getPlayerName(), skullItem.getTexture(), item.getCustomModelData(), null);
        }
        if(Objects.equals(item.getType(), Material.POTION.toString()) || Objects.equals(item.getType(), Material.SPLASH_POTION.toString()) || Objects.equals(item.getType(), Material.LINGERING_POTION.toString())){
            Color color = ((SkysongPotionItem) item).getColor();
            getPlugin().getLogger().info("Generating SkysongPotionItem with color: " + color);
            return GenerateSkysongItem(item.getName(), item.getLore(), item.getType(), item.getItemModel(), item.getDeleted(), item.getProperties(), item.getTagsAsStrings(), item.getValues(), null, null, null, item.getCustomModelData(), color);
        }

        return GenerateSkysongItem(item.getName(), item.getLore(), item.getType(), item.getItemModel(), item.getDeleted(), item.getProperties(), item.getTagsAsStrings(), item.getValues(), null, null, null, item.getCustomModelData(), null);
    }

    public ItemStack GenerateSkysongItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, ArrayList<String> tags, ArrayList<String> values, String id, String playerName, String texture, CustomModelData customModelData, Color potionColor) {
        Material mat = Material.getMaterial(type.toUpperCase());
        if (mat == null){
            mat = Material.BARRIER;
        }
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getPlugin().getMiniMessage().deserialize(TextParser.translateColorCodes(TextParser.translateHexColorCodes("&#", "", name))).decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, name);

        ItemData data = ItemData.from(meta);
        data.setLoreSections(lore);

        meta.lore(TextParser.TranslateToLore(lore, deleted));

        if(deleted != null && !deleted.isEmpty()){
            data.setDeletedLines(deleted);
        }

        if(itemModel != null)
            meta.setItemModel(itemModel);

        getPlugin().getLogger().warning("custommodeldata: " + itemModel);

        if(properties != null && !properties.isEmpty())
            data.setPropertiesText(properties);

        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);

        if(!tags.isEmpty())
            for(int i = 0; i < tags.size(); i++){
                if(!isATag(tags.get(i))){
                    continue;
                }
                if(Constants.tagList.get(getTag(tags.get(i))) == PersistentDataType.BOOLEAN){
                    boolean value = Boolean.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), Constants.tagList.get(getTag(tags.get(i))), value);
                }
                else if(Constants.tagList.get(getTag(tags.get(i))) == PersistentDataType.INTEGER){
                    int value = Integer.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), Constants.tagList.get(getTag(tags.get(i))), value);
                }
                else if(Constants.tagList.get(getTag(tags.get(i))) == PersistentDataType.FLOAT){
                    float value = Float.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), Constants.tagList.get(getTag(tags.get(i))), value);
                }
                else if(Constants.tagList.get(getTag(tags.get(i))) == PersistentDataType.STRING){
                    String value = values.get(i);
                    meta.getPersistentDataContainer().set(getTag(tags.get(i)), Constants.tagList.get(getTag(tags.get(i))), value);
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
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, io.papermc.paper.datacomponent.item.CustomModelData.customModelData().addStrings(customModelData.getStrings()).addFloats(customModelData.getFloats()).addFlags(customModelData.getFlags()).addColors(customModelData.getColors()));

        if(potionColor != null) {
            PotionContents contents = item.getData(DataComponentTypes.POTION_CONTENTS);
            PotionContents newContents = PotionContents.potionContents().potion(contents.potion()).customColor(potionColor).build();

            item.setData(DataComponentTypes.POTION_CONTENTS, newContents);
        }
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
        if(item.getItemModel() != null)
            yaml.set("itemModel", item.getItemModel().getNamespace() + ":" + item.getItemModel().getKey());
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

        if(item instanceof SkysongPotionItem) {
            SkysongPotionItem potionItem = (SkysongPotionItem) item;
            yaml.set("potionColor", potionItem.getColor().asRGB());
        }

        if(item.getCustomModelData() != null) {
            yaml.set("customModelData.floats", item.getCustomModelData().getFloats());
            yaml.set("customModelData.flags", item.getCustomModelData().getFlags());
            yaml.set("customModelData.strings", item.getCustomModelData().getStrings());
            List<String> rgbList = new ArrayList<>();
            for (Color color : item.getCustomModelData().getColors()) {
                rgbList.add(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
            }
            yaml.set("customModelData.colors", rgbList);
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
        ArrayList<String> model = new ArrayList<>(Arrays.asList(yaml.getString("itemModel", "").split(":")));
        NamespacedKey itemModel;
        if(!(model.size() < 2 || model.get(0).isEmpty() || model.get(1).isEmpty())) {
            itemModel = (new NamespacedKey(model.get(0), model.get(1)));
        }
        else
            itemModel = null; // If no custom model data is specified, set it to null

        List<String> rgbList = yaml.getStringList("customModelData.colors");
        List<Color> parsedColors = new ArrayList<>();
        for (String rgb : rgbList) {
            String[] parts = rgb.split(",");
            if (parts.length == 3) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    parsedColors.add(Color.fromRGB(r, g, b));
                } catch (NumberFormatException e) {
                    getPlugin().getLogger().warning("Invalid RGB format in customModelData.colors: " + rgb);
                }
            } else {
                getPlugin().getLogger().warning("Invalid RGB entry in customModelData.colors: " + rgb);
            }
        }

        CustomModelData customModelData = new CustomModelData(yaml.getFloatList("customModelData.floats"), yaml.getBooleanList("customModelData.flags"), yaml.getStringList("customModelData.strings"), parsedColors);

        String deleted = yaml.getString("deleted", "");
        String properties = yaml.getString("properties", "");
        List<String> tagIds = yaml.getStringList("tags");
        List<String> values = yaml.getStringList("values");
        if (tagIds.size() != values.size()) {
            getPlugin().getLogger().warning("Item file " + file.getName() + " has " + tagIds.size()
                    + " tags but " + values.size() + " values; tags without a value are ignored.");
        }
        LinkedHashMap<NamespacedKey, String> tagValues = SkysongItem.pairTagValues(tagIds, values);

        if(type.equals(Material.PLAYER_HEAD.toString())) {
            String id = yaml.getString("id", "");
            String playerName = yaml.getString("playername", "");
            String texture = yaml.getString("texture", "");
            return new SkysongSkullItem(name, lore, type, itemModel, deleted, properties, tagValues, id, playerName, texture, customModelData);
        }

        if(type.equals(Material.POTION.toString()) || type.equals(Material.SPLASH_POTION.toString()) || type.equals(Material.LINGERING_POTION.toString())){
            Color color = Color.fromRGB(yaml.getInt("potionColor"));
            return new SkysongPotionItem(name, lore, type, itemModel, deleted, properties, tagValues, customModelData, color);
        }

        return new SkysongItem(name, lore, type, itemModel, deleted, properties, tagValues, customModelData);
    }

    /** Grants or revokes a permission item's permission. Does nothing without LuckPerms. */
    public static void updatePlayerPermissions(Player player, boolean add, String permission) {
        getPlugin().getLuckPerms()
                .ifPresent(luckPerms -> luckPerms.updatePermission(player.getUniqueId(), add, permission));
    }

    public static File getOrGenerateOutputFile(){
        File outputDir = new File(getPlugin().getDataFolder(), "GeneratedItems");
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create the directory if it doesn't exist
        }
        return outputDir;
    }

    public static ItemStack updateItemLore(ItemStack inputItem){
        if(inputItem == null || inputItem.getType() == Material.AIR){
            return null;
        }
        ItemStack item = inputItem;
        ItemMeta meta = item.getItemMeta();
        if(meta == null){
            return null;
        }
        ItemData data = ItemData.from(meta);
        List<Component> lore = TextParser.TranslateToLore(data.getLoreSections(), data.getDeletedLines());

        if(data.hasProperties()){
            lore.add(getPlugin().getMiniMessage().deserialize(""));

            //Update Printable Properties

            List<String> currentProperties = data.getProperties();
            StringBuilder returnMessage = new StringBuilder();
            ArrayList<PrintableProperty> organizedProperties = new ArrayList<PrintableProperty>();
            for(String property : currentProperties){
                Property bufferProp = Utils.getProperty(property);
                if(!(bufferProp instanceof PrintableProperty prop)){
                    continue;
                }
                if(organizedProperties.isEmpty())
                    organizedProperties.add(prop);
                else{
                    boolean added = false;
                    for(int i = 0; i < organizedProperties.size(); i++){
                        if(organizedProperties.get(i).getWeight() < prop.getWeight()){
                            organizedProperties.add(i, prop);
                            added = true;
                            break;
                        }
                    }
                    if(!added){
                        organizedProperties.add(prop);
                    }
                }
            }
            for (PrintableProperty property : organizedProperties) {
                if(property instanceof BreweryPrintableProperty brewProp){
                    // Brew scores come from TheBrewingProject; without it there is nothing to print.
                    if(getPlugin().getBrewingProject().isEmpty()){
                        continue;
                    }
                    if(brewProp.getBrewedPrinting(item) == null){
                        Bukkit.getLogger().warning("BreweryPrintableProperty " + brewProp.getId() + " returned null for brewed printing. Skipping.");
                        continue;
                    }
                    Bukkit.getLogger().warning("Adding lore for BreweryPrintableProperty " + brewProp.getId() + ": " + brewProp.getBrewedPrinting(item));
                    lore.add(brewProp.getBrewedPrinting(item));
                }
                else {
                    returnMessage.append(property.getParsedPrinting(item));
                    lore.add(getPlugin().getMiniMessage().deserialize(returnMessage.toString()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
                    returnMessage.replace(0, returnMessage.length(), "");
                }

            }

            //Update Rename Properties
            if(meta.getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING) != null){
                String newName = meta.getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING);
                newName = TextParser.translateHexColorCodes("&#", "", newName);
                newName = TextParser.translateColorCodes(newName);
                meta.displayName(getPlugin().getMiniMessage().deserialize(newName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
            }

            for(String property : currentProperties){
                Property bufferProp = Utils.getProperty(property);
                if(!(bufferProp instanceof RenameProperty)){
                    continue;
                }
                RenameProperty prop = (RenameProperty) bufferProp;

                if(item.getItemMeta().displayName() != null) {
                    String newName = meta.getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING);
                    newName = TextParser.translateHexColorCodes("&#", "", newName);
                    newName = TextParser.translateColorCodes(newName);
                    meta.displayName(getPlugin().getMiniMessage().deserialize(prop.getTag() + " " + newName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
                }
            }

            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSkysongItem(ItemStack item){
        if(item == null || item.getType() == Material.AIR){
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta == null){
            return false;
        }
        return meta.getPersistentDataContainer().has(ItemKeys.VERSION, PersistentDataType.STRING);
    }

    public void InitializeMaterialPermissions(){
        // This grabs a file from the plugin's data folder called "materials.yml"
        // and reads through it, saving the material id and the permission node required to
        // use it to a map. This is used for some plugin integrations to deny the ability for a player to
        // use a SkysongItem they shouldn't be able to.
        File materialFile = new File(getPlugin().getDataFolder(), "materials.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        if(!materialFile.exists()){
            try {
                materialFile.createNewFile();
                yaml.set("materials.IRON_INGOT", "skysongitems.use.iron");
                yaml.set("materials.DIAMOND", "skysongitems.use.diamond");
                yaml.save(materialFile);
            } catch (IOException e) {
                getPlugin().getLogger().severe("Failed to create materials.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }

        yaml = YamlConfiguration.loadConfiguration(materialFile);
        if (yaml.getConfigurationSection("materials") == null) {
            getPlugin().getLogger().warning("No materials section found in materials.yml.");
            return;
        }

        List<String> set = yaml.getConfigurationSection("materials").getKeys(false).stream().toList();
        for(String material : set){
            String permission = yaml.getString("materials." + material);
            if(permission != null){
                Constants.materialPermissions.put(material, permission);
                getPlugin().getLogger().info("Added material permission: " + material + " -> " + permission);
            } else {
                getPlugin().getLogger().warning("No permission found for material: " + material);
            }
        }
    }

    public static ArrayList<ItemStack> consumeItem(ItemStack item){
        return consumeItem(item, getPlugin().getLogger());
    }

    /**
     * Runs the item's onConsumption commands and returns the items the player should get back.
     * Takes the logger as a parameter so the command loop can be tested without an enabled plugin.
     */
    static ArrayList<ItemStack> consumeItem(ItemStack item, Logger logger){
        if(item == null || item.getType() == Material.AIR){
            return null;
        }
        ArrayList<ItemStack> returnItems = new ArrayList<>();
        for(String command : getConsumptionCommands(item)){
            String[] parts = command.split(":");
            boolean stopAfterCommand = false;
            if(parts[0].equals("charge")){
                stopAfterCommand = runChargeCommand(item, parts, returnItems, logger);
            } else {
                runReturnItemCommand(item, parts[0], returnItems);
            }
            if(stopAfterCommand){
                break;
            }
        }
        return returnItems;
    }

    private static String[] getConsumptionCommands(ItemStack item){
        if(!item.hasItemMeta()){
            return new String[0];
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(ItemKeys.ON_CONSUMPTION)){
            return new String[0];
        }
        return pdc.get(ItemKeys.ON_CONSUMPTION, PersistentDataType.STRING).split(";");
    }

    /**
     * Handles a "charge:amount:stop" command by taking charges off one item of the stack.
     * Returns true when the remaining commands should be skipped.
     */
    private static boolean runChargeCommand(ItemStack item, String[] parts, List<ItemStack> returnItems, Logger logger){
        if(!item.getItemMeta().getPersistentDataContainer().has(ItemKeys.CHARGES, PersistentDataType.INTEGER)){
            logger.warning("Item has onConsumption command to consume charges but no charges tag found. Skipping command.");
            return false;
        }
        if (item.getAmount() > 1) {
            ItemStack remainingItem = item.clone();
            remainingItem.setAmount(remainingItem.getAmount() - 1);
            returnItems.add(remainingItem);
        }
        int chargesToConsume = parseChargesToConsume(parts[1], logger);
        int currentCharges = item.getItemMeta().getPersistentDataContainer().get(ItemKeys.CHARGES, PersistentDataType.INTEGER);
        int newCharges = currentCharges - chargesToConsume;
        if (newCharges > 0) {
            ItemStack chargedItem = item.clone();
            chargedItem.setAmount(1);
            ItemMeta meta = chargedItem.getItemMeta();
            meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, newCharges);
            chargedItem.setItemMeta(meta);
            chargedItem = updateItemLore(chargedItem);
            returnItems.add(chargedItem);
        }
        return parts[2].equals("stop") && newCharges > 0;
    }

    private static int parseChargesToConsume(String amount, Logger logger){
        try {
            return Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            logger.warning("Invalid number format for charges to consume: " + amount);
            return 1;
        }
    }

    /** Handles a command naming a generated item: uses up one item of the stack and gives the named item. */
    private static void runReturnItemCommand(ItemStack item, String returnedItem, List<ItemStack> returnItems){
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() > 0) {
            returnItems.add(item);
        }

        File itemFile = getPlugin().getDataFolder().toPath().resolve("GeneratedItems").resolve(returnedItem + ".yml").toFile();
        if (itemFile.exists()) {
            SkysongItem skysongItem = LoadSkysongItem(itemFile);
            returnItems.add(getPlugin().getUtils().GenerateSkysongItem(skysongItem));
        }
    }
}
