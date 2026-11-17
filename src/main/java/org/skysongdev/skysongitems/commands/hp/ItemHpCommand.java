package org.skysongdev.skysongitems.commands.hp;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.commands.timer.LaterEdits;
import org.skysongdev.skysongitems.commands.timer.TimingArgs;
import org.skysongdev.skysongitems.timer.TimerAction;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.List;
import java.util.Locale;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si hp}: changes the HP of the held item. HP works like charges, except
 * that players cannot restore it: everyone may {@code reduce} it, and only staff
 * may {@code set}, {@code setmax}, {@code add} or {@code remove} it. Staff may also
 * make any of these changes later with {@code after <time>}, or again and again with
 * {@code every <time>}, and add or reduce HP for a while with a time on its own, such as
 * {@code add 5 24h}: the same amount is taken back off once the time has passed.
 */
public class ItemHpCommand implements TabExecutor {
    /** The permission staff need to restore, set or remove an item's HP. */
    public static final String STAFF_PERMISSION = "skysongspecialitems.cit";

    private static final String REDUCE = "reduce";
    private static final String REMOVE = "remove";
    private static final List<String> STAFF_SUBCOMMANDS = List.of("set", "setmax", "add", REDUCE, REMOVE);
    private static final String USAGE = "/si hp (" + String.join("|", STAFF_SUBCOMMANDS)
            + ") (amount) [time | after <time> | every <time>]";
    private static final List<String> PLAYER_SUBCOMMANDS = List.of(REDUCE);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        String subcommand = allowedSubcommand(sender, args);
        if (subcommand == null) {
            return true;
        }
        Player player = (Player) sender;
        int ownCount = REMOVE.equals(subcommand) ? 1 : 2;
        if (TimingArgs.refusedForPlayer(player, args, ownCount)) {
            return true;
        }
        TimingArgs timing = TimingArgs.readOrRefuse(player, args, ownCount, !undoable(subcommand));
        if (timing == null) {
            return true;
        }
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            send(sender, "<red>Please hold the item you wish to edit!");
            return true;
        }
        if (REMOVE.equals(subcommand)) {
            remove(player, item, timing);
        } else {
            editAmount(player, item, new Edit(subcommand, args, timing));
        }
        player.updateInventory();
        return true;
    }

    /** The subcommand typed, or null when the sender was told it can't be run. */
    private static @Nullable String allowedSubcommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            send(sender, "<red>Usage: " + USAGE);
            return null;
        }
        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (!subcommandsFor(sender).contains(subcommand)) {
            send(sender, STAFF_SUBCOMMANDS.contains(subcommand)
                    ? "You don't have permissions to do that command!"
                    : "<red>Unknown subcommand! Usage: " + USAGE);
            return null;
        }
        return subcommand;
    }

    /** A subcommand that takes an amount, as typed. */
    private record Edit(String subcommand, String[] args, TimingArgs timing) {
    }

    private static void remove(Player player, ItemStack item, TimingArgs timing) {
        if (timing.isLater()) {
            LaterEdits.schedule(player, item, timing, BuiltInTimerActions.REMOVE_HP, null, null);
        } else {
            getPlugin().getItemEditor().forCommand(player).removeItemHp(item);
        }
    }

    /** Runs a subcommand that takes an amount, now or on a timer, telling the player why when it fails. */
    private static void editAmount(Player player, ItemStack item, Edit edit) {
        Integer amount = edit.args().length < 2 ? null : parse(edit.args()[1]);
        if (amount == null) {
            send(player, "<red>Please type in a valid number!");
            return;
        }
        ItemEditor.Result result = edit.timing().isLater()
                ? schedule(player, item, edit, amount)
                : apply(getPlugin().getItemEditor().forCommand(player), item, edit.subcommand(), amount);
        if (ItemEditor.Result.NO_HP.equals(result)) {
            send(player, "<red>This item doesn't have HP!");
        } else if (ItemEditor.Result.INVALID_AMOUNT.equals(result)) {
            send(player, "<red>\"" + edit.args()[1] + "\" is out of range!");
        }
        if (ItemEditor.Result.DONE.equals(result) && edit.timing().isTimed()) {
            int added = REDUCE.equals(edit.subcommand()) ? -amount : amount;
            LaterEdits.undoLater(player, item, edit.timing(), BuiltInTimerActions.ADD_HP, null, String.valueOf(-added));
        }
    }

    /**
     * Whether the subcommand's change can be undone after a time on its own, such as
     * {@code add 5 24h}: adding and reducing can, by the same amount; setting and removing
     * can't, since the old value isn't kept.
     */
    private static boolean undoable(String subcommand) {
        return "add".equals(subcommand) || REDUCE.equals(subcommand);
    }

    private static ItemEditor.Result apply(ItemEditor editor, ItemStack item, String subcommand, int amount) {
        return switch (subcommand) {
            case "set" -> editor.setItemHp(item, amount);
            case "setmax" -> editor.setMaxItemHp(item, amount);
            case "add" -> editor.addItemHp(item, amount);
            default -> reduce(editor, item, amount);
        };
    }

    /** Stores the change as a timer; reducing is stored as adding a negative amount. */
    private static ItemEditor.Result schedule(Player player, ItemStack item, Edit edit, int amount) {
        if (REDUCE.equals(edit.subcommand()) && amount < 0) {
            return ItemEditor.Result.INVALID_AMOUNT;
        }
        TimerAction action = switch (edit.subcommand()) {
            case "set" -> BuiltInTimerActions.SET_HP;
            case "setmax" -> BuiltInTimerActions.SET_MAX_HP;
            default -> BuiltInTimerActions.ADD_HP;
        };
        int value = REDUCE.equals(edit.subcommand()) ? -amount : amount;
        return LaterEdits.schedule(player, item, edit.timing(), action, null, String.valueOf(value));
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
        if (args.length > 1) {
            return timeSuggestions(sender, args);
        }
        return subcommandsFor(sender).stream().filter(name -> name.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
    }

    /** Example times after the amount, or right after {@code remove}, for staff. */
    private static List<String> timeSuggestions(CommandSender sender, String[] args) {
        int ownCount = REMOVE.equalsIgnoreCase(args[0]) ? 1 : 2;
        if (args.length <= ownCount || !sender.hasPermission(STAFF_PERMISSION)) {
            return List.of();
        }
        return TimingArgs.suggestions(args, ownCount);
    }
}
