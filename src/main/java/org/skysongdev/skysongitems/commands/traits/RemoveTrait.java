package org.skysongdev.skysongitems.commands.traits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.commands.timer.LaterEdits;
import org.skysongdev.skysongitems.commands.timer.TimingArgs;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveTrait implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("This command can only be used by players."));
            return true;
        }
        Player player = (Player) commandSender;

        if(strings.length < 1){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Usage: /si trait remove <trait> [after <time> | every <time>]"));
            return true;
        }
        TimingArgs timing = TimingArgs.readOrRefuse(player, strings, 1, true);
        if (timing == null) {
            return true;
        }

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You must hold an item in your main hand to use this command."));
            return true;
        }

        if(!getPlugin().getTraitRegistry().contains(strings[0])){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid trait!"));
            return true;
        }

        // The updater may replace the held item, so edit the one it leaves in the hand.
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack updatedItem = player.getInventory().getItemInMainHand();

        if (timing.isLater()) {
            LaterEdits.schedule(player, updatedItem, timing, BuiltInTimerActions.REMOVE_TRAIT, strings[0], null);
            return true;
        }
        ItemEditor.Result result = getPlugin().getItemEditor().forCommand(player).removeTrait(updatedItem, strings[0]);
        String failure = failureMessage(result);
        if (failure != null) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>" + failure));
            return true;
        }

        player.updateInventory();
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>Trait removed successfully!"));
        return true;
    }

    /** What to tell the player when the trait couldn't be removed, or null when it was. */
    private static @Nullable String failureMessage(ItemEditor.Result result) {
        return switch (result) {
            case NO_TRAITS -> "This item has no traits.";
            case MISSING -> "This item does not have this trait.";
            default -> null;
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length > 1) {
            return TimingArgs.suggestions(strings, 1);
        }
        ArrayList<String> traits = new ArrayList<>();
        Player player = (Player) commandSender;
        if(player.getInventory().getItemInMainHand().getType().isAir())
            return List.of("NO ITEM IN MAIN HAND");
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

        ItemData data = ItemData.from(meta);
        if(!data.hasTraits())
            return List.of();

        for(String trait : data.getTraits()){
            if(trait.toLowerCase().startsWith(strings[0].toLowerCase())){
                traits.add(trait);
            }
        }
        return traits;
    }
}
