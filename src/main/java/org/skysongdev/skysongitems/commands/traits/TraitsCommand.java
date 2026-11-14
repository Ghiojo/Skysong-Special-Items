package org.skysongdev.skysongitems.commands.traits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class TraitsCommand implements TabExecutor {
    /** Permission needed to add or remove traits, as declared in plugin.yml. */
    static final String EDIT_PERMISSION = "skysongspecialitems.cit";

    private final AddTrait addTrait;
    private final RemoveTrait removeTrait;
    private final ViewTraits viewTraits;

    public TraitsCommand() {
        this(new AddTrait(), new RemoveTrait(), new ViewTraits());
    }

    /**
     * Lets tests replace the sub-command handlers, so the permission check can
     * be verified without running them.
     */
    TraitsCommand(AddTrait addTrait, RemoveTrait removeTrait, ViewTraits viewTraits) {
        this.addTrait = addTrait;
        this.removeTrait = removeTrait;
        this.viewTraits = viewTraits;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1){
            sender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<gray>Usage: /properties <sub-command>"));
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch(args[0].toLowerCase()){
            case "add":
                if(sender.hasPermission(EDIT_PERMISSION)){
                    addTrait.onCommand(sender, command, label, subArgs);
                } else{
                    sender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>You do not have permission to use this command."));
                }
                break;
            case "remove":
                if(sender.hasPermission(EDIT_PERMISSION)){
                    removeTrait.onCommand(sender, command, label, subArgs);
                } else{
                    sender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>You do not have permission to use this command."));
                }
                break;
            case "view":
                viewTraits.onCommand(sender, command, label, subArgs);
                break;
            default:
                sender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Invalid sub-command!"));
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
                    return addTrait.onTabComplete(sender, command, label, subArgs);
                case "remove":
                    return removeTrait.onTabComplete(sender, command, label, subArgs);
                default:
                    return List.of("");
            }
        }
        return List.of("");
    }
}
