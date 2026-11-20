package org.skysongdev.skysongitems.commands.transform;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.transform.ItemTransformer;
import org.skysongdev.skysongitems.transform.Transformation;
import org.skysongdev.skysongitems.transform.TransformationMessages;
import org.skysongdev.skysongitems.transform.TransformationRules;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests the lines {@code /transform} sends. */
class TransformCommandTest extends MockBukkitTest {
    private SkysongItems plugin;
    private PlayerMock player;
    private Command command;
    private TransformationRules rules;

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        command = plugin.getCommand("transform");
        player = server.addPlayer();
        rules = rulesWith(null, TransformationMessages.DEFAULTS);
    }

    private static TransformationRules rulesWith(String ruleMessage, TransformationMessages messages) {
        Map<NamespacedKey, String> tags = Map.of(ItemKeys.MATERIAL_ID, "night_blade");
        SkysongItem night = new SkysongItem("Night", new ArrayList<>(List.of("physical")), "IRON_SWORD", null, "", "", tags);
        Transformation rule = new Transformation("day_blade", "weapons/night_blade", night, "night_blade", null, ruleMessage);
        return new TransformationRules(Map.of("day_blade", rule), List.of(), messages);
    }

    private void run() {
        ItemTransformer transformer = new ItemTransformer(() -> rules, plugin.getTagRegistry(), plugin.getTraitRegistry(), ignored -> { },
                plugin.getUpdater()::upgrade);
        new TransformCommand(transformer, () -> rules).onCommand(player, command, "transform", new String[0]);
    }

    private void holdDayBlade() {
        player.getInventory().setItemInMainHand(
                TaggedItems.stones(1).with(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, "day_blade").build());
    }

    private String nextPlainMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    @Test
    void eachOutcomeSendsItsDefaultLine() {
        run();
        assertEquals("[SkysongItems] Hold the item you want to transform.", nextPlainMessage());

        player.getInventory().setItemInMainHand(new ItemStack(Material.STONE));
        run();
        assertEquals("[SkysongItems] This item doesn't transform.", nextPlainMessage());

        holdDayBlade();
        run();
        assertEquals("[SkysongItems] Your item transforms.", nextPlainMessage());
    }

    @Test
    void aRulesOwnMessageReplacesTheDefaultLine() {
        rules = rulesWith("<gray>The blade darkens.", TransformationMessages.DEFAULTS);
        holdDayBlade();

        run();

        assertEquals("[SkysongItems] The blade darkens.", nextPlainMessage());
    }

    @Test
    void theMessagesSectionReplacesTheLinesItNames() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("messages.no-transformation", "<gray>Nothing happens.");
        rules = rulesWith(null, TransformationMessages.from(yaml.getConfigurationSection("messages")));

        player.getInventory().setItemInMainHand(new ItemStack(Material.STONE));
        run();
        holdDayBlade();
        run();

        assertEquals("[SkysongItems] Nothing happens.", nextPlainMessage());
        assertEquals("[SkysongItems] Your item transforms.", nextPlainMessage(), "lines it leaves out keep their default");
    }
}
