package org.skysongdev.skysongitems.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skysongdev.skysongitems.commands.books.*;
import org.skysongdev.skysongitems.commands.charges.*;
import org.skysongdev.skysongitems.commands.lore.Updater;

import java.util.Arrays;

public class SkysongItemsCommand implements CommandExecutor {
    private final org.skysongdev.skysongitems.SkysongItems plugin;
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
    SetCharges setCharges;
    SetMaxCharges setMaxCharges;
    public AddCharges addCharges;
    ReduceCharges reduceCharges;
    RemoveCharges removeCharges;
    DBReload dbReload;
    SetAddable setAddable;

    public org.skysongdev.skysongitems.SkysongItems getPlugin() {
        return plugin;
    }

    public SkysongItemsCommand(org.skysongdev.skysongitems.SkysongItems plugin) {
        this.plugin = plugin;
        saveBook = new SaveBook(plugin);
        openBook = new OpenBook(plugin);
        setBookContent = new SetBookContent(plugin);
        deleteBook = new DeleteBook(plugin);
        listBooks = new ListBooks(plugin);
        bindBook = new BindBook(plugin);
        viewExtendedLore = new ViewExtendedLore(this);
        dmOnly = new SetDMOnly();
        setRightClickBook = new SetRightClickBook();
        unbindBook = new UnbindBook();
        dbReload = new DBReload(plugin);
        addCharges = new AddCharges(plugin);
        reduceCharges = new ReduceCharges(this);
        setCharges = new SetCharges(plugin);
        setMaxCharges = new SetMaxCharges(plugin);
        setAddable = new SetAddable(plugin);
        removeCharges = new RemoveCharges(plugin);
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
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "openbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    openBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "savebook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    saveBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "setbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setBookContent.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "delbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    deleteBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "bindbook":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    bindBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "viewdesc":
                viewExtendedLore.onCommand(commandSender, command, s, newArgs);
                break;
            case "listbooks":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    listBooks.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "setdm":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    dmOnly.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "setrc":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setRightClickBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "unbind":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    unbindBook.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "dbreload":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    dbReload.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "setcharges":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setCharges.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "setmaxcharges":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setMaxCharges.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "addcharges":
                addCharges.onCommand(commandSender, command, s, newArgs);
                break;
            case "reducecharges":
                reduceCharges.onCommand(commandSender, command, s, newArgs);
                break;
            case "chargerm":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    removeCharges.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "updatelore":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    plugin.getUpdater().UpdateLore((Player) commandSender);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            case "setaddable":
                if(commandSender.hasPermission("skysongspecialitems.cit"))
                    setAddable.onCommand(commandSender, command, s, newArgs);
                else
                    commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" +ChatColor.of("RED") + "You don't have permissions to do that command!");
                break;
            default:
                commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Please introduce valid arguments!");
                return true;
        }
        return true;
    }
}
