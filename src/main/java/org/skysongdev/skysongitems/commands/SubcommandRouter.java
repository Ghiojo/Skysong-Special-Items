package org.skysongdev.skysongitems.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * A command made of named subcommands, such as {@code /si} or {@code /si book}.
 * Subclasses register their subcommands; this class looks the first argument up,
 * checks that the sender may run it (permission, and being a player where the
 * handler needs one), and hands the handler the remaining arguments. Tab completion
 * offers only the subcommands the sender may run.
 */
public class SubcommandRouter implements TabExecutor {
    /** Needed for every subcommand that edits items, books or the plugin's data. */
    private static final String ITEM_EDIT_PERMISSION = "skysongspecialitems.cit";

    private final SubcommandRegistry registry = new SubcommandRegistry();
    private final String usage;

    /** @param usage how the command is typed, shown when no subcommand is given, for example {@code /si book} */
    protected SubcommandRouter(String usage) {
        this.usage = usage;
    }

    protected final void register(Subcommand subcommand) {
        registry.register(subcommand);
    }

    /** Registers a subcommand that needs the item edit permission. */
    protected final void registerItemEdit(Subcommand.Builder builder) {
        register(builder.permission(ITEM_EDIT_PERMISSION).build());
    }

    public SubcommandRegistry getRegistry() {
        return registry;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Too Little arguments! (Usage: " + usage + " (subcommand))");
            return true;
        }

        Optional<Subcommand> found = registry.find(strings[0]);
        if (found.isEmpty()) {
            sendTagged(commandSender, "Please introduce valid arguments!");
            return true;
        }
        Subcommand subcommand = found.get();
        if (!subcommand.canRun(commandSender)) {
            sendTagged(commandSender, "You don't have permissions to do that command!");
            return true;
        }
        if (subcommand.isPlayerOnly() && !(commandSender instanceof Player)) {
            sendTagged(commandSender, "<red>This command can only be used by players!");
            return true;
        }
        subcommand.executor().onCommand(commandSender, command, s, argumentsAfterName(strings));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length <= 1) {
            String typed = strings.length == 0 ? "" : strings[0];
            return namesUsableBy(commandSender).stream().filter(name -> name.startsWith(typed)).toList();
        }
        // Player-only completers read the sender's held item, so the console gets nothing.
        TabCompleter completer = registry.find(strings[0])
                .filter(subcommand -> subcommand.canRun(commandSender))
                .filter(subcommand -> !subcommand.isPlayerOnly() || commandSender instanceof Player)
                .map(Subcommand::tabCompleter)
                .orElse(null);
        if (completer == null) {
            return List.of("");
        }
        return completer.onTabComplete(commandSender, command, s, argumentsAfterName(strings));
    }

    /** The name and aliases of every subcommand the sender has permission to run. */
    private List<String> namesUsableBy(CommandSender sender) {
        return registry.all().stream()
                .filter(subcommand -> subcommand.canRun(sender))
                .flatMap(subcommand -> subcommand.names().stream())
                .toList();
    }

    private static void sendTagged(CommandSender sender, String message) {
        sender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }

    private static String[] argumentsAfterName(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
