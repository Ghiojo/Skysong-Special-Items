package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.integration.CraftEngineItemIds;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si furniture} (alias {@code /si ceid}): shows, sets or removes the
 * CraftEngine id of the item in the player's main hand.
 *
 * <ul>
 *     <li>{@code /si furniture} shows the item's id</li>
 *     <li>{@code /si furniture set <namespace:id>} sets it</li>
 *     <li>{@code /si furniture remove} removes it</li>
 * </ul>
 *
 * <p>CraftEngine treats an item carrying the id of one of its furniture items as
 * that furniture, so the Skysong item can be placed, and the furniture's
 * {@code furniture_item} loot gives the same item back with its Skysong tags. An id
 * CraftEngine has not loaded is still set, with a warning, so items can be made
 * before their furniture is configured. Needs CraftEngine installed.
 */
public class FurnitureIdCommand implements TabExecutor {
    /** A resource location, as CraftEngine ids are: lowercase namespace and path. */
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    // MiniMessage would read the placeholder as a tag, so its bracket is escaped.
    private static final String USAGE = "Usage: /si furniture [set \\<namespace:id> | remove]";

    private final Supplier<Optional<CraftEngineItemIds>> craftEngine;

    public FurnitureIdCommand() {
        this(() -> getPlugin().getCraftEngine());
    }

    /** @param craftEngine looked up on each use, since CraftEngine is optional */
    public FurnitureIdCommand(Supplier<Optional<CraftEngineItemIds>> craftEngine) {
        this.craftEngine = craftEngine;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        Optional<CraftEngineItemIds> ids = craftEngine.get();
        if (ids.isEmpty()) {
            send(player, "<red>CraftEngine is not installed, so items cannot be given a CraftEngine id.");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            send(player, "You must hold an item in your main hand to use this command.");
            return true;
        }

        String action = strings.length == 0 ? "" : strings[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "" -> show(player, ids.get(), item);
            case "set" -> set(player, ids.get(), item, strings);
            case "remove" -> remove(player, ids.get(), item);
            default -> send(player, USAGE);
        }
        return true;
    }

    private static void show(Player player, CraftEngineItemIds ids, ItemStack item) {
        Optional<String> id = ids.idOf(item);
        if (id.isEmpty()) {
            send(player, "This item has no CraftEngine id.");
            return;
        }
        send(player, "This item's CraftEngine id is <gold>" + id.get() + "<gray>.");
    }

    private static void set(Player player, CraftEngineItemIds ids, ItemStack item, String[] args) {
        if (args.length < 2 || !ID.matcher(args[1]).matches()) {
            send(player, "<red>Usage: /si furniture set \\<namespace:id> (lowercase, for example skysong:oak_table)");
            return;
        }
        String id = args[1];
        player.getInventory().setItemInMainHand(ids.withId(item, id));
        send(player, "<green>CraftEngine id set to " + id + ".");
        if (!ids.isLoaded(id)) {
            send(player, "<yellow>CraftEngine has no item with the id " + id
                    + " loaded, so the item will not place as furniture until it does.");
        }
    }

    private static void remove(Player player, CraftEngineItemIds ids, ItemStack item) {
        if (ids.idOf(item).isEmpty()) {
            send(player, "<red>This item has no CraftEngine id.");
            return;
        }
        player.getInventory().setItemInMainHand(ids.withoutId(item));
        send(player, "<green>CraftEngine id removed.");
    }

    private static void send(Player player, String message) {
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return startingWith(List.of("set", "remove"), strings[0]);
        }
        if (strings.length == 2 && "set".equalsIgnoreCase(strings[0])) {
            List<String> loaded = craftEngine.get().map(CraftEngineItemIds::loadedIds).orElse(List.of());
            return startingWith(loaded, strings[1]);
        }
        return List.of();
    }

    private static List<String> startingWith(List<String> options, String prefix) {
        return options.stream().filter(option -> option.startsWith(prefix)).toList();
    }
}
