package org.skysongdev.skysongitems.commands.custommodeldata;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class CustomModelDataCommand implements TabExecutor {

    AddCustomModelData addCustomModelData = new AddCustomModelData();
    DeleteCustomModelData deleteCustomModelData = new DeleteCustomModelData();
    ListCustomModelData listCustomModelData = new ListCustomModelData();
    RemoveCustomModelData removeCustomModelData = new RemoveCustomModelData();
    SetCustomModelData setCustomModelData = new SetCustomModelData();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<gray>Usage: /properties <sub-command>"));
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(strings, 1, strings.length);

        switch(strings[0].toLowerCase()){
            case "add":
                addCustomModelData.onCommand(commandSender, command, s, subArgs);
                break;
            case "delete":
                deleteCustomModelData.onCommand(commandSender, command, s, subArgs);
                break;
            case "list":
                listCustomModelData.onCommand(commandSender, command, s, subArgs);
                break;
            case "remove":
                removeCustomModelData.onCommand(commandSender, command, s, subArgs);
                break;
            case "set":
                setCustomModelData.onCommand(commandSender, command, s, subArgs);
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        String[] subArgs = Arrays.copyOfRange(strings, 1, strings.length);
        switch(strings.length){
            case 1:
                return List.of("add", "delete", "list", "remove", "set").stream().filter(sub -> sub.startsWith(strings[0].toLowerCase())).toList();
            case 2:
                switch(strings[0].toLowerCase()){
                    case "add":
                        return addCustomModelData.onTabComplete(commandSender, command, s, subArgs);
                    case "delete":
                        return deleteCustomModelData.onTabComplete(commandSender, command, s, subArgs);
                    case "list":
                        return listCustomModelData.onTabComplete(commandSender, command, s, subArgs);
                    case "remove":
                        return removeCustomModelData.onTabComplete(commandSender, command, s, subArgs);
                    case "set":
                        return setCustomModelData.onTabComplete(commandSender, command, s, subArgs);
                }
        }
        return List.of();
    }
}
