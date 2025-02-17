package org.skysongdev.skysongitems.commands.lore.physicaldesc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skysongdev.skysongitems.events.ItemEditEvent;

import java.util.Arrays;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class PhysicalDesc implements CommandExecutor {
    AddPhysical addPhys = new AddPhysical();
    SetPhysical setPhys = new SetPhysical();
    DelPhys delPhys = new DelPhys();

    DelLinePhysical delLine = new DelLinePhysical();
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String[] newArgs = Arrays.copyOfRange(strings, 1, strings.length);
        switch (strings[0].toLowerCase()) {
            case "add":
                if(strings.length > 1) {
                    addPhys.onCommand(commandSender, command, s, newArgs);
                }
                else
                    return false;
                break;
            case "set":
                if(strings.length > 1) {
                    setPhys.onCommand(commandSender, command, s, newArgs);
                }
                else
                    return false;
                break;
            case "del":
            case "delete":
                delPhys.onCommand(commandSender, command, s, newArgs);
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
