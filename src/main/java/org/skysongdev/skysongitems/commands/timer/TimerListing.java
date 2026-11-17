package org.skysongdev.skysongitems.commands.timer;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.timer.TimerDescriptions;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/** Sends the list of an item's timers, the same for {@code /si timer list} and {@code /timers}. */
final class TimerListing {
    static final String NO_TIMERS = "This item has no timers.";

    private TimerListing() {
    }

    /** Lists the item's timers to the receiver, or says it has none. */
    static void send(CommandSender receiver, ItemStack item, TimerDescriptions descriptions) {
        if (item.getType().isAir()) {
            send(receiver, "<red>Hold the item whose timers you want to see.");
            return;
        }
        List<ItemTimer> timers = ItemTimerStore.read(item.getPersistentDataContainer());
        if (timers.isEmpty()) {
            send(receiver, NO_TIMERS);
            return;
        }
        send(receiver, "This item has " + timers.size() + (timers.size() == 1 ? " timer:" : " timers:"));
        descriptions.describe(timers).forEach(line -> send(receiver, "<gray>" + line));
    }

    static void send(CommandSender receiver, String message) {
        receiver.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }
}
