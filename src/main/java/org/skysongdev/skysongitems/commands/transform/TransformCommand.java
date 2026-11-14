package org.skysongdev.skysongitems.commands.transform;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.transform.ItemTransformer;
import org.skysongdev.skysongitems.transform.Transformation;
import org.skysongdev.skysongitems.transform.TransformationMessages;
import org.skysongdev.skysongitems.transform.TransformationRules;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.function.Supplier;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /transform} (alias {@code /tf}): turns the item in the player's main hand into its
 * next form, or says it doesn't transform. See {@link ItemTransformer}.
 */
public class TransformCommand implements CommandExecutor {
    private final ItemTransformer transformer;
    private final Supplier<TransformationRules> rules;

    /** @param rules looked up on each use, for the messages, since /si reload replaces them */
    public TransformCommand(ItemTransformer transformer, Supplier<TransformationRules> rules) {
        this.transformer = transformer;
        this.rules = rules;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "<red>Only players can transform the item they hold.");
            return true;
        }
        // Read before transforming, as the held item then is in its new form.
        TransformationRules current = rules.get();
        String ruleMessage = current.find(heldMaterialId(player)).map(Transformation::message).orElse(null);
        TransformationMessages messages = current.messages();
        String message = switch (transformer.transform(player)) {
            case TRANSFORMED -> ruleMessage != null ? ruleMessage : messages.transformed();
            case NOT_HOLDING -> messages.notHolding();
            case NO_PERMISSION -> messages.noPermission();
            // The listener that cancelled the transformation says why, if it should.
            case CANCELLED -> null;
            default -> messages.noTransformation();
        };
        if (message != null) {
            send(player, message);
        }
        return true;
    }

    private static String heldMaterialId(Player player) {
        return ItemTransformer.materialIdOf(player.getInventory().getItemInMainHand());
    }

    private static void send(CommandSender receiver, String message) {
        receiver.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }
}
