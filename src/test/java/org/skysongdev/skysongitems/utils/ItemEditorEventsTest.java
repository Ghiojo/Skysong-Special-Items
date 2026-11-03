package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.api.event.ItemChargesChangeEvent;
import org.skysongdev.skysongitems.api.event.ItemTagChangeEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The change events {@link ItemEditor} fires for every edit, so commands, consumption
 * and the API all fire them. Uses the "arcrot" int tag and the "test" and "charges"
 * properties from the bundled config.yml.
 */
class ItemEditorEventsTest extends MockBukkitTest {
    private ItemEditor editor;
    private PlayerMock player;
    private RecordedItemEvents events;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        editor = SkysongItems.getPlugin().getItemEditor();
        player = server.addPlayer();
        events = RecordedItemEvents.listenOn(server);
    }

    private static ItemStack chargedItem(int charges, int maxCharges) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges);
        meta.getPersistentDataContainer().set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, maxCharges);
        ItemData data = ItemData.from(meta);
        data.setLoreSections(List.of("physical", "functional", "skill"));
        data.setPropertiesText("charges");
        item.setItemMeta(meta);
        return item;
    }

    @Test
    void changingChargesFiresOneChargesEventWithTheHolderAndCause() {
        ItemStack item = chargedItem(5, 10);

        editor.forCommand(player).setCharges(item, 3);

        ItemChargesChangeEvent event = events.onlyChargeChange();
        assertSame(player, event.getHolder());
        assertEquals(5, event.getOldCharges());
        assertEquals(3, event.getNewCharges());
        assertEquals(ItemChangeCause.COMMAND, event.getCause());
        assertTrue(events.tagChanges().isEmpty());
    }

    @Test
    void theEventCarriesACopyOfTheItemAfterTheChange() {
        ItemStack item = chargedItem(5, 10);

        editor.forCommand(player).addCharges(item, -2, false);

        ItemStack copy = events.onlyChargeChange().getItem();
        assertEquals(3, copy.getPersistentDataContainer().get(ItemKeys.CHARGES, PersistentDataType.INTEGER));
        copy.setAmount(7);
        assertEquals(1, item.getAmount());
    }

    @Test
    void writingTheSameChargesAgainFiresNothing() {
        ItemStack item = chargedItem(5, 10);

        editor.setCharges(item, 5);

        assertTrue(events.chargeChanges().isEmpty());
    }

    @Test
    void aRefusedEditFiresNothing() {
        ItemStack item = chargedItem(5, 10);

        editor.setCharges(item, -1);
        editor.setTag(item, "arcrot", "not a number");

        assertTrue(events.chargeChanges().isEmpty());
        assertTrue(events.tagChanges().isEmpty());
    }

    @Test
    void removingChargesReportsZeroCharges() {
        ItemStack item = chargedItem(4, 10);

        editor.removeCharges(item);

        ItemChargesChangeEvent event = events.onlyChargeChange();
        assertEquals(4, event.getOldCharges());
        assertEquals(0, event.getNewCharges());
    }

    @Test
    void settingATagFiresATagEventWithTypedValues() {
        ItemStack item = chargedItem(5, 10);

        editor.forCommand(player).setTag(item, "arcrot", "4");
        editor.forCommand(player).setTag(item, "arcrot", "6");

        List<ItemTagChangeEvent> changes = events.tagChanges();
        assertEquals(2, changes.size());
        ItemTagChangeEvent first = changes.getFirst();
        assertSame(player, first.getHolder());
        assertEquals("arcrot", first.getTagId());
        assertEquals(TagType.INT, first.getType());
        assertNull(first.getOldValue());
        assertEquals(4, first.getNewValue());
        assertEquals(4, changes.get(1).getOldValue());
        assertEquals(6, changes.get(1).getNewValue());
    }

    @Test
    void removingATagReportsNoNewValue() {
        ItemStack item = chargedItem(5, 10);
        editor.setTag(item, "arcrot", "4");
        events.tagChanges().clear();

        editor.removeTag(item, "arcrot");

        ItemTagChangeEvent event = events.onlyTagChange();
        assertEquals(4, event.getOldValue());
        assertNull(event.getNewValue());
    }

    @Test
    void settingTheChargesTagFiresOnlyAChargesEvent() {
        ItemStack item = chargedItem(5, 10);

        editor.setTag(item, "charges", "2");

        assertEquals(2, events.onlyChargeChange().getNewCharges());
        assertTrue(events.tagChanges().isEmpty());
    }

    @Test
    void addingAPrintablePropertyReportsTheValueTagsItSets() {
        ItemStack item = chargedItem(5, 10);
        editor.removeCharges(item);
        events.chargeChanges().clear();
        events.tagChanges().clear();

        editor.addProperty(item, "charges");

        ItemTagChangeEvent event = events.onlyTagChange();
        assertEquals("max_charges", event.getTagId());
        assertNull(event.getOldValue());
        assertEquals(0, event.getNewValue());
        assertTrue(events.chargeChanges().isEmpty(), "charges went from none to 0, which counts as no change");
    }

    @Test
    void anEditorWithoutAHolderFiresEventsWithoutOne() {
        editor.setCharges(chargedItem(5, 10), 1);

        assertNull(events.onlyChargeChange().getHolder());
    }

    @Test
    void consumingTheLastChargeFiresAUsedUpEvent() {
        ItemStack item = chargedItem(1, 3);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.ON_CONSUMPTION, PersistentDataType.STRING, "charge:1:stop");
        item.setItemMeta(meta);

        List<ItemStack> returned = ConsumptionHandler.consume(player, item);

        assertTrue(returned.isEmpty());
        ItemChargesChangeEvent event = events.onlyChargeChange();
        assertSame(player, event.getHolder());
        assertEquals(ItemChangeCause.CONSUMED, event.getCause());
        assertEquals(1, event.getOldCharges());
        assertEquals(0, event.getNewCharges());
        assertTrue(event.isUsedUp());
    }

    @Test
    void consumingAChargeThatLeavesSomeIsNotUsedUp() {
        ItemStack item = chargedItem(3, 3);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.ON_CONSUMPTION, PersistentDataType.STRING, "charge:1:stop");
        item.setItemMeta(meta);

        ConsumptionHandler.consume(player, item);

        ItemChargesChangeEvent event = events.onlyChargeChange();
        assertEquals(2, event.getNewCharges());
        assertFalse(event.isUsedUp());
    }
}
