package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemTraits;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The charges and traits reads, with the traits in the bundled config.yml (test, charges, spell). */
class ItemChargeAndTraitReaderTest extends MockBukkitTest {
    private ItemCharges charges;
    private ItemTraits traits;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        charges = SkysongItemsApi.get().charges();
        traits = SkysongItemsApi.get().traits();
    }

    private static ItemStack withTraits(String stored) {
        return TaggedItems.stones(1).with(ItemKeys.TRAITS, PersistentDataType.STRING, stored).build();
    }

    @Test
    void chargesAndMaximumAreRead() {
        ItemStack item = TaggedItems.stones(1)
                .with(ItemKeys.CHARGES, PersistentDataType.INTEGER, 2)
                .with(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, 5)
                .build();

        assertEquals(OptionalInt.of(2), charges.get(item));
        assertEquals(OptionalInt.of(5), charges.getMax(item));
        assertTrue(charges.hasLeft(item));
    }

    @Test
    void zeroChargesLeftIsNotTheSameAsNoCharges() {
        ItemStack spent = TaggedItems.stones(1).with(ItemKeys.CHARGES, PersistentDataType.INTEGER, 0).build();

        assertEquals(OptionalInt.of(0), charges.get(spent));
        assertEquals(OptionalInt.empty(), charges.get(TaggedItems.stones(1).build()));
        assertFalse(charges.hasLeft(spent));
    }

    @Test
    void traitsAreReadInOrderSkippingBlanks() {
        ItemStack item = withTraits("charges;;spell");

        assertEquals(List.of("charges", "spell"), traits.get(item));
        assertTrue(traits.has(item, "spell"));
        assertFalse(traits.has(item, "spel"));
    }

    @Test
    void traitsOfAnItemNotYetUpdatedAreRead() {
        ItemStack item = TaggedItems.stones(1).with(ItemKeys.LEGACY_TRAITS, PersistentDataType.STRING, "spell").build();

        assertEquals(List.of("spell"), traits.get(item));
    }

    @Test
    void traitsOfAnItemWithoutAnyAreEmptyAndCannotBeChanged() {
        List<String> none = traits.get(TaggedItems.stones(1).build());

        assertTrue(none.isEmpty());
        assertTrue(traits.get(null).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> none.add("spell"));
    }

    @Test
    void configuredTraitsExist() {
        assertTrue(traits.exists("Spell"));
        assertFalse(traits.exists("missing"));
        assertEquals(List.of("test", "charges", "spell"), List.copyOf(traits.ids()));
    }
}
