package org.skysongdev.skysongitems.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Utils {
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

        TagRegistry tagRegistry = getPlugin().getTagRegistry();
        if(!tags.isEmpty())
            for(int i = 0; i < tags.size(); i++){
                if(!tagRegistry.contains(tags.get(i))){
                    continue;
                }
                NamespacedKey tag = tagRegistry.getKey(tags.get(i));
                PersistentDataType<?, ?> tagType = tagRegistry.getType(tag);
                if(PersistentDataType.BOOLEAN.equals(tagType)){
                    boolean value = Boolean.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(tag, PersistentDataType.BOOLEAN, value);
                }
                else if(PersistentDataType.INTEGER.equals(tagType)){
                    int value = Integer.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(tag, PersistentDataType.INTEGER, value);
                }
                else if(PersistentDataType.FLOAT.equals(tagType)){
                    float value = Float.valueOf(values.get(i));
                    meta.getPersistentDataContainer().set(tag, PersistentDataType.FLOAT, value);
                }
                else if(PersistentDataType.STRING.equals(tagType)){
                    String value = values.get(i);
                    meta.getPersistentDataContainer().set(tag, PersistentDataType.STRING, value);
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

    /** Grants or revokes a permission item's permission. Does nothing without LuckPerms. */
    public static void updatePlayerPermissions(Player player, boolean add, String permission) {
        getPlugin().getLuckPerms()
                .ifPresent(luckPerms -> luckPerms.updatePermission(player.getUniqueId(), add, permission));
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
                Property bufferProp = getPlugin().getPropertyRegistry().get(property);
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
                Property bufferProp = getPlugin().getPropertyRegistry().get(property);
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
            SkysongItem skysongItem = ItemTemplateRepository.load(itemFile);
            returnItems.add(getPlugin().getUtils().GenerateSkysongItem(skysongItem));
        }
    }
}
