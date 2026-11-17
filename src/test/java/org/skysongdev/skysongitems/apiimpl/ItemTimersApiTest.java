package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemTimer;
import org.skysongdev.skysongitems.api.ItemTemplate;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.TemplateTimer;
import org.skysongdev.skysongitems.api.TimerChange;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Item timers through {@code timers()}. */
class ItemTimersApiTest extends MockBukkitTest {
    private static final org.skysongdev.skysongitems.timer.ItemTimer HONED_ENDS =
            new org.skysongdev.skysongitems.timer.ItemTimer("real", 1000, "remove_trait", "honed", null, "24h", null);
    private static final org.skysongdev.skysongitems.timer.ItemTimer MONTHLY =
            new org.skysongdev.skysongitems.timer.ItemTimer("real", 2000, "set_tag", "arcrot", "3", "1mo", "1mo");

    private SkysongItemsApi api;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        api = SkysongItemsApi.get();
    }

    private static ItemStack skysongItemWithTimers() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        item.setItemMeta(meta);
        ItemTimerStore.write(item, List.of(HONED_ENDS, MONTHLY));
        return item;
    }

    @Test
    void listsTheTimersAsStored() {
        assertEquals(List.of(new ItemTimer("real", 1000, "remove_trait", "honed", null, "24h", null),
                        new ItemTimer("real", 2000, "set_tag", "arcrot", "3", "1mo", "1mo")),
                api.timers().list(skysongItemWithTimers()));
        assertEquals(List.of(), api.timers().list(null));
    }

    @Test
    void afterAndEveryStoreTheChangeAndReplaceTheSameChangesEarlierTimer() {
        ItemStack item = skysongItemWithTimers();
        long before = System.currentTimeMillis();

        ItemTimer later = api.timers().after(item, TimerChange.setTag("arcrot", 5), "1d");
        ItemTimer monthly = api.timers().every(item, TimerChange.addToTag("arcrot", -1), "1mo");

        assertEquals("set_tag", later.action());
        assertEquals("5", later.value());
        assertTrue(later.due() >= before + 86_400_000L);
        assertEquals("1mo", monthly.every());
        assertFalse(api.tags().getInt(item, "arcrot").isPresent());
        // The new set_tag timer on arcrot replaced the monthly one that was stored before.
        assertEquals(List.of("remove_trait", "set_tag", "add_tag"),
                api.timers().list(item).stream().map(ItemTimer::action).toList());
    }

    @Test
    void afterRefusesAChangeTheItemCanNeverTakeButNotOneItJustDoesNotNeedYet() {
        ItemStack item = skysongItemWithTimers();

        assertThrows(IllegalArgumentException.class, () -> api.timers().after(item, TimerChange.setTag("arcrot", "three"), "1d"));
        assertThrows(IllegalArgumentException.class, () -> api.timers().after(item, TimerChange.addTrait("nope"), "1d"));
        assertThrows(IllegalArgumentException.class, () -> api.timers().after(item, TimerChange.removeTrait("test"), "soon"));
        assertEquals(2, api.timers().list(item).size());

        api.timers().after(item, TimerChange.removeTrait("test"), "1d");
        assertEquals(3, api.timers().list(item).size());
    }

    @Test
    void cancelRemovesOneTimerAndClearRemovesAll() {
        ItemStack item = skysongItemWithTimers();

        assertTrue(api.timers().cancel(item, 0));
        assertFalse(api.timers().cancel(item, 5));
        assertEquals(List.of(MONTHLY), ItemTimerStore.read(item.getPersistentDataContainer()));

        api.timers().clear(item);
        assertEquals(List.of(), ItemTimerStore.read(item.getPersistentDataContainer()));
    }

    @Test
    void cancellingOnAPlainItemIsRefusedExceptDetached() {
        ItemStack plain = new ItemStack(Material.STONE);
        ItemTimerStore.write(plain, List.of(HONED_ENDS));

        assertThrows(IllegalArgumentException.class, () -> api.timers().clear(plain));
        api.detached().timers().clear(plain);
        assertEquals(List.of(), ItemTimerStore.read(plain.getPersistentDataContainer()));
    }

    @Test
    void aTemplateKeepsTheItemsTimersAsSpansAndEachCreatedItemStartsThemAgain() {
        ItemStack item = skysongItemWithTimers();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, "Blade");
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        long before = System.currentTimeMillis();

        ItemTemplate template = api.templates().snapshot(item);

        assertEquals(List.of(new TemplateTimer(TimerChange.removeTrait("honed"), "24h", false, "real"),
                new TemplateTimer(TimerChange.setTag("arcrot", 3), "1mo", true, "real")), template.timers());
        assertTrue(api.timers().list(template.create()).get(0).due() >= before + 86_400_000L);
    }
}
