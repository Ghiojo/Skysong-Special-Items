package org.skysongdev.skysongitems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.commands.books.BookCommand;
import org.skysongdev.skysongitems.commands.charges.ChargesCommand;
import org.skysongdev.skysongitems.commands.hp.ItemHpCommand;
import org.skysongdev.skysongitems.commands.custommodeldata.CustomModelDataCommand;
import org.skysongdev.skysongitems.commands.lore.RenameCommand;
import org.skysongdev.skysongitems.commands.misc.*;
import org.skysongdev.skysongitems.commands.traits.DetailsCommand;
import org.skysongdev.skysongitems.commands.traits.TraitsCommand;
import org.skysongdev.skysongitems.commands.transform.TransformationsCommand;
import org.skysongdev.skysongitems.commands.tags.TagCommand;
import org.skysongdev.skysongitems.commands.timer.TimerCommand;

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
        this(new TraitsCommand(), new UpdateItem());
    }

    /**
     * Lets tests replace the handlers of the {@code trait} and {@code update}
     * subcommands, so dispatch can be checked without running them.
     */
    SkysongItemsCommand(TraitsCommand traitsCommand, UpdateItem updateItem) {
        super("/si");
        registerSubcommands(traitsCommand, updateItem);
    }

    private void registerSubcommands(TraitsCommand traitsCommand, UpdateItem updateItem) {
        BookCommand bookCommand = new BookCommand(getPlugin().getBookQueries());
        TagCommand tagCommand = new TagCommand();
        RenameCommand renameCommand = new RenameCommand();
        ChargesCommand chargesCommand = new ChargesCommand();
        ItemHpCommand itemHpCommand = new ItemHpCommand();
        LoadItem loadItem = new LoadItem();
        SaveItem saveItem = new SaveItem();
        CustomModelDataCommand customModelDataCommand = new CustomModelDataCommand();
        FurnitureIdCommand furnitureIdCommand = new FurnitureIdCommand();
        TransformationsCommand transformationsCommand = new TransformationsCommand();
        TimerCommand timerCommand = new TimerCommand();

        registerItemEdit(Subcommand.builder("disable", new DisableFunc()).alias("dis").playerOnly());
        // Each book subcommand checks its own permission; anyone may view a bound description.
        register(Subcommand.builder("book", bookCommand).tabCompleter(bookCommand).build());
        registerItemEdit(Subcommand.builder("updatelore", UPDATE_LORE).playerOnly());
        registerItemEdit(Subcommand.builder("tag", tagCommand).tabCompleter(tagCommand).playerOnly());
        // trait and update check their own permissions; details only reads the held item.
        // prop and property are the names trait had before traits were renamed.
        register(Subcommand.builder("trait", traitsCommand).alias("prop").alias("property")
                .tabCompleter(traitsCommand).build());
        register(Subcommand.builder("details", new DetailsCommand()).playerOnly().build());
        register(Subcommand.builder("update", updateItem).playerOnly().build());
        registerItemEdit(Subcommand.builder("reload", new ReloadCommand()).playerOnly());
        registerItemEdit(Subcommand.builder("rename", renameCommand).tabCompleter(renameCommand).playerOnly());
        registerItemEdit(Subcommand.builder("charges", chargesCommand).tabCompleter(chargesCommand).playerOnly());
        registerItemEdit(Subcommand.builder("hp", itemHpCommand).tabCompleter(itemHpCommand).playerOnly());
        registerItemEdit(Subcommand.builder("loaditem", loadItem).tabCompleter(loadItem));
        registerItemEdit(Subcommand.builder("saveitem", saveItem).tabCompleter(saveItem));
        registerItemEdit(Subcommand.builder("setmodel", new SetItemModel()));
        registerItemEdit(Subcommand.builder("cmd", customModelDataCommand).tabCompleter(customModelDataCommand).playerOnly());
        registerItemEdit(Subcommand.builder("furniture", furnitureIdCommand).alias("ceid")
                .tabCompleter(furnitureIdCommand).playerOnly());
        registerItemEdit(Subcommand.builder("transform", transformationsCommand).tabCompleter(transformationsCommand));
        registerItemEdit(Subcommand.builder("timer", timerCommand).tabCompleter(timerCommand).playerOnly());
    }

    /** Applies the held item's due timers first, so every subcommand sees the item as it is now. */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player && getPlugin().getItemTimers() != null) {
            getPlugin().getItemTimers().checkHeldItem(player);
        }
        return super.onCommand(sender, command, label, args);
    }
}
