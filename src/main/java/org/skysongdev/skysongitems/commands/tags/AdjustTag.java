package org.skysongdev.skysongitems.commands.tags;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.commands.timer.LaterEdits;
import org.skysongdev.skysongitems.commands.timer.TimingArgs;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.TagRegistry;
import org.skysongdev.skysongitems.utils.TagTypes;

import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si tag add <tag> <amount>} and {@code /si tag reduce <tag> <amount>}: counts a
 * number tag on the held item up or down. A tag the item doesn't have counts as 0, and the
 * tag never goes below 0.
 */
public class AdjustTag implements TabExecutor {
    private final boolean reduce;

    /** @param reduce whether this is {@code reduce}, which takes the amount off */
    public AdjustTag(boolean reduce) {
        this.reduce = reduce;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 2) {
            send(player, "Usage: /si tag " + name() + " <tag> <amount> [after <time> | every <time>]");
            return true;
        }
        TimingArgs timing = TimingArgs.readOrRefuse(player, args, 2, true);
        if (timing == null) {
            return true;
        }
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            send(player, "You must hold an item in your main hand to use this command.");
            return true;
        }
        String amount = reduce ? negated(args[1]) : args[1];
        ItemEditor.Result result = timing.isLater()
                ? LaterEdits.schedule(player, item, timing, BuiltInTimerActions.ADD_TAG, args[0], amount)
                : getPlugin().getItemEditor().forCommand(player).addTag(item, args[0], amount);
        if (!ItemEditor.Result.DONE.equals(result)) {
            send(player, "<red>" + failureMessage(result));
            return true;
        }
        if (timing.isLater()) {
            return true;
        }
        getPlugin().syncPermissionItems(player);
        send(player, "<green>The tag " + args[0] + " is now " + currentValue(item, args[0]) + ".");
        return true;
    }

    private String name() {
        return reduce ? "reduce" : "add";
    }

    /** The amount with its sign turned, as text the tag's parser reads. */
    private static String negated(String amount) {
        return amount.startsWith("-") ? amount.substring(1) : "-" + amount;
    }

    private static String failureMessage(ItemEditor.Result result) {
        return switch (result) {
            case NOT_NUMERIC -> "This tag doesn't hold numbers.";
            case NOT_A_NUMBER -> "Invalid amount for this tag! (Requires a number of the tag's kind)";
            default -> "Invalid Tag!";
        };
    }

    private static String currentValue(ItemStack item, String tagId) {
        TagRegistry.RegisteredTag tag = getPlugin().getTagRegistry().find(tagId);
        Object value = item.getPersistentDataContainer().get(tag.key(), tag.type());
        return String.valueOf(value);
    }

    private static void send(Player player, String message) {
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }

    /** Offers the registered number tags. */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 2) {
            return TimingArgs.suggestions(args, 2);
        }
        if (args.length != 1) {
            return List.of();
        }
        TagRegistry tags = getPlugin().getTagRegistry();
        return tags.getKeys().stream()
                .filter(key -> isNumber(TagTypes.of(tags.getType(key))))
                .map(key -> key.getKey())
                .filter(id -> id.startsWith(args[0]))
                .toList();
    }

    private static boolean isNumber(TagType type) {
        return TagType.INT.equals(type) || TagType.FLOAT.equals(type);
    }
}
