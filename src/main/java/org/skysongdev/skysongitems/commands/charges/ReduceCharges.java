package org.skysongdev.skysongitems.commands.charges;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.skysongdev.skysongitems.commands.SkysongItemsCommand;

public class ReduceCharges implements CommandExecutor {
    ChargesCommand parent;

    public ReduceCharges(ChargesCommand parent) {
        this.parent = parent;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String[] sentstring = strings;
        sentstring[0] = "-" + strings[0];
        parent.addCharges.onCommand(commandSender, command, s, sentstring);
        return true;
    }
}
