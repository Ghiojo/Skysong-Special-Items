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
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.api.event.ItemChargesChangeEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Charge and property writes through {@code charges()} and {@code properties()}, which
 * follow the same rules as {@code /si charges} and {@code /si prop}. Uses the "test" and
 * "charges" properties from the bundled config.yml.
 */
class ItemChargeAndPropertyWritesTest extends MockBukkitTest {
    private ItemCharges charges;
    private ItemProperties properties;
    private RecordedItemEvents events;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        charges = SkysongItemsApi.get().charges();
        properties = SkysongItemsApi.get().properties();
        events = RecordedItemEvents.listenOn(server);
    }

    private static ItemStack skysongItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack chargedItem(int charges, int maxCharges) {
        ItemStack item = skysongItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges);
        meta.getPersistentDataContainer().set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, maxCharges);
        ItemData.from(meta).setPropertiesText("charges");
        item.setItemMeta(meta);
        return item;
    }

    @Test
    void setIsCappedAtTheMaxAndReturnsTheNewCharges() {
        ItemStack item = chargedItem(2, 5);

        assertEquals(5, charges.set(item, 9));
        assertEquals(5, charges.get(item).orElseThrow());
    }

    @Test
    void setOnAnItemWithoutChargesMakesItCharged() {
        ItemStack item = skysongItem();

        assertEquals(4, charges.set(item, 4));

        assertEquals(4, charges.getMax(item).orElseThrow());
        assertTrue(properties.has(item, "charges"));
    }

    @Test
    void setRefusesANegativeAmount() {
        ItemStack item = chargedItem(2, 5);

        assertThrows(IllegalArgumentException.class, () -> charges.set(item, -1));
        assertEquals(2, charges.get(item).orElseThrow());
    }

    @Test
    void addFiresAChargesEventWithTheHolderAndApiCause() {
        PlayerMock player = server.addPlayer();
        ItemStack item = chargedItem(2, 5);

        assertEquals(3, charges.add(player, item, 1));

        ItemChargesChangeEvent event = events.onlyChargeChange();
        assertSame(player, event.getHolder());
        assertEquals(ItemChangeCause.API, event.getCause());
        assertEquals(2, event.getOldCharges());
        assertEquals(3, event.getNewCharges());
    }

    @Test
    void addStaysBetweenZeroAndTheMax() {
        ItemStack item = chargedItem(2, 5);

        assertEquals(0, charges.add(item, -10));
        assertEquals(5, charges.add(item, 10));
    }

    @Test
    void addRefusesAnItemWithoutCharges() {
        assertThrows(IllegalArgumentException.class, () -> charges.add(skysongItem(), 1));
    }

    @Test
    void addRespectsAnItemThatDoesNotAllowAddingCharges() {
        ItemStack item = chargedItem(2, 5);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, false);
        item.setItemMeta(meta);

        assertThrows(IllegalArgumentException.class, () -> charges.add(item, 1));
        assertEquals(1, charges.add(item, -1));
    }

    @Test
    void setMaxRefusesLessThanOne() {
        ItemStack item = chargedItem(2, 5);

        assertThrows(IllegalArgumentException.class, () -> charges.setMax(item, 0));
        charges.setMax(item, 8);
        assertEquals(8, charges.getMax(item).orElseThrow());
    }

    @Test
    void chargeWritesRefuseAPlainItem() {
        ItemStack item = new ItemStack(Material.STONE);

        assertThrows(IllegalArgumentException.class, () -> charges.set(item, 1));
        assertFalse(charges.get(item).isPresent());
    }

    @Test
    void addPropertyReportsWhetherItWasAdded() {
        ItemStack item = skysongItem();

        assertTrue(properties.add(item, "test"));
        assertFalse(properties.add(item, "test"));
        assertEquals(List.of("test"), properties.get(item));
    }

    @Test
    void addPropertyRefusesAnUnknownProperty() {
        assertThrows(IllegalArgumentException.class, () -> properties.add(skysongItem(), "nope"));
    }

    @Test
    void removePropertyReportsWhetherItWasRemoved() {
        ItemStack item = skysongItem();
        properties.add(item, "test");

        assertTrue(properties.remove(item, "test"));
        assertFalse(properties.remove(item, "test"));
        assertTrue(properties.get(item).isEmpty());
    }

    @Test
    void propertyWritesRefuseAPlainItem() {
        assertThrows(IllegalArgumentException.class, () -> properties.add(new ItemStack(Material.STONE), "test"));
    }
}
