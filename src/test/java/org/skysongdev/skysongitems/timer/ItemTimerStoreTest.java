package org.skysongdev.skysongitems.timer;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Tests storing timers on an item. */
class ItemTimerStoreTest extends MockBukkitTest {
    private static final long DUE = 1_795_521_600_000L;
    private static final ItemTimer HONED_ENDS = new ItemTimer("real", DUE, "remove_trait", "honed", null, "24h", null);

    @Test
    void readsBackWhatItWrote() {
        ItemStack item = TaggedItems.stones(1).build();
        List<ItemTimer> timers = List.of(HONED_ENDS,
                new ItemTimer("real", DUE, "set_tag", "arcrot", "3", "1mo", "1mo"));

        ItemTimerStore.write(item, timers);

        assertEquals(timers, ItemTimerStore.read(item.getPersistentDataContainer()));
    }

    @Test
    void keepsTimersOfClocksAndActionsNobodyRegistered() {
        ItemStack item = TaggedItems.stones(1).build();
        List<ItemTimer> timers = List.of(new ItemTimer("season", 12, "summon_dragon", null, null, null, "1season"));

        ItemTimerStore.write(item, timers);

        assertEquals(timers, ItemTimerStore.read(item.getPersistentDataContainer()));
    }

    @Test
    void writingNoTimersRemovesTheTag() {
        ItemStack item = TaggedItems.stones(1).build();
        ItemTimerStore.write(item, List.of(HONED_ENDS));

        ItemTimerStore.write(item, List.of());

        assertFalse(ItemTimerStore.hasTimers(item.getPersistentDataContainer()));
    }

    @Test
    void skipsEntriesWithoutADueTime() {
        ItemStack item = TaggedItems.stones(1).build();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer entry = meta.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
        entry.set(ItemKeys.named("action"), PersistentDataType.STRING, "remove_trait");
        meta.getPersistentDataContainer().set(ItemKeys.TIMERS, PersistentDataType.LIST.dataContainers(), List.of(entry));
        item.setItemMeta(meta);

        assertEquals(List.of(), ItemTimerStore.read(item.getPersistentDataContainer()));
    }
}
