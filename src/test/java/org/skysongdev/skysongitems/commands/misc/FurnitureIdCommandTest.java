package org.skysongdev.skysongitems.commands.misc;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.FakeCraftEngine;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests {@code /si furniture} against a fake CraftEngine. */
class FurnitureIdCommandTest extends MockBukkitTest {
    private static final String TABLE_ID = "skysong:oak_table";

    private final FakeCraftEngine craftEngine = new FakeCraftEngine().load(TABLE_ID).load("skysong:armchair");
    private final FurnitureIdCommand furnitureCommand = new FurnitureIdCommand(() -> Optional.of(craftEngine));
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        // The messages are built with the plugin's MiniMessage instance.
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    @Test
    void refusedWithoutCraftEngine() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        new FurnitureIdCommand(Optional::empty).onCommand(player, new StubCommand(), "si", new String[]{"set", TABLE_ID});

        assertEquals("[SkysongItems] CraftEngine is not installed, so items cannot be given a CraftEngine id.", nextPlainMessage());
    }

    @Test
    void emptyHandIsRefused() {
        run("set", TABLE_ID);

        assertEquals("[SkysongItems] You must hold an item in your main hand to use this command.", nextPlainMessage());
    }

    @Test
    void setGivesTheHeldItemTheId() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        run("set", TABLE_ID);

        assertEquals(Optional.of(TABLE_ID), craftEngine.idOf(heldItem()));
        assertEquals("[SkysongItems] CraftEngine id set to skysong:oak_table.", nextPlainMessage());
        assertNull(player.nextMessage(), "a loaded id needs no warning");
    }

    @Test
    void setKeepsTheSkysongTags() {
        ItemStack item = new ItemStack(Material.OAK_PLANKS);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, "Oak Table");
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);

        run("set", TABLE_ID);

        assertEquals("Oak Table", heldItem().getPersistentDataContainer().get(ItemKeys.NAME, PersistentDataType.STRING));
    }

    @Test
    void setWithAnIdCraftEngineHasNotLoadedWarnsButSetsIt() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        run("set", "skysong:not_configured");

        assertEquals(Optional.of("skysong:not_configured"), craftEngine.idOf(heldItem()));
        assertEquals("[SkysongItems] CraftEngine id set to skysong:not_configured.", nextPlainMessage());
        assertEquals("[SkysongItems] CraftEngine has no item with the id skysong:not_configured loaded,"
                + " so the item will not place as furniture until it does.", nextPlainMessage());
    }

    @Test
    void setWithoutAnIdShowsTheUsage() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        run("set");

        assertEquals("[SkysongItems] Usage: /si furniture set <namespace:id> (lowercase, for example skysong:oak_table)",
                nextPlainMessage());
    }

    @Test
    void setRefusesAnIdWithoutNamespaceOrInUppercase() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        run("set", "oak_table");
        run("set", "Skysong:Oak_Table");

        assertTrue(craftEngine.idOf(heldItem()).isEmpty());
        assertTrue(nextPlainMessage().startsWith("[SkysongItems] Usage: /si furniture set"));
        assertTrue(nextPlainMessage().startsWith("[SkysongItems] Usage: /si furniture set"));
    }

    @Test
    void removeTakesTheIdOff() {
        player.getInventory().setItemInMainHand(craftEngine.withId(new ItemStack(Material.OAK_PLANKS), TABLE_ID));

        run("remove");

        assertTrue(craftEngine.idOf(heldItem()).isEmpty());
        assertEquals("[SkysongItems] CraftEngine id removed.", nextPlainMessage());
    }

    @Test
    void removeOnAnItemWithoutAnIdSaysSo() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        run("remove");

        assertEquals("[SkysongItems] This item has no CraftEngine id.", nextPlainMessage());
    }

    @Test
    void noArgumentsShowsTheId() {
        player.getInventory().setItemInMainHand(craftEngine.withId(new ItemStack(Material.OAK_PLANKS), TABLE_ID));

        run();

        assertEquals("[SkysongItems] This item's CraftEngine id is skysong:oak_table.", nextPlainMessage());
    }

    @Test
    void noArgumentsOnAnItemWithoutAnIdSaysSo() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        run();

        assertEquals("[SkysongItems] This item has no CraftEngine id.", nextPlainMessage());
    }

    @Test
    void unknownActionShowsTheUsage() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_PLANKS));

        run("clear");

        assertEquals("[SkysongItems] Usage: /si furniture [set <namespace:id> | remove]", nextPlainMessage());
    }

    @Test
    void tabCompletionOffersTheActions() {
        assertEquals(List.of("set", "remove"), tabComplete(""));
        assertEquals(List.of("remove"), tabComplete("r"));
    }

    @Test
    void tabCompletionOffersLoadedIdsForSet() {
        assertEquals(List.of("skysong:armchair", TABLE_ID), tabComplete("set", ""));
        assertEquals(List.of(TABLE_ID), tabComplete("set", "skysong:o"));
    }

    @Test
    void tabCompletionOffersNoIdsWithoutCraftEngine() {
        FurnitureIdCommand withoutCraftEngine = new FurnitureIdCommand(Optional::empty);

        assertEquals(List.of(), withoutCraftEngine.onTabComplete(player, new StubCommand(), "si", new String[]{"set", ""}));
    }

    private ItemStack heldItem() {
        return player.getInventory().getItemInMainHand();
    }

    private void run(String... args) {
        furnitureCommand.onCommand(player, new StubCommand(), "si", args);
    }

    private List<String> tabComplete(String... args) {
        return furnitureCommand.onTabComplete(player, new StubCommand(), "si", args);
    }

    private String nextPlainMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    /** The command never looks at the command object, so any instance will do. */
    private static final class StubCommand extends Command {
        private StubCommand() {
            super("si");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            return true;
        }
    }
}
