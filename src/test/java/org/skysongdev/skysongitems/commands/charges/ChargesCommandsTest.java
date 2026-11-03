package org.skysongdev.skysongitems.commands.charges;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.api.event.ItemChargesChangeEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the messages and results of {@code /si charges add}, {@code set},
 * {@code setmax} and {@code addable} on the held item.
 */
class ChargesCommandsTest extends MockBukkitTest {
    private static final String CIT_PERMISSION = "skysongspecialitems.cit";

    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    /** Holds an item at the current version, with 2 of 5 charges unless uncharged. */
    private void holdItem(boolean charged) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData data = ItemData.from(meta);
        data.setLoreSections(List.of("physical", "functional", "skill"));
        if (charged) {
            container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 2);
            container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, 5);
            data.setPropertiesText("charges");
        }
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
    }

    private void lockAdding() {
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, false);
        item.setItemMeta(meta);
    }

    private void run(CommandExecutor executor, String... args) {
        executor.onCommand(player, new StubCommand(), "si", args);
    }

    private String nextMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    private Integer heldCharges() {
        return player.getInventory().getItemInMainHand().getItemMeta()
                .getPersistentDataContainer().get(ItemKeys.CHARGES, PersistentDataType.INTEGER);
    }

    @Test
    void addChargesAddsToTheHeldItem() {
        holdItem(true);

        run(new AddCharges(), "2");

        assertEquals(4, heldCharges());
    }

    @Test
    void addChargesFiresAChargesEventNamingThePlayer() {
        holdItem(true);
        RecordedItemEvents events = RecordedItemEvents.listenOn(server);

        run(new AddCharges(), "2");

        ItemChargesChangeEvent event = events.onlyChargeChange();
        assertSame(player, event.getHolder());
        assertEquals(ItemChangeCause.COMMAND, event.getCause());
        assertEquals(2, event.getOldCharges());
        assertEquals(4, event.getNewCharges());
    }

    @Test
    void addChargesExplainsAnItemWithoutCharges() {
        holdItem(false);

        run(new AddCharges(), "2");

        assertEquals("[SkysongItems] This item doesn't use charges!", nextMessage());
    }

    @Test
    void addChargesRefusesALockedItemWithoutPermission() {
        holdItem(true);
        lockAdding();

        run(new AddCharges(), "2");

        assertEquals("[SkysongItems] You cannot add charges to this item!", nextMessage());
        assertEquals(2, heldCharges());
    }

    @Test
    void addChargesToALockedItemWorksWithPermission() {
        holdItem(true);
        lockAdding();
        player.addAttachment(MockBukkit.createMockPlugin(), CIT_PERMISSION, true);

        run(new AddCharges(), "2");

        assertEquals(4, heldCharges());
    }

    @Test
    void setChargesExplainsANegativeAmount() {
        holdItem(true);

        run(new SetCharges(), "-1");

        assertEquals("[SkysongItems]  \"-1\" is negative!", nextMessage());
    }

    @Test
    void setMaxChargesExplainsZero() {
        holdItem(true);

        run(new SetMaxCharges(), "0");

        assertEquals("[SkysongItems]  \"0\" is negative or zero!", nextMessage());
    }

    @Test
    void addableTogglesAndSaysTheNewSetting() {
        holdItem(true);

        run(new SetAddable());
        run(new SetAddable());

        assertTrue(nextMessage().endsWith("Item charge adding is now enabled"));
        assertTrue(nextMessage().endsWith("Item charge adding is now disabled"));
    }

    @Test
    void addableExplainsAnItemWithoutCharges() {
        holdItem(false);

        run(new SetAddable());

        assertEquals("[SkysongItems] This item doesn't use charges!", nextMessage());
    }

    /** The commands never look at the command object, so any instance will do. */
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
