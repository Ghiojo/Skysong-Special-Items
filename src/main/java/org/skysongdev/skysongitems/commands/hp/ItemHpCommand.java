package org.skysongdev.skysongitems.commands.hp;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.List;
import java.util.Locale;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si hp}: changes the HP of the held item. HP works like charges, except
 * that players cannot restore it: everyone may {@code reduce} it, and only staff
 * may {@code set}, {@code setmax}, {@code add} or {@code remove} it.
 */
public class ItemHpCommand implements TabExecutor {
    /** The permission staff need to restore, set or remove an item's HP. */
    public static final String STAFF_PERMISSION = "skysongspecialitems.cit";

    private static final String REDUCE = "reduce";
    private static final List<String> STAFF_SUBCOMMANDS = List.of("set", "setmax", "add", REDUCE, "remove");
    private static final List<String> PLAYER_SUBCOMMANDS = List.of(REDUCE);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (args.length < 1) {
            send(sender, "<red>Usage: /si hp (" + String.join("|", STAFF_SUBCOMMANDS) + ") (amount)");
            return true;
        }
        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (!subcommandsFor(sender).contains(subcommand)) {
            send(sender, STAFF_SUBCOMMANDS.contains(subcommand)
                    ? "You don't have permissions to do that command!"
                    : "<red>Unknown subcommand! Usage: /si hp (" + String.join("|", STAFF_SUBCOMMANDS) + ") (amount)");
            return true;
        }
        Player player = (Player) sender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            send(sender, "<red>Please hold the item you wish to edit!");
            return true;
        }
        ItemEditor editor = getPlugin().getItemEditor().forCommand(player);
        if ("remove".equals(subcommand)) {
            editor.removeItemHp(item);
        } else if (!editAmount(player, editor, item, subcommand, args)) {
            return true;
        }
        player.updateInventory();
        return true;
    }

    /** Runs a subcommand that takes an amount, telling the player why when it fails. */
    private static boolean editAmount(Player player, ItemEditor editor, ItemStack item, String subcommand,
                                      String[] args) {
        Integer amount = args.length < 2 ? null : parse(args[1]);
        if (amount == null) {
            send(player, "<red>Please type in a valid number!");
            return false;
        }
        ItemEditor.Result result = apply(editor, item, subcommand, amount);
        if (ItemEditor.Result.NO_HP.equals(result)) {
            send(player, "<red>This item doesn't have HP!");
        } else if (ItemEditor.Result.INVALID_AMOUNT.equals(result)) {
            send(player, "<red>\"" + args[1] + "\" is out of range!");
        }
        return ItemEditor.Result.DONE.equals(result);
    }

    private static ItemEditor.Result apply(ItemEditor editor, ItemStack item, String subcommand, int amount) {
        return switch (subcommand) {
            case "set" -> editor.setItemHp(item, amount);
            case "setmax" -> editor.setMaxItemHp(item, amount);
            case "add" -> editor.addItemHp(item, amount);
            default -> reduce(editor, item, amount);
        };
    }

    /** Takes HP off the item; a negative amount would restore it, so it is refused. */
    private static ItemEditor.Result reduce(ItemEditor editor, ItemStack item, int amount) {
        if (amount < 0) {
            return ItemEditor.Result.INVALID_AMOUNT;
        }
        return editor.addItemHp(item, -amount);
    }

    private static Integer parse(String amount) {
        try {
            return Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<String> subcommandsFor(CommandSender sender) {
        return sender.hasPermission(STAFF_PERMISSION) ? STAFF_SUBCOMMANDS : PLAYER_SUBCOMMANDS;
    }

    private static void send(CommandSender sender, String message) {
        sender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                                      @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return subcommandsFor(sender).stream().filter(name -> name.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
    }
}
