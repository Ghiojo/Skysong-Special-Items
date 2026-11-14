package org.skysongdev.skysongitems.commands.transform;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.transform.ItemTransformer;
import org.skysongdev.skysongitems.transform.Transformation;
import org.skysongdev.skysongitems.transform.TransformationRules;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si transform}: lets staff see the transformations loaded from transformations.yml.
 *
 * <ul>
 *     <li>{@code /si transform list} lists every rule, then every rule skipped on load and why</li>
 *     <li>{@code /si transform check} shows the held item's form and what it would become,
 *     without changing it</li>
 * </ul>
 */
public class TransformationsCommand implements TabExecutor {
    private static final String USAGE = "Usage: /si transform <list | check>";

    private final Supplier<TransformationRules> rules;

    public TransformationsCommand() {
        this(() -> getPlugin().getTransformations());
    }

    /** @param rules looked up on each use, since /si reload replaces them */
    public TransformationsCommand(Supplier<TransformationRules> rules) {
        this.rules = rules;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String action = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "list" -> list(sender, rules.get());
            case "check" -> check(sender, rules.get());
            default -> send(sender, USAGE);
        }
        return true;
    }

    private static void list(CommandSender sender, TransformationRules rules) {
        List<Transformation> all = rules.all();
        send(sender, all.isEmpty() ? "No transformations are loaded." : all.size() + " transformations:");
        for (Transformation rule : all) {
            send(sender, "<gold>" + rule.fromId() + "<gray> -> <gold>" + rule.targetMaterialId()
                    + "<gray> (" + rule.targetId() + ")");
        }
        for (String problem : rules.problems()) {
            send(sender, "<yellow>" + problem);
        }
    }

    private static void check(CommandSender sender, TransformationRules rules) {
        if (!(sender instanceof Player player)) {
            send(sender, "<red>Only players can check the item they hold.");
            return;
        }
        String materialId = ItemTransformer.materialIdOf(player.getInventory().getItemInMainHand());
        if (materialId == null) {
            send(player, "This item has no material_id, so it cannot transform.");
            return;
        }
        Optional<Transformation> rule = rules.find(materialId);
        if (rule.isEmpty()) {
            send(player, "This item is in the form <gold>" + materialId + "<gray>, which has no transformation.");
            return;
        }
        send(player, "This item is in the form <gold>" + materialId + "<gray> and transforms into <gold>"
                + rule.get().targetMaterialId() + "<gray> (" + rule.get().targetId() + ").");
    }

    private static void send(CommandSender receiver, String message) {
        receiver.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + message));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return List.of("list", "check").stream().filter(option -> option.startsWith(args[0])).toList();
    }
}
