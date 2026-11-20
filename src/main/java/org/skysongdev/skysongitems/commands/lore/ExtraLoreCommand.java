package org.skysongdev.skysongitems.commands.lore;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ExtraLore;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si extralore}: shows or clears the lore lines other plugins put on the item in
 * the player's main hand, through {@code lore().setExtra}.
 *
 * <ul>
 *     <li>{@code /si extralore} or {@code /si extralore list} shows each plugin's lines and where they go</li>
 *     <li>{@code /si extralore clear <plugin>} removes one plugin's lines</li>
 *     <li>{@code /si extralore clear all} removes every plugin's lines</li>
 * </ul>
 */
public class ExtraLoreCommand implements TabExecutor {
    private static final String LIST = "list";
    private static final String CLEAR = "clear";
    private static final String ALL = "all";
    // MiniMessage would read the placeholder as a tag, so its bracket is escaped.
    private static final String USAGE = "Usage: /si extralore [list | clear \\<plugin|all>]";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            send(player, "You must hold an item in your main hand to use this command.");
            return true;
        }
        String action = args.length == 0 ? LIST : args[0].toLowerCase(Locale.ROOT);
        if (LIST.equals(action)) {
            list(player, ExtraLore.read(item.getPersistentDataContainer()));
        } else if (CLEAR.equals(action) && args.length == 2) {
            clear(player, item, args[1].toLowerCase(Locale.ROOT));
        } else {
            send(player, USAGE);
        }
        return true;
    }

    private static void list(Player player, ExtraLore extras) {
        if (extras.isEmpty()) {
            send(player, "No plugin has put lore lines on this item.");
            return;
        }
        for (ExtraLore.Entry entry : extras.entries()) {
            send(player, "<gold>" + entry.owner() + "<gray> (" + entry.position() + "):");
            entry.lines().forEach(line -> player.sendMessage(Component.text("  ").append(line)));
        }
    }

    private static void clear(Player player, ItemStack item, String owner) {
        ItemMeta meta = item.getItemMeta();
        List<String> owners = ALL.equals(owner) ? ownersOn(item) : List.of(owner);
        boolean removed = false;
        for (String each : owners) {
            removed |= ExtraLore.remove(meta.getPersistentDataContainer(), each);
        }
        if (!removed) {
            send(player, "<red>No lore lines from " + owner + " on this item.");
            return;
        }
        item.setItemMeta(meta);
        ItemLoreRenderer.redraw(item);
        player.getInventory().setItemInMainHand(item);
        send(player, "<green>Removed the lore lines from " + owner + ".");
    }

    private static List<String> ownersOn(ItemStack item) {
        return ExtraLore.read(item.getPersistentDataContainer()).entries().stream().map(ExtraLore.Entry::owner).toList();
    }

    private static void send(Player player, String message) {
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                                      @NotNull String[] args) {
        if (args.length == 1) {
            return startingWith(List.of(LIST, CLEAR), args[0]);
        }
        if (args.length == 2 && CLEAR.equalsIgnoreCase(args[0]) && sender instanceof Player player) {
            List<String> options = new ArrayList<>(ownersOn(player.getInventory().getItemInMainHand()));
            options.add(ALL);
            return startingWith(options, args[1]);
        }
        return List.of();
    }

    private static List<String> startingWith(List<String> options, String prefix) {
        return options.stream().filter(option -> option.startsWith(prefix.toLowerCase(Locale.ROOT))).toList();
    }
}
