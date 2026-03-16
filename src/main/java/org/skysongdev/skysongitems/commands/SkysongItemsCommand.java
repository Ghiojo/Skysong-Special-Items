package org.skysongdev.skysongitems.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.commands.books.*;
import org.skysongdev.skysongitems.commands.charges.*;
import org.skysongdev.skysongitems.commands.custommodeldata.CustomModelDataCommand;
import org.skysongdev.skysongitems.commands.lore.RenameCommand;
import org.skysongdev.skysongitems.commands.misc.*;
import org.skysongdev.skysongitems.commands.properties.PropertiesCommand;
import org.skysongdev.skysongitems.commands.tags.TagCommand;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SkysongItemsCommand implements TabExecutor {
    DisableFunc disableFunc = new DisableFunc();
    SaveBook saveBook;
    public OpenBook openBook;
    SetBookContent setBookContent;
    DeleteBook deleteBook;
    ListBooks listBooks;
    BindBook bindBook;
    ViewExtendedLore viewExtendedLore;
    SetRightClickBook setRightClickBook;
    SetDMOnly dmOnly;
    UnbindBook unbindBook;
    DBReload dbReload;
    TagCommand tagCommand;
    PropertiesCommand propertiesCommand;
    UpdateItem updateItem;
    ReloadCommand reloadCommand;
    RenameCommand renameCommand;
    ChargesCommand chargesCommand;
    LoadItem loadItem;
    SaveItem saveItem;
    SetItemModel setItemModel;
    CustomModelDataCommand customModelDataCommand;

    public SkysongItemsCommand() {
        saveBook = new SaveBook();
        openBook = new OpenBook();
        setBookContent = new SetBookContent();
        deleteBook = new DeleteBook();
        listBooks = new ListBooks();
        bindBook = new BindBook();
        viewExtendedLore = new ViewExtendedLore(this);
        dmOnly = new SetDMOnly();
        setRightClickBook = new SetRightClickBook();
        unbindBook = new UnbindBook();
        dbReload = new DBReload();
        tagCommand = new TagCommand();
        propertiesCommand = new PropertiesCommand();
        updateItem = new UpdateItem();
        reloadCommand = new ReloadCommand();
        renameCommand = new RenameCommand();
        chargesCommand = new ChargesCommand();
        loadItem = new LoadItem();
        saveItem = new SaveItem();
        setItemModel = new SetItemModel();
        customModelDataCommand = new CustomModelDataCommand();
    }

    public ViewExtendedLore getViewExtendedLore() {
        return viewExtendedLore;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String[] newArgs = Arrays.copyOfRange(strings, 1, strings.length);

        if(strings.length < 1){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Too Little arguments! (Usage: /si (subcommand))");
            return true;
        }

        switch(strings[0].toLowerCase()){
            case "disable":
            case "dis":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    disableFunc.onCommand(commandSender, command, s, strings);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "openbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    openBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "savebook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    saveBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "setbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setBookContent.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "delbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    deleteBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "bindbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    bindBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "viewdesc":
                viewExtendedLore.onCommand(commandSender, command, s, newArgs);
                break;
            case "listbooks":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    listBooks.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "setdm":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    dmOnly.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "setrc":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setRightClickBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "unbind":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    unbindBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "dbreload":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    dbReload.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "updatelore":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    getPlugin().getUpdater().UpdateItem((Player) commandSender);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "tag":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    tagCommand.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "prop":
            case "property":
                propertiesCommand.onCommand(commandSender, command, s, newArgs);
            case "update":
                updateItem.onCommand(commandSender, command, s, newArgs);
                break;
            case "reload":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    reloadCommand.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "rename":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    renameCommand.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "charges":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    chargesCommand.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "loaditem":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    loadItem.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );

                break;
            case "saveitem":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    saveItem.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "setmodel":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setItemModel.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            case "cmd":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    customModelDataCommand.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You don't have permissions to do that command!") );
                break;
            default:
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Please introduce valid arguments!"));
                return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> subcommands = new ArrayList<>();

        if(strings.length <= 1) {
            if (commandSender.hasPermission("skysongspecialitems.cit")) {
                subcommands.add("disable");
                subcommands.add("openbook");
                subcommands.add("savebook");
                subcommands.add("setbook");
                subcommands.add("delbook");
                subcommands.add("bindbook");
                subcommands.add("viewdesc");
                subcommands.add("listbooks");
                subcommands.add("setdm");
                subcommands.add("setrc");
                subcommands.add("unbind");
                subcommands.add("dbreload");
                subcommands.add("updatelore");
                subcommands.add("tag");
                subcommands.add("prop");
                subcommands.add("property");
                subcommands.add("update");
                subcommands.add("reload");
                subcommands.add("rename");
                subcommands.add("charges");
                subcommands.add("loaditem");
                subcommands.add("saveitem");
                subcommands.add("setmodel");
                subcommands.add("cmd");
            } else {
                subcommands.add("viewdesc");
                subcommands.add("update");
                subcommands.add("rename");
                subcommands.add("charges");
            }
            return subcommands.stream().filter(a -> a.startsWith(strings[0])).toList();
        }
        if(strings.length > 1){
            String[] subArgs = Arrays.copyOfRange(strings, 1, strings.length);
            switch(strings[0].toLowerCase()) {
                case "tag":
                    return tagCommand.onTabComplete(commandSender, command, s, subArgs);
                case "prop":
                case "property":
                    return propertiesCommand.onTabComplete(commandSender, command, s, subArgs);
                case "rename":
                    return renameCommand.onTabComplete(commandSender, command, s, subArgs);
                case "charges":
                    return chargesCommand.onTabComplete(commandSender, command, s, subArgs);
                case "loaditem":
                    return loadItem.onTabComplete(commandSender, command, s, subArgs);
                case "cmd":
                    return customModelDataCommand.onTabComplete(commandSender, command, s, subArgs);
                default:
                    return Arrays.asList("").stream().toList();
            }
        }

        return null;
    }
}
