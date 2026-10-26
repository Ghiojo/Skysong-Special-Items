package org.skysongdev.skysongitems.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.skysongdev.skysongitems.commands.books.BookCommand;
import org.skysongdev.skysongitems.commands.charges.ChargesCommand;
import org.skysongdev.skysongitems.commands.custommodeldata.CustomModelDataCommand;
import org.skysongdev.skysongitems.commands.lore.RenameCommand;
import org.skysongdev.skysongitems.commands.misc.*;
import org.skysongdev.skysongitems.commands.properties.DetailsCommand;
import org.skysongdev.skysongitems.commands.properties.PropertiesCommand;
import org.skysongdev.skysongitems.commands.tags.TagCommand;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * The {@code /si} command. Each subcommand is registered once in
 * {@link #registerSubcommands}; {@link SubcommandRouter} looks the subcommand up,
 * checks that the sender may run it and hands it the remaining arguments.
 */
public class SkysongItemsCommand extends SubcommandRouter {
    /** {@code /si updatelore} has no handler class of its own; it runs the plugin's lore updater. */
    private static final CommandExecutor UPDATE_LORE = (sender, command, label, args) -> {
        getPlugin().getUpdater().UpdateItem((Player) sender);
        return true;
    };

    public SkysongItemsCommand() {
        this(new PropertiesCommand(), new UpdateItem());
    }

    /**
     * Lets tests replace the handlers of the {@code prop} and {@code update}
     * subcommands, so dispatch can be checked without running them.
     */
    SkysongItemsCommand(PropertiesCommand propertiesCommand, UpdateItem updateItem) {
        super("/si");
        registerSubcommands(propertiesCommand, updateItem);
    }

    private void registerSubcommands(PropertiesCommand propertiesCommand, UpdateItem updateItem) {
        BookCommand bookCommand = new BookCommand(getPlugin().getBookQueries());
        TagCommand tagCommand = new TagCommand();
        RenameCommand renameCommand = new RenameCommand();
        ChargesCommand chargesCommand = new ChargesCommand();
        LoadItem loadItem = new LoadItem();
        CustomModelDataCommand customModelDataCommand = new CustomModelDataCommand();
        FurnitureIdCommand furnitureIdCommand = new FurnitureIdCommand();

        registerItemEdit(Subcommand.builder("disable", new DisableFunc()).alias("dis").playerOnly());
        // Each book subcommand checks its own permission; anyone may view a bound description.
        register(Subcommand.builder("book", bookCommand).tabCompleter(bookCommand).build());
        registerItemEdit(Subcommand.builder("updatelore", UPDATE_LORE).playerOnly());
        registerItemEdit(Subcommand.builder("tag", tagCommand).tabCompleter(tagCommand).playerOnly());
        // prop and update check their own permissions; details only reads the held item.
        register(Subcommand.builder("prop", propertiesCommand).alias("property")
                .tabCompleter(propertiesCommand).build());
        register(Subcommand.builder("details", new DetailsCommand()).playerOnly().build());
        register(Subcommand.builder("update", updateItem).playerOnly().build());
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
}
