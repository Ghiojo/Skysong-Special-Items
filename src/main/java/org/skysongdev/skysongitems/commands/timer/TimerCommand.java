package org.skysongdev.skysongitems.commands.timer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.timer.TimerDescriptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si timer}: lets staff see and cancel the timers on the item they hold.
 *
 * <ul>
 *     <li>{@code /si timer list} lists them, numbered, with the time left</li>
 *     <li>{@code /si timer clear} cancels them all; {@code /si timer clear <number>} cancels one</li>
 * </ul>
 * Due timers have already run by then, as {@code /si} applies them to the held item first.
 */
public class TimerCommand implements TabExecutor {
    private static final String USAGE = "Usage: /si timer <list | clear [number]>";

    private final Supplier<TimerDescriptions> descriptions;

    public TimerCommand() {
        this(() -> getPlugin().getTimerDescriptions());
    }

    public TimerCommand(Supplier<TimerDescriptions> descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        String action = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        ItemStack item = player.getInventory().getItemInMainHand();
        switch (action) {
            case "list" -> TimerListing.send(player, item, descriptions.get());
            case "clear" -> clear(player, item, args.length > 1 ? args[1] : null);
            default -> TimerListing.send(player, USAGE);
        }
        return true;
    }

    private static void clear(Player player, ItemStack item, String number) {
        List<ItemTimer> timers = item.getType().isAir() ? List.of()
                : new ArrayList<>(ItemTimerStore.read(item.getPersistentDataContainer()));
        if (timers.isEmpty()) {
            TimerListing.send(player, TimerListing.NO_TIMERS);
            return;
        }
        if (number == null) {
            ItemTimerStore.write(item, List.of());
            TimerListing.send(player, "<green>Cancelled every timer on this item.");
            return;
        }
        int index = parseIndex(number, timers.size());
        if (index < 0) {
            TimerListing.send(player, "<red>Give a timer number from 1 to " + timers.size() + ", as /si timer list shows.");
            return;
        }
        timers.remove(index);
        ItemTimerStore.write(item, timers);
        TimerListing.send(player, "<green>Cancelled timer " + number + ".");
    }

    /** The list position of a timer number from 1 to {@code count}, or -1 for anything else. */
    private static int parseIndex(String number, int count) {
        try {
            int index = Integer.parseInt(number) - 1;
            return index >= 0 && index < count ? index : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("list", "clear").stream().filter(option -> option.startsWith(args[0])).toList();
        }
        if (args.length == 2 && "clear".equalsIgnoreCase(args[0]) && sender instanceof Player player) {
            int count = ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer()).size();
            return IntStream.rangeClosed(1, count).mapToObj(String::valueOf).filter(n -> n.startsWith(args[1])).toList();
        }
        return List.of();
    }
}
