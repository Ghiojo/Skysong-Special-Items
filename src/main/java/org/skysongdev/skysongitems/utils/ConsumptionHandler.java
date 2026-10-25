package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * Runs the commands stored in an item's {@link ItemKeys#ON_CONSUMPTION} tag when
 * the item is consumed. Commands are separated by {@code ;}: {@code charge:amount:stop}
 * or {@code charge:amount:continue} takes charges off the item, and any other
 * command names a generated item to give back. A charge command written without
 * its amount takes one charge, and one written without its suffix acts as
 * {@code stop}, so a mistyped command never hands out the items after it.
 */
public final class ConsumptionHandler {
    private static final int AMOUNT_PART = 1;
    private static final int SUFFIX_PART = 2;

    private ConsumptionHandler() {
    }

    public static ArrayList<ItemStack> consume(ItemStack item){
        return consume(item, getPlugin().getLogger());
    }

    /**
     * Runs the item's onConsumption commands and returns the items the player should get back.
     * Takes the logger as a parameter so the command loop can be tested without an enabled plugin.
     */
    static ArrayList<ItemStack> consume(ItemStack item, Logger logger){
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
        int chargesToConsume = parseChargesToConsume(parts, logger);
        int currentCharges = item.getItemMeta().getPersistentDataContainer().get(ItemKeys.CHARGES, PersistentDataType.INTEGER);
        int newCharges = currentCharges - chargesToConsume;
        if (newCharges > 0) {
            ItemStack chargedItem = item.clone();
            chargedItem.setAmount(1);
            ItemMeta meta = chargedItem.getItemMeta();
            meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, newCharges);
            chargedItem.setItemMeta(meta);
            chargedItem = ItemLoreRenderer.refresh(chargedItem);
            returnItems.add(chargedItem);
        }
        return stopsAfterCharge(parts, logger) && newCharges > 0;
    }

    private static int parseChargesToConsume(String[] parts, Logger logger){
        if(parts.length <= AMOUNT_PART){
            logger.warning("Charge command has no amount of charges to consume. Consuming 1 charge.");
            return 1;
        }
        String amount = parts[AMOUNT_PART];
        try {
            return Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            logger.warning("Invalid number format for charges to consume: " + amount);
            return 1;
        }
    }

    /** Reads the command's stop/continue suffix, treating a missing one as "stop". */
    private static boolean stopsAfterCharge(String[] parts, Logger logger){
        if(parts.length <= SUFFIX_PART){
            logger.warning("Charge command has no :stop or :continue suffix. Treating it as :stop.");
            return true;
        }
        return parts[SUFFIX_PART].equals("stop");
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
            returnItems.add(ItemFactory.create(skysongItem));
        }
    }
}
