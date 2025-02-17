package org.skysongdev.skysongitems.commands.charges;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class ChargesCommand implements TabExecutor {
    SetCharges setCharges;
    SetMaxCharges setMaxCharges;
    public AddCharges addCharges;
    ReduceCharges reduceCharges;
    RemoveCharges removeCharges;
    SetAddable setAddable;

    public ChargesCommand(){
        this.setCharges = new SetCharges();
        this.setMaxCharges = new SetMaxCharges();
        this.addCharges = new AddCharges();
        this.reduceCharges = new ReduceCharges(this);
        this.removeCharges = new RemoveCharges();
        this.setAddable = new SetAddable();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        String[] newArgs = Arrays.copyOfRange(strings, 1, strings.length);

        if(strings.length < 1){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Too Little arguments! (Usage: /si (subcommand))");
            return true;
        }

        switch (strings[0].toLowerCase()) {
            case "set":
                if(commandSender.hasPermission("skysongspecialitems.cit")){
                    setCharges.onCommand(commandSender, command, s, newArgs);
                } else {
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "You don't have permissions to do that command!") );
                }
                break;
            case "setmax":
                if(commandSender.hasPermission("skysongspecialitems.cit")){
                    setMaxCharges.onCommand(commandSender, command, s, newArgs);
                } else {
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "You don't have permissions to do that command!") );
                }
                break;
            case "add":
                addCharges.onCommand(commandSender, command, s, newArgs);
                break;
            case "reduce":
                reduceCharges.onCommand(commandSender, command, s, newArgs);
                break;
            case "remove":
                if(commandSender.hasPermission("skysongspecialitems.cit")){
                    removeCharges.onCommand(commandSender, command, s, newArgs);
                } else {
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "You don't have permissions to do that command!") );
                }
                break;
            case "addable":
                if(commandSender.hasPermission("skysongspecialitems.cit")){
                    setAddable.onCommand(commandSender, command, s, newArgs);
                } else {
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "You don't have permissions to do that command!") );
                }
                break;
            default:
                commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Incorrect Arguments! Usage: /si (subcommand) (args)");
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> subcommands = new ArrayList<>();
        if (commandSender.hasPermission("skysongspecialitems.cit")) {
            subcommands.add("set");
            subcommands.add("setmax");
            subcommands.add("add");
            subcommands.add("reduce");
            subcommands.add("remove");
            subcommands.add("addable");
        } else {
            subcommands.add("add");
            subcommands.add("reduce");
        }
        return subcommands.stream().filter(a -> a.startsWith(strings[0])).toList();
    }
}
