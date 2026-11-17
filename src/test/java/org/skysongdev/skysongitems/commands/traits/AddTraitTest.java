package org.skysongdev.skysongitems.commands.traits;

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
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the messages of {@code /si trait add}, using the "test" base trait of the bundled config.yml. */
class AddTraitTest extends MockBukkitTest {
    private final AddTrait addTrait = new AddTrait();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
    }

    @Test
    void noTraitIdShowsTheAddUsage() {
        run();

        assertEquals("[SkysongItems] Usage: /si trait add <trait> [time]", nextMessage());
    }

    @Test
    void addingATraitConfirms() {
        run("test");

        assertEquals("[SkysongItems] Trait added!", nextMessage());
    }

    @Test
    void addingATraitTwiceSaysTheItemHasIt() {
        run("test");
        nextMessage();

        run("test");

        assertEquals("[SkysongItems] This item already has this trait!", nextMessage());
    }

    @Test
    void addingATraitForATimeSchedulesItsRemovalAndAddingItAgainRestartsIt() {
        long before = System.currentTimeMillis();
        run("test", "24h");
        assertEquals("[SkysongItems] Trait added for 24h!", nextMessage());

        run("test", "2d");

        assertEquals("[SkysongItems] This item already has this trait; it now lasts for 2d.", nextMessage());
        List<ItemTimer> timers = heldTimers();
        assertEquals(1, timers.size());
        assertEquals("remove_trait", timers.get(0).action());
        assertTrue(timers.get(0).due() >= before + 2 * 86_400_000L);
    }

    @Test
    void addingATimedTraitAgainWithoutATimeMakesItPermanent() {
        run("test", "24h");
        nextMessage();

        run("test");

        assertEquals("[SkysongItems] This item's trait is now permanent.", nextMessage());
        assertTrue(heldTimers().isEmpty());
    }

    @Test
    void aTimeThatIsNotOneIsRefused() {
        run("test", "tomorrow");

        assertTrue(nextMessage().startsWith("[SkysongItems] Invalid time!"));
        assertTrue(heldTimers().isEmpty());
    }

    private List<ItemTimer> heldTimers() {
        return ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer());
    }

    @Test
    void unknownTraitIsRefusedWithTheValidTraits() {
        run("not_a_trait");

        assertEquals("[SkysongItems] Invalid trait!", nextMessage());
        assertTrue(nextMessage().startsWith("[SkysongItems] Valid traits: ["));
    }

    private void run(String... args) {
        addTrait.onCommand(player, new StubCommand(), "si", args);
    }

    private String nextMessage() {
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
