package org.skysongdev.skysongitems.commands.lore.funcionaldesc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skysongdev.skysongitems.events.ItemEditEvent;

import java.util.Arrays;

public class FunctionalDesc implements CommandExecutor {
    AddFunctional addFunc = new AddFunctional();
    SetFunctional setFunc = new SetFunctional();
    DelFunctional delFunc = new DelFunctional();
    DelLineFunctional delLine = new DelLineFunctional();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;

        String[] newArgs = Arrays.copyOfRange(strings, 1, strings.length);
        switch (strings[0].toLowerCase()) {
            case "add":
                if(strings.length > 1) {
                    addFunc.onCommand(commandSender, command, s, newArgs);
                }
                break;
            case "set":
                if(strings.length > 1) {
                    setFunc.onCommand(commandSender, command, s, newArgs);
                }
                break;
            case "del":
            case "delete":
                delFunc.onCommand(commandSender, command, s, newArgs);
                break;
            case "delline":
            case "dl":
                if(strings.length > 1) {
                    delLine.onCommand(commandSender, command, s, newArgs);
                }
                break;
            default:
                return false;
        }
        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));
        return true;
    }

}
