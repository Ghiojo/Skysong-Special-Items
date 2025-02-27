package org.skysongdev.skysongitems.commands.properties;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class PropertiesCommand implements TabExecutor {
    private AddProperty addProperty = new AddProperty();
    private RemoveProperty removeProperty = new RemoveProperty();
    private ViewProperties viewProperties = new ViewProperties();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1){
            sender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<gray>Usage: /properties <sub-command>"));
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch(args[0].toLowerCase()){
            case "add":
                if(sender.hasPermission("skysongitems.cit")){
                    addProperty.onCommand(sender, command, label, subArgs);
                } else{
                    sender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>You do not have permission to use this command."));
                }
                break;
            case "remove":
                if(sender.hasPermission("skysongitems.cit")){
                    removeProperty.onCommand(sender, command, label, subArgs);
                } else{
                    sender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>You do not have permission to use this command."));
                }
                break;
            case "view":
                viewProperties.onCommand(sender, command, label, subArgs);
                break;
            default:
                sender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid sub-command!"));
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return List.of("add", "remove", "view");
        }
        if(args.length > 1){
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            switch(args[0].toLowerCase()){
                case "add":
                    return addProperty.onTabComplete(sender, command, label, subArgs);
                case "remove":
                    return removeProperty.onTabComplete(sender, command, label, subArgs);
                default:
                    return List.of("");
            }
        }
        return List.of("");
    }
}
