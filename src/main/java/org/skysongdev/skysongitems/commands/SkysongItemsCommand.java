package org.skysongdev.skysongitems.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.commands.books.*;
import org.skysongdev.skysongitems.commands.charges.ChargesCommand;
import org.skysongdev.skysongitems.commands.custommodeldata.CustomModelDataCommand;
import org.skysongdev.skysongitems.commands.lore.RenameCommand;
import org.skysongdev.skysongitems.commands.misc.*;
import org.skysongdev.skysongitems.commands.properties.DetailsCommand;
import org.skysongdev.skysongitems.commands.properties.PropertiesCommand;
import org.skysongdev.skysongitems.commands.tags.TagCommand;
import org.skysongdev.skysongitems.database.BookQueries;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * The {@code /si} command. Each subcommand is registered once in
 * {@link #registerSubcommands}; this class only looks the subcommand up, checks
 * that the sender may run it (permission, and being a player where the handler
 * needs one), and hands it the remaining arguments.
 */
public class SkysongItemsCommand implements TabExecutor {
    /** Needed for every subcommand that edits items, books or the plugin's data. */
    static final String ITEM_EDIT_PERMISSION = "skysongspecialitems.cit";

    /** {@code /si updatelore} has no handler class of its own; it runs the plugin's lore updater. */
    private static final CommandExecutor UPDATE_LORE = (sender, command, label, args) -> {
        getPlugin().getUpdater().UpdateItem((Player) sender);
        return true;
    };

    private final SubcommandRegistry registry = new SubcommandRegistry();

    public SkysongItemsCommand() {
        this(new PropertiesCommand(), new UpdateItem());
    }

    /**
     * Lets tests replace the handlers of the {@code prop} and {@code update}
     * subcommands, so dispatch can be checked without running them.
     */
    SkysongItemsCommand(PropertiesCommand propertiesCommand, UpdateItem updateItem) {
        registerSubcommands(propertiesCommand, updateItem);
    }

    private void registerSubcommands(PropertiesCommand propertiesCommand, UpdateItem updateItem) {
        BookQueries bookQueries = getPlugin().getBookQueries();
        OpenBook openBook = new OpenBook(bookQueries);
        TagCommand tagCommand = new TagCommand();
        RenameCommand renameCommand = new RenameCommand();
        ChargesCommand chargesCommand = new ChargesCommand();
        LoadItem loadItem = new LoadItem();
        CustomModelDataCommand customModelDataCommand = new CustomModelDataCommand();
        FurnitureIdCommand furnitureIdCommand = new FurnitureIdCommand();

        registerItemEdit(Subcommand.builder("disable", new DisableFunc()).alias("dis").playerOnly());
        registerItemEdit(Subcommand.builder("openbook", openBook).playerOnly());
        registerItemEdit(Subcommand.builder("savebook", new SaveBook(bookQueries)).playerOnly());
        registerItemEdit(Subcommand.builder("setbook", new SetBookContent(bookQueries)).playerOnly());
        registerItemEdit(Subcommand.builder("delbook", new DeleteBook(bookQueries)));
        registerItemEdit(Subcommand.builder("bindbook", new BindBook(bookQueries)).playerOnly());
        registry.register(Subcommand.builder("viewdesc", new ViewExtendedLore(openBook)).playerOnly().build());
        registerItemEdit(Subcommand.builder("listbooks", new ListBooks(bookQueries)));
        registerItemEdit(Subcommand.builder("setdm", new SetDMOnly()).playerOnly());
        registerItemEdit(Subcommand.builder("setrc", new SetRightClickBook()).playerOnly());
        registerItemEdit(Subcommand.builder("unbind", new UnbindBook()).playerOnly());
        registerItemEdit(Subcommand.builder("dbreload", new DBReload()));
        registerItemEdit(Subcommand.builder("updatelore", UPDATE_LORE).playerOnly());
        registerItemEdit(Subcommand.builder("tag", tagCommand).tabCompleter(tagCommand).playerOnly());
        // prop and update check their own permissions; details only reads the held item.
        registry.register(Subcommand.builder("prop", propertiesCommand).alias("property")
                .tabCompleter(propertiesCommand).build());
        registry.register(Subcommand.builder("details", new DetailsCommand()).playerOnly().build());
        registry.register(Subcommand.builder("update", updateItem).playerOnly().build());
        registerItemEdit(Subcommand.builder("reload", new ReloadCommand()).playerOnly());
        registerItemEdit(Subcommand.builder("rename", renameCommand).tabCompleter(renameCommand).playerOnly());
        registerItemEdit(Subcommand.builder("charges", chargesCommand).tabCompleter(chargesCommand).playerOnly());
        registerItemEdit(Subcommand.builder("loaditem", loadItem).tabCompleter(loadItem));
        registerItemEdit(Subcommand.builder("saveitem", new SaveItem()));
        registerItemEdit(Subcommand.builder("setmodel", new SetItemModel()));
        registerItemEdit(Subcommand.builder("cmd", customModelDataCommand).tabCompleter(customModelDataCommand).playerOnly());
        registerItemEdit(Subcommand.builder("furniture", furnitureIdCommand).alias("ceid")
                .tabCompleter(furnitureIdCommand).playerOnly());
    }

    private void registerItemEdit(Subcommand.Builder builder) {
        registry.register(builder.permission(ITEM_EDIT_PERMISSION).build());
    }

    SubcommandRegistry getRegistry() {
        return registry;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Too Little arguments! (Usage: /si (subcommand))");
            return true;
        }

        Optional<Subcommand> found = registry.find(strings[0]);
        if (found.isEmpty()) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Please introduce valid arguments!"));
            return true;
        }
        Subcommand subcommand = found.get();
        if (!subcommand.canRun(commandSender)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!"));
            return true;
        }
        if (subcommand.isPlayerOnly() && !(commandSender instanceof Player)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This command can only be used by players!"));
            return true;
        }
        subcommand.executor().onCommand(commandSender, command, s, argumentsAfterName(strings));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length <= 1) {
            return namesUsableBy(commandSender).stream().filter(name -> name.startsWith(strings[0])).toList();
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

    private static String[] argumentsAfterName(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
