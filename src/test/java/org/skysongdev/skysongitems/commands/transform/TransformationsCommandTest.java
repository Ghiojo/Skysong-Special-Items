package org.skysongdev.skysongitems.commands.transform;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.transform.Transformation;
import org.skysongdev.skysongitems.transform.TransformationRules;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Tests {@code /si transform list} and {@code /si transform check}. */
class TransformationsCommandTest extends MockBukkitTest {
    private static final String SKIPPED = "transformations.yml: skipped the transformation of sun_staff: weapons/missing is not a saved item.";

    private PlayerMock player;
    private Command command;
    private final TransformationsCommand transform = new TransformationsCommand(TransformationsCommandTest::rules);

    @BeforeEach
    void setUp() {
        // The messages are built with the plugin's MiniMessage instance.
        command = MockBukkit.load(SkysongItems.class).getCommand("skysongitems");
        player = server.addPlayer();
    }

    private static TransformationRules rules() {
        Map<NamespacedKey, String> tags = Map.of(ItemKeys.MATERIAL_ID, "night_blade");
        SkysongItem night = new SkysongItem("Night", new ArrayList<>(List.of("physical")), "IRON_SWORD", null, "", "", tags);
        Transformation rule = new Transformation("day_blade", "weapons/night_blade", night, "night_blade", null, null);
        return new TransformationRules(Map.of("day_blade", rule), List.of(SKIPPED));
    }

    private void run(String... args) {
        transform.onCommand(player, command, "si", args);
    }

    private void hold(String materialId) {
        ItemStack item = TaggedItems.stones(1).with(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, materialId).build();
        player.getInventory().setItemInMainHand(item);
    }

    private String nextPlainMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    @Test
    void listShowsEveryRuleThenEverySkippedRule() {
        run("list");

        assertEquals("[SkysongItems] 1 transformations:", nextPlainMessage());
        assertEquals("[SkysongItems] day_blade -> night_blade (weapons/night_blade)", nextPlainMessage());
        assertEquals("[SkysongItems] " + SKIPPED, nextPlainMessage());
        assertNull(player.nextComponentMessage());
    }

    @Test
    void checkShowsWhatTheHeldFormBecomesWithoutChangingIt() {
        hold("day_blade");
        ItemStack before = player.getInventory().getItemInMainHand().clone();

        run("check");

        assertEquals("[SkysongItems] This item is in the form day_blade and transforms into night_blade (weapons/night_blade).",
                nextPlainMessage());
        assertEquals(before, player.getInventory().getItemInMainHand());
    }

    @Test
    void checkOnAFormWithoutARuleSaysSo() {
        hold("night_blade");

        run("check");

        assertEquals("[SkysongItems] This item is in the form night_blade, which has no transformation.", nextPlainMessage());
    }

    @Test
    void checkOnAnItemWithoutAMaterialIdSaysItCannotTransform() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.STONE));

        run("check");

        assertEquals("[SkysongItems] This item has no material_id, so it cannot transform.", nextPlainMessage());
    }

    @Test
    void anUnknownActionShowsTheUsage() {
        run("swap");

        assertEquals("[SkysongItems] Usage: /si transform <list | check>", nextPlainMessage());
    }
}
