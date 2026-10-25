package org.skysongdev.skysongitems.commands.properties;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.PropertyRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si details}: lists the properties of the item in the player's main
 * hand, each with its type and description on hover. Property ids that are no
 * longer in config.yml are left out, so an item with only those has no properties.
 */
public class DetailsCommand implements CommandExecutor {
    private static final String SEPARATOR = "<#864811>, ";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType().isAir()) {
            player.sendMessage("You must hold an item in your main hand to use this command.");
            return true;
        }

        ItemData data = ItemData.from(itemInMainHand.getItemMeta());
        String description = data.hasProperties() ? describe(data.getProperties()) : "";
        if (description.isEmpty()) {
            player.sendMessage("This item has no properties.");
            return true;
        }

        player.sendMessage(getPlugin().getMiniMessage().deserialize("<#864811>Item properties:"));
        player.sendMessage(getPlugin().getMiniMessage().deserialize(description));
        return true;
    }

    private static String describe(List<String> propertyIds) {
        PropertyRegistry registry = getPlugin().getPropertyRegistry();
        List<String> entries = new ArrayList<>();
        for (String id : propertyIds) {
            Property property = registry.get(id);
            if (property != null) {
                entries.add(describe(property));
            }
        }
        return String.join(SEPARATOR, entries);
    }

    private static String describe(Property property) {
        return "<gold><hover:show_text:\"<gold>" + property.getName()
                + "<newline><gray><i>" + property.getType() + " property"
                + "<newline><newline><gray>" + property.getDescription() + "\">"
                + property.getName() + "</hover></gold>";
    }
}
