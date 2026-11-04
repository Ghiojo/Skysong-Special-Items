package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemAccess;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Writes through {@code detached()}, for items nobody holds: they store the value and
 * nothing else. Uses the bundled config.yml, where "arcrot" is an int tag and "charges"
 * a printable property over the charges and max_charges value tags.
 */
class DetachedItemEditsTest extends MockBukkitTest {
    private SkysongItemsApi api;
    private ItemAccess detached;
    private RecordedItemEvents events;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        api = SkysongItemsApi.get();
        detached = api.detached();
        events = RecordedItemEvents.listenOn(server);
    }

    /** An item as SkysongItems generates them, with a physical description to draw lore from. */
    private static ItemStack skysongItem() {
        return TaggedItems.stones(1)
                .with(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION)
                .with(ItemKeys.NAME, PersistentDataType.STRING, "Sword")
                .with(ItemKeys.LORE, PersistentDataType.STRING, "physical")
                .build();
    }

    private static Integer storedInt(ItemStack item, NamespacedKey key) {
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    @Test
    void theDetachedViewIsTheSameOneEachTime() {
        assertSame(detached, api.detached());
    }

    @Test
    void aTagIsWrittenToAnItemThatIsNotASkysongItem() {
        ItemStack plain = new ItemStack(Material.STONE);

        detached.tags().set(plain, "arcrot", 7);

        assertEquals(7, api.getIntTag(plain, "arcrot").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> api.tags().set(new ItemStack(Material.STONE), "arcrot", 7));
    }

    @Test
    void aDetachedWriteFiresNoEvent() {
        ItemStack item = skysongItem();

        detached.tags().set(item, "arcrot", 7);
        detached.charges().set(item, 3);

        assertTrue(events.tagChanges().isEmpty(), "a detached write fired " + events.tagChanges().size() + " tag events");
        assertTrue(events.chargeChanges().isEmpty(), "a detached write fired a charges event");
    }

    @Test
    void aHeldWriteStillFiresItsEvent() {
        ItemStack item = skysongItem();

        api.tags().set(item, "arcrot", 7);

        assertEquals("arcrot", events.onlyTagChange().tagId());
    }

    @Test
    void addingAPropertyLeavesItsValueTagsAlone() {
        ItemStack item = skysongItem();

        assertTrue(detached.properties().add(item, "charges"));

        assertEquals(List.of("charges"), detached.properties().get(item));
        assertNull(storedInt(item, ItemKeys.CHARGES));
        assertNull(storedInt(item, ItemKeys.MAX_CHARGES));
    }

    @Test
    void addingAPropertyHeldStillSetsItsValueTags() {
        ItemStack item = skysongItem();

        assertTrue(api.properties().add(item, "charges"));

        assertEquals(Integer.valueOf(0), storedInt(item, ItemKeys.CHARGES));
    }

    @Test
    void theLoreIsNotRedrawn() {
        ItemStack detachedItem = skysongItem();
        ItemStack heldItem = skysongItem();

        detached.charges().set(detachedItem, 3);
        api.charges().set(heldItem, 3);

        assertNull(detachedItem.getItemMeta().lore());
        assertFalse(heldItem.getItemMeta().lore().isEmpty());
    }

    @Test
    void whatSkysongItemsDoesNotHaveIsStillRefused() {
        ItemStack item = skysongItem();

        assertThrows(IllegalArgumentException.class, () -> detached.tags().set(item, "not_a_tag", 7));
        assertThrows(IllegalArgumentException.class, () -> detached.tags().remove(item, "not_a_tag"));
        assertThrows(IllegalArgumentException.class, () -> detached.properties().add(item, "not_a_property"));
    }

    @Test
    void thereIsStillNothingToWriteToWithoutAnItem() {
        assertThrows(IllegalArgumentException.class, () -> detached.tags().set(null, "arcrot", 7));
        assertThrows(IllegalArgumentException.class, () -> detached.tags().set(new ItemStack(Material.AIR), "arcrot", 7));
        assertThrows(IllegalArgumentException.class, () -> detached.charges().set(null, 3));
        assertThrows(IllegalArgumentException.class, () -> detached.properties().add(null, "charges"));
    }
}
