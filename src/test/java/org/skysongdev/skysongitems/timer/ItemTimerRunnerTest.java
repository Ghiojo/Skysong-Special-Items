package org.skysongdev.skysongitems.timer;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests applying the timers that are due on an item. The bundled config.yml defines the
 * int tag arcrot and the trait test.
 */
class ItemTimerRunnerTest extends MockBukkitTest {
    private static final long NOW = Instant.parse("2026-11-17T12:00:00Z").toEpochMilli();
    private static final NamespacedKey ARCROT = ItemKeys.named("arcrot");

    private final List<Long> countedTimes = new ArrayList<>();
    private ItemTimerRunner runner;

    @BeforeEach
    void setUp() {
        SkysongItems plugin = MockBukkit.load(SkysongItems.class);
        TimerActions actions = new TimerActions();
        BuiltInTimerActions.registerAll(actions);
        actions.register(new TimerAction() {
            @Override
            public String id() {
                return "count";
            }

            @Override
            public ItemStack apply(TimerContext context, ItemStack item, ItemTimer timer, long times) {
                countedTimes.add(times);
                return item;
            }

            @Override
            public String describe(ItemTimer timer) {
                return "count";
            }
        });
        TimerClocks clocks = new TimerClocks(new RealTimeClock(Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC)));
        runner = new ItemTimerRunner(clocks, actions, plugin::getItemEditor);
    }

    private static ItemStack itemWith(ItemTimer... timers) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        ItemData data = new ItemData(meta.getPersistentDataContainer());
        data.setLoreSections(List.of("physical", "functional", "skill"));
        data.addTrait("test");
        item.setItemMeta(meta);
        ItemTimerStore.write(item, List.of(timers));
        return item;
    }

    private static ItemTimer once(long due, String action, String target, String value) {
        return new ItemTimer("real", due, action, target, value, null, null);
    }

    private static boolean hasTrait(ItemStack item) {
        return ItemData.from(item.getItemMeta()).hasTrait("test");
    }

    private static Integer arcrot(ItemStack item) {
        return item.getPersistentDataContainer().get(ARCROT, PersistentDataType.INTEGER);
    }

    private static List<ItemTimer> timersOn(ItemStack item) {
        return ItemTimerStore.read(item.getPersistentDataContainer());
    }

    @Test
    void appliesADueTimerAndForgetsIt() {
        ItemStack item = itemWith(once(NOW - 1, "remove_trait", "test", null));

        assertTrue(runner.run(item, null).isPresent());

        assertFalse(hasTrait(item));
        assertFalse(ItemTimerStore.hasTimers(item.getPersistentDataContainer()));
    }

    @Test
    void leavesTimersThatAreNotDueYet() {
        ItemTimer later = once(NOW + 60_000, "remove_trait", "test", null);
        ItemStack item = itemWith(later);

        assertTrue(runner.run(item, null).isEmpty());

        assertTrue(hasTrait(item));
        assertEquals(List.of(later), timersOn(item));
    }

    @Test
    void appliesDueTimersOldestFirst() {
        ItemStack item = itemWith(once(NOW - 10, "remove_tag", "arcrot", null), once(NOW - 20, "set_tag", "arcrot", "3"));

        runner.run(item, null);

        assertNull(arcrot(item));
    }

    @Test
    void aRepeatingTimerHandsItsActionEveryMissedRepeatAtOnceAndKeepsItsSchedule() {
        // Due on August 17, then September, October and November 17: four times by now.
        long firstDue = Instant.parse("2026-08-17T09:00:00Z").toEpochMilli();
        ItemStack item = itemWith(new ItemTimer("real", firstDue, "count", null, null, "1mo", "1mo"));

        runner.run(item, null);

        assertEquals(List.of(4L), countedTimes);
        assertEquals(List.of(new ItemTimer("real", Instant.parse("2026-12-17T09:00:00Z").toEpochMilli(), "count",
                null, null, "1mo", "1mo")), timersOn(item));
    }

    @Test
    void aTimerWhoseTargetIsGoneClearsItselfWhenItRunsOnce() {
        ItemStack item = itemWith(once(NOW, "remove_trait", "deleted_trait", null));

        runner.run(item, null);

        assertEquals(List.of(), timersOn(item));
    }

    @Test
    void timersOfUnknownClocksOrActionsWait() {
        ItemTimer unknownAction = once(NOW - 1, "summon_dragon", null, null);
        ItemTimer unknownClock = new ItemTimer("season", 0, "remove_trait", "test", null, null, null);
        ItemStack item = itemWith(unknownAction, unknownClock);

        assertTrue(runner.run(item, null).isEmpty());

        assertEquals(List.of(unknownAction, unknownClock), timersOn(item));
    }

    @Test
    void appliesTheTimersOfEveryItemInAnInventory() {
        Inventory chest = server.createInventory(null, 9);
        chest.setItem(0, itemWith(once(NOW, "remove_trait", "test", null)));
        chest.setItem(1, itemWith(once(NOW + 5000, "remove_trait", "test", null)));

        assertEquals(1, runner.runAll(chest, null));

        assertFalse(hasTrait(chest.getItem(0)));
        assertTrue(hasTrait(chest.getItem(1)));
    }

    @Test
    void reportsTheChangeAsATimerChangeByTheHolder() {
        PlayerMock player = server.addPlayer();
        ItemStack item = itemWith(once(NOW, "set_tag", "arcrot", "2"));
        RecordedItemEvents events = RecordedItemEvents.listenOn(server);

        runner.run(item, player);

        assertEquals(ItemChangeCause.TIMER, events.onlyTagChange().getCause());
        assertSame(player, events.onlyTagChange().getHolder());
    }
}
