package org.skysongdev.skysongitems.commands.timer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.timer.ItemTimerTriggers;
import org.skysongdev.skysongitems.timer.TimerDescriptions;

import java.util.function.Supplier;

/**
 * {@code /timers} (alias {@code /itemtimers}): shows players the timers on the item they hold,
 * read only. Due timers are applied first, so the list is what is still to come.
 */
public class PlayerTimersCommand implements CommandExecutor {
    private final ItemTimerTriggers triggers;
    private final Supplier<TimerDescriptions> descriptions;

    public PlayerTimersCommand(ItemTimerTriggers triggers, Supplier<TimerDescriptions> descriptions) {
        this.triggers = triggers;
        this.descriptions = descriptions;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            TimerListing.send(sender, "<red>Only players can see the timers of the item they hold.");
            return true;
        }
        triggers.checkHeldItem(player);
        TimerListing.send(player, player.getInventory().getItemInMainHand(), descriptions.get());
        return true;
    }
}
