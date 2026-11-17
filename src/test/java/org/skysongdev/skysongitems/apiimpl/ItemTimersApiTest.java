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
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.utils.Constants;
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
}
