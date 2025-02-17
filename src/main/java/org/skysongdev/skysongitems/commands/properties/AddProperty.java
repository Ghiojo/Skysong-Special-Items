package org.skysongdev.skysongitems.commands.properties;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.commands.misc.UpdateItem;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.PrintableProperty;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class AddProperty implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("This command can only be used by players."));
            return true;
        }
        Player player = (Player) commandSender;



        if(args.length < 1){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "Usage: /properties add <property>"));
            return true;
        }

        getPlugin().getUpdater().UpdateLore(player);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand == null || itemInMainHand.getType().isAir()) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "You must hold an item in your main hand to use this command."));
            return true;
        }

        if(!Utils.isAProperty(args[0])){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "Invalid property!"));
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "Valid properties: " + Utils.properties.stream().map(Property::getId).toList()));
            return true;
        }

        ItemMeta meta = itemInMainHand.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if(container.has(Utils.propertiesKey)){
            String[] currentProperties = container.get(Utils.propertiesKey, PersistentDataType.STRING).split(";");
            for(String property : currentProperties){
                if(property.equals(args[0])){
                    player.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "This item already has this property!"));
                    return true;
                }
            }
            container.set(Utils.propertiesKey, PersistentDataType.STRING, container.get(Utils.propertiesKey, PersistentDataType.STRING) + ";" + args[0]);
        } else {
            container.set(Utils.propertiesKey, PersistentDataType.STRING, args[0]);
        }

        if(Utils.getProperty(args[0]) != null){
            Property property = Utils.getProperty(args[0]);
            if(property instanceof PrintableProperty){
                ArrayList<String> values = ((PrintableProperty) property).getValuetags();
                for(String value : values) {
                    PersistentDataType type = getPlugin().getUtils().tagList.get(new NamespacedKey(getPlugin(), value));
                    if (type == PersistentDataType.STRING) {
                        container.set(new NamespacedKey(getPlugin(), value), PersistentDataType.STRING, "");
                    } else if (type == PersistentDataType.INTEGER) {
                        container.set(new NamespacedKey(getPlugin(), value), PersistentDataType.INTEGER, 0);
                    } else if (type == PersistentDataType.FLOAT) {
                        container.set(new NamespacedKey(getPlugin(), value), PersistentDataType.FLOAT, 0.0f);
                    } else if (type == PersistentDataType.BOOLEAN) {
                        container.set(new NamespacedKey(getPlugin(), value), PersistentDataType.BOOLEAN, false);
                    }
                }
            }
        }

        itemInMainHand.setItemMeta(meta);
        player.updateInventory();
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "Property added!"));

        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));


        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> properties = new ArrayList<>();
        for(Property property : Utils.properties){
            properties.add(property.getId());
        }
        return properties.stream().filter(a -> a.startsWith(args[0])).toList();
    }
}
