package org.skysongdev.skysongitems.commands.traits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.commands.timer.LaterEdits;
import org.skysongdev.skysongitems.commands.timer.TimingArgs;
import org.skysongdev.skysongitems.timer.ItemTimerScheduler;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.Trait;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class AddTrait implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("This command can only be used by players."));
            return true;
        }
        Player player = (Player) commandSender;



        if(args.length < 1){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Usage: /si trait add <trait> [time | after <time> | every <time>]"));
            return true;
        }
        TimingArgs timing = TimingArgs.readOrRefuse(player, args, 1, false);
        if (timing == null) {
            return true;
        }

        getPlugin().getUpdater().UpdateItem(player);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You must hold an item in your main hand to use this command."));
            return true;
        }

        if(!getPlugin().getTraitRegistry().contains(args[0])){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid trait!"));
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Valid traits: " + getPlugin().getTraitRegistry().getAll().stream().map(Trait::getId).toList()));
            return true;
        }

        if (timing.isLater()) {
            LaterEdits.schedule(player, itemInMainHand, timing, BuiltInTimerActions.ADD_TRAIT, args[0], null);
            return true;
        }
        boolean added = !ItemEditor.Result.ALREADY_PRESENT.equals(getPlugin().getItemEditor().forCommand(player).addTrait(itemInMainHand, args[0]));
        boolean timerChanged = ItemTimerScheduler.replaceOrCancel(itemInMainHand, timing.forTiming(),
                BuiltInTimerActions.REMOVE_TRAIT.id(), args[0]);
        player.updateInventory();
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + confirmation(added, timerChanged, timing)));
        return true;
    }

    private static String confirmation(boolean added, boolean timerChanged, TimingArgs timing) {
        if (added) {
            return "Trait added" + timing.forText() + "!";
        }
        if (!timerChanged) {
            return "This item already has this trait!";
        }
        return timing.isTimed() ? "This item already has this trait; it now lasts" + timing.forText() + "." : "This item's trait is now permanent.";
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 2) {
            return TimingArgs.suggestions(args, 1);
        }
        if (args.length > 2) {
            return List.of();
        }
        ArrayList<String> traits = new ArrayList<>();
        for(Trait trait : getPlugin().getTraitRegistry().getAll()){
            traits.add(trait.getId());
        }
        return traits.stream().filter(a -> a.startsWith(args[0])).toList();
    }
}
