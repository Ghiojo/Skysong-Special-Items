package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.api.event.ItemTagChangeEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Item HP through {@code itemHp()} and the {@code getItemHp}/{@code hasItemHp} shortcuts. */
class ItemHpApiTest extends MockBukkitTest {
    private SkysongItemsApi api;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        api = SkysongItemsApi.get();
    }

    private static ItemStack skysongItem() {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        return item;
    }

    @Test
    void anItemWithoutHpReadsAsEmptyAndHasNoneLeft() {
        ItemStack item = skysongItem();

        assertEquals(OptionalInt.empty(), api.itemHp().get(item));
        assertEquals(0, api.getItemHp(item));
        assertFalse(api.hasItemHp(null));
    }

    @Test
    void writesAreReadBackAndHpIsSeparateFromCharges() {
        ItemStack item = skysongItem();

        api.itemHp().setMax(item, 10);

        assertEquals(4, api.itemHp().add(item, -6));
        assertEquals(OptionalInt.of(10), api.itemHp().getMax(item));
        assertTrue(api.hasItemHp(item));
        assertEquals(OptionalInt.empty(), api.charges().get(item));
    }

    @Test
    void damagingAHeldItemFiresATagEventNamingTheHolder() {
        PlayerMock player = server.addPlayer();
        ItemStack item = skysongItem();
        api.itemHp().setMax(item, 10);
        RecordedItemEvents events = RecordedItemEvents.listenOn(server);

        api.itemHp().add(player, item, -10);

        ItemTagChangeEvent event = events.onlyTagChange();
        assertSame(player, event.getHolder());
        assertEquals(ItemChangeCause.API, event.getCause());
        assertEquals(0, event.getNewValue());
        assertFalse(api.hasItemHp(item));
    }

    @Test
    void addingToAnItemWithoutHpIsRefused() {
        ItemStack item = skysongItem();

        IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, () -> api.itemHp().add(item, -1));

        assertTrue(refused.getMessage().contains("the item has no HP"));
    }

    @Test
    void theDetachedViewSetsHpOnAPlainItem() {
        ItemStack plain = new ItemStack(Material.STICK);

        api.detached().itemHp().setMax(plain, 5);

        assertEquals(5, api.getItemHp(plain));
    }
}
