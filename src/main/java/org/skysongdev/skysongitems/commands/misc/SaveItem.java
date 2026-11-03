package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.SkysongItem;
import org.skysongdev.skysongitems.utils.SkysongPotionItem;
import org.skysongdev.skysongitems.utils.SkysongSkullItem;

import java.util.List;
import java.util.Objects;

/**
 * {@code /si saveitem [folder]}: saves the held item as a template in GeneratedItems, or in
 * a subfolder of it such as {@code weapons} or {@code weapons/swords}.
 */
public class SaveItem implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!commandSender.hasPermission("skysongitems.save")) {
            send(commandSender, "<red>You do not have permission to use this command.");
            return false;
        }
        if(!(commandSender instanceof Player player))
            return false;
        if(strings.length > 1) {
            send(player, "<red>Usage: /si saveitem [folder]");
            return true;
        }
        String folder = strings.length == 0 ? "" : strings[0];
        if(!ItemTemplateRepository.isValidFolder(folder)) {
            send(player, "<red>Folder names may only use a-z, 0-9, _ and -, with / between folders.");
            return true;
        }
        if(player.getInventory().getItemInMainHand().getType().isAir()) {
            send(player, "<red>Please hold an item in your main hand!");
            return false;
        }
        updateIfOutdated(player);

        String id = ItemTemplateRepository.save(toSkysongItem(player.getInventory().getItemInMainHand()), folder);

        send(player, "<green>Item saved successfully as " + id + "!");
        SkysongItems.getPlugin().fixFilePaths();
        return true;
    }

    /** Suggests the existing subfolders of GeneratedItems for the folder argument. */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length != 1)
            return List.of();
        return ItemTemplateRepository.savedItemFolders().stream()
                .filter(folder -> folder.startsWith(strings[0]))
                .toList();
    }

    private static void updateIfOutdated(Player player) {
        String version = player.getInventory().getItemInMainHand().getPersistentDataContainer()
                .get(ItemKeys.VERSION, PersistentDataType.STRING);
        if(!Objects.equals(version, Constants.ITEM_VERSION)) {
            SkysongItems.getPlugin().getUpdater().UpdateItem(player);
        }
    }

    private static SkysongItem toSkysongItem(ItemStack held) {
        Material type = held.getType();
        if(type == Material.PLAYER_HEAD)
            return new SkysongSkullItem(held);
        if(type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION)
            return new SkysongPotionItem(held);
        return new SkysongItem(held);
    }

    private static void send(CommandSender receiver, String message) {
        receiver.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }
}
