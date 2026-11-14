package org.skysongdev.skysongitems.commands.traits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.Trait;
import org.skysongdev.skysongitems.utils.TraitRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si details}: lists the traits of the item in the player's main
 * hand, each with its type and description on hover. Trait ids that are no
 * longer in config.yml are left out, so an item with only those has no traits.
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
        String description = data.hasTraits() ? describe(data.getTraits()) : "";
        if (description.isEmpty()) {
            player.sendMessage("This item has no traits.");
            return true;
        }

        player.sendMessage(getPlugin().getMiniMessage().deserialize("<#864811>Item traits:"));
        player.sendMessage(getPlugin().getMiniMessage().deserialize(description));
        return true;
    }

    private static String describe(List<String> traitIds) {
        TraitRegistry registry = getPlugin().getTraitRegistry();
        List<String> entries = new ArrayList<>();
        for (String id : traitIds) {
            Trait trait = registry.get(id);
            if (trait != null) {
                entries.add(describe(trait));
            }
        }
        return String.join(SEPARATOR, entries);
    }

    private static String describe(Trait trait) {
        return "<gold><hover:show_text:\"<gold>" + trait.getName()
                + "<newline><gray><i>" + trait.getType() + " trait"
                + "<newline><newline><gray>" + trait.getDescription() + "\">"
                + trait.getName() + "</hover></gold>";
    }
}
