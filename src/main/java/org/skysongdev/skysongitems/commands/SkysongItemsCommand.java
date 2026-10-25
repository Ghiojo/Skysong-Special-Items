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

        registerItemEdit(Subcommand.builder("disable", new DisableFunc()).hiddenAlias("dis").playerOnly());
        registerItemEdit(Subcommand.builder("openbook", openBook).playerOnly());
        registerItemEdit(Subcommand.builder("savebook", new SaveBook(bookQueries)).playerOnly());
        registerItemEdit(Subcommand.builder("setbook", new SetBookContent(bookQueries)).playerOnly());
        registerItemEdit(Subcommand.builder("delbook", new DeleteBook(bookQueries)));
        registerItemEdit(Subcommand.builder("bindbook", new BindBook(bookQueries)).playerOnly());
        registry.register(Subcommand.builder("viewdesc", new ViewExtendedLore(openBook)).listedToEveryone().playerOnly().build());
        registerItemEdit(Subcommand.builder("listbooks", new ListBooks(bookQueries)));
        registerItemEdit(Subcommand.builder("setdm", new SetDMOnly()).playerOnly());
        registerItemEdit(Subcommand.builder("setrc", new SetRightClickBook()).playerOnly());
        registerItemEdit(Subcommand.builder("unbind", new UnbindBook()).playerOnly());
        registerItemEdit(Subcommand.builder("dbreload", new DBReload()));
        registerItemEdit(Subcommand.builder("updatelore", UPDATE_LORE).playerOnly());
        registerItemEdit(Subcommand.builder("tag", tagCommand).tabCompleter(tagCommand));
        // prop and update check their own permissions; details only reads the held item.
        registry.register(Subcommand.builder("prop", propertiesCommand).alias("property")
                .tabCompleter(propertiesCommand).build());
        registry.register(Subcommand.builder("details", new DetailsCommand()).listedToEveryone().playerOnly().build());
        registry.register(Subcommand.builder("update", updateItem).listedToEveryone().playerOnly().build());
        registerItemEdit(Subcommand.builder("reload", new ReloadCommand()).playerOnly());
        registerItemEdit(Subcommand.builder("rename", renameCommand).tabCompleter(renameCommand).listedToEveryone().playerOnly());
        registerItemEdit(Subcommand.builder("charges", chargesCommand).tabCompleter(chargesCommand).listedToEveryone());
        registerItemEdit(Subcommand.builder("loaditem", loadItem).tabCompleter(loadItem));
        registerItemEdit(Subcommand.builder("saveitem", new SaveItem()));
        registerItemEdit(Subcommand.builder("setmodel", new SetItemModel()));
        registerItemEdit(Subcommand.builder("cmd", customModelDataCommand).tabCompleter(customModelDataCommand));
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
            return listedNames(commandSender).stream().filter(name -> name.startsWith(strings[0])).toList();
        }
        TabCompleter completer = registry.find(strings[0]).map(Subcommand::tabCompleter).orElse(null);
        if (completer == null) {
            return List.of("");
        }
        return completer.onTabComplete(commandSender, command, s, argumentsAfterName(strings));
    }

    /**
     * Item editors are offered every subcommand; everyone else only those marked
     * as listed to everyone.
     */
    private List<String> listedNames(CommandSender sender) {
        boolean itemEditor = sender.hasPermission(ITEM_EDIT_PERMISSION);
        return registry.all().stream()
                .filter(subcommand -> itemEditor || subcommand.isListedToEveryone())
                .flatMap(subcommand -> subcommand.listedNames().stream())
                .toList();
    }

    private static String[] argumentsAfterName(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
