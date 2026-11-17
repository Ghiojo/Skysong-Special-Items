package org.skysongdev.skysongitems.commands.timer;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.timer.RealTimeClock;
import org.skysongdev.skysongitems.timer.TimerActions;
import org.skysongdev.skysongitems.timer.TimerClocks;
import org.skysongdev.skysongitems.timer.TimerDescriptions;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests {@code /si timer list}, {@code /si timer clear} and the players' {@code /timers}. */
class TimerCommandTest extends MockBukkitTest {
    private static final long NOW = Instant.parse("2026-11-17T12:00:00Z").toEpochMilli();
    private static final ItemTimer HONED_ENDS = new ItemTimer("real", NOW + 86_399_000, "remove_trait", "honed",
            null, "24h", null);
    private static final ItemTimer MONTHLY_ARCROT = new ItemTimer("real", NOW + 172_800_000, "set_tag", "arcrot",
            "3", "1mo", "1mo");

    private PlayerMock player;
    private TimerCommand timer;

    @BeforeEach
    void setUp() {
        // The messages are built with the plugin's MiniMessage instance.
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
        TimerActions actions = new TimerActions();
        BuiltInTimerActions.registerAll(actions);
        TimerClocks clocks = new TimerClocks(new RealTimeClock(Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC)));
        timer = new TimerCommand(() -> new TimerDescriptions(clocks, actions));
    }

    private void hold(ItemTimer... timers) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemTimerStore.write(item, List.of(timers));
        player.getInventory().setItemInMainHand(item);
    }

    private void run(String... args) {
        timer.onCommand(player, server.getCommandMap().getCommand("skysongitems"), "si", args);
    }

    private List<ItemTimer> heldTimers() {
        return ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer());
    }

    private String nextPlainMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    @Test
    void listsTheTimersWithTheTimeLeft() {
        hold(HONED_ENDS, MONTHLY_ARCROT);

        run("list");

        assertEquals("[SkysongItems] This item has 2 timers:", nextPlainMessage());
        assertEquals("[SkysongItems] 1. trait remove honed in 23h59m59s", nextPlainMessage());
        assertEquals("[SkysongItems] 2. tag set arcrot 3 in 2d, then every 1mo", nextPlainMessage());
    }

    @Test
    void siAppliesTheHeldItemsDueTimersBeforeTheSubcommandRuns() {
        hold(new ItemTimer("real", Instant.now().minusSeconds(1).toEpochMilli(), "remove_trait", "honed", null, null, null));
        player.setOp(true);

        player.performCommand("si timer list");

        assertEquals("[SkysongItems] This item has no timers.", nextPlainMessage());
    }

    @Test
    void playersSeeTheTimersWithTimersWithoutStaffPermission() {
        hold(new ItemTimer("real", Instant.now().plusSeconds(3600).toEpochMilli(), "remove_trait", "honed", null, "1h", null));

        player.performCommand("timers");

        assertEquals("[SkysongItems] This item has 1 timer:", nextPlainMessage());
    }

    @Test
    void clearWithoutANumberCancelsEveryTimer() {
        hold(HONED_ENDS, MONTHLY_ARCROT);

        run("clear");

        assertEquals(List.of(), heldTimers());
    }

    @Test
    void clearWithANumberCancelsThatTimer() {
        hold(HONED_ENDS, MONTHLY_ARCROT);

        run("clear", "1");

        assertEquals(List.of(MONTHLY_ARCROT), heldTimers());
    }

    @Test
    void clearRefusesANumberThatIsNotOnTheList() {
        hold(HONED_ENDS);

        run("clear", "2");

        assertEquals("[SkysongItems] Give a timer number from 1 to 1, as /si timer list shows.", nextPlainMessage());
        assertEquals(List.of(HONED_ENDS), heldTimers());
    }
}
