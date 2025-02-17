package org.skysongdev.skysongitems.commands.tags;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class TagCommand implements TabExecutor {
    SetTag setTag = new SetTag();
    RemoveTag removeTag = new RemoveTag();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<gray>Usage: /properties <sub-command>"));
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(strings, 1, strings.length);

        switch(strings[0].toLowerCase()){
            case "set":
                setTag.onCommand(commandSender, command, s, subArgs);
                break;
            case "remove":
                removeTag.onCommand(commandSender, command, s, subArgs);
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 1)
            return List.of("set", "remove");
        if(strings.length > 1){
            String[] subArgs = Arrays.copyOfRange(strings, 1, strings.length);
            switch(strings[0].toLowerCase()){
                case "set":
                    return setTag.onTabComplete(commandSender, command, s, subArgs);
                case "remove":
                    return removeTag.onTabComplete(commandSender, command, s, subArgs);
                default:
                    return null;
            }
        }
        return null;
    }
}
