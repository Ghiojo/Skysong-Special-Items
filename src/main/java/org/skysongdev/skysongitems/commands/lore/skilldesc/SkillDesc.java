package org.skysongdev.skysongitems.commands.lore.skilldesc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skysongdev.skysongitems.events.ItemEditEvent;

import java.util.Arrays;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SkillDesc implements CommandExecutor {
    AddSkill addSkill = new AddSkill();
    SetSkill setSkill = new SetSkill();
    DelSkill delSkill = new DelSkill();
    DelLineSkill delLine = new DelLineSkill();
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String[] newArgs = Arrays.copyOfRange(strings, 1, strings.length);
        switch (strings[0].toLowerCase()) {
            case "add":
                if (strings.length > 1) {
                    addSkill.onCommand(commandSender, command, s, newArgs);
                }
                break;
            case "set":
                if (strings.length > 1) {
                    setSkill.onCommand(commandSender, command, s, newArgs);
                }
                break;
            case "del":
            case "delete":
                delSkill.onCommand(commandSender, command, s, newArgs);
                break;
            case "delline":
            case "dl":
                if (strings.length > 1) {
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
