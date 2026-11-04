package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The reads SkysongStats needs, called the way it will call them: through
 * {@link SkysongItemsApi#get()} with the tags in the bundled config.yml (arcrot,
 * charged_arcrot and fictis, all int).
 */
class SkysongStatsReadsTest extends MockBukkitTest {
    private SkysongItemsApi api;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        api = SkysongItemsApi.get();
    }

    private static ItemStack arcrot(int value, int amount) {
        return TaggedItems.stones(amount).with(ItemKeys.named("arcrot"), PersistentDataType.INTEGER, value).build();
    }

    private static ItemStack chargedArcrot(int value, int charges) {
        return TaggedItems.stones(1)
                .with(ItemKeys.named("charged_arcrot"), PersistentDataType.INTEGER, value)
                .with(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges)
                .build();
    }

    @Test
    void getIntTagReadsTheValueOnOneItem() {
        assertEquals(OptionalInt.of(3), api.getIntTag(arcrot(3, 1), "arcrot"));
    }

    @Test
    void getIntTagMatchesTheIdIgnoringCase() {
        assertEquals(OptionalInt.of(3), api.getIntTag(arcrot(3, 1), "ARCROT"));
    }

    @Test
    void getIntTagIsEmptyWhenTheItemLacksTheTag() {
        assertEquals(OptionalInt.empty(), api.getIntTag(arcrot(3, 1), "fictis"));
        assertEquals(OptionalInt.empty(), api.getIntTag(new ItemStack(Material.STONE), "arcrot"));
    }

    @Test
    void getIntTagIsEmptyForNoItemOrAir() {
        assertEquals(OptionalInt.empty(), api.getIntTag(null, "arcrot"));
        assertEquals(OptionalInt.empty(), api.getIntTag(new ItemStack(Material.AIR), "arcrot"));
    }

    @Test
    void getIntTagIsEmptyForAnUnknownTag() {
        ItemStack item = TaggedItems.stones(1).with(ItemKeys.named("unknown"), PersistentDataType.INTEGER, 5).build();

        assertEquals(OptionalInt.empty(), api.getIntTag(item, "unknown"));
    }

    @Test
    void getChargesReadsTheChargesLeft() {
        assertEquals(4, api.getCharges(chargedArcrot(1, 4)));
    }

    @Test
    void getChargesIsZeroWithoutCharges() {
        assertEquals(0, api.getCharges(arcrot(1, 1)));
        assertEquals(0, api.getCharges(null));
    }

    @Test
    void hasChargesNeedsMoreThanZero() {
        assertTrue(api.hasCharges(chargedArcrot(1, 1)));
        assertFalse(api.hasCharges(chargedArcrot(1, 0)));
        assertFalse(api.hasCharges(arcrot(1, 1)));
    }

    @Test
    void sumIntTagMultipliesEachValueByItsAmount() {
        Iterable<ItemStack> items = Arrays.asList(arcrot(2, 3), null, new ItemStack(Material.AIR), arcrot(5, 1),
                new ItemStack(Material.DIRT));

        assertEquals(11, api.sumIntTag(items, "arcrot", false));
    }

    @Test
    void sumIntTagCanCountOnlyItemsWithChargesLeft() {
        Iterable<ItemStack> items = Arrays.asList(chargedArcrot(2, 1), chargedArcrot(7, 0), chargedArcrot(3, 5));

        assertEquals(5, api.sumIntTag(items, "charged_arcrot", true));
        assertEquals(12, api.sumIntTag(items, "charged_arcrot", false));
    }

    @Test
    void sumIntTagIsZeroForAnUnknownTag() {
        assertEquals(0, api.sumIntTag(Arrays.asList(arcrot(2, 3)), "missing", false));
    }

    @Test
    void sumIntTagCountsAPlayersInventory() {
        var player = server.addPlayer();
        player.getInventory().addItem(arcrot(2, 3));
        player.getInventory().addItem(new ItemStack(Material.DIRT));

        assertEquals(6, api.sumIntTag(player.getInventory(), "arcrot", false));
    }

    @Test
    void isTagKnowsConfiguredAndBuiltInTags() {
        assertTrue(api.isTag("fictis"));
        assertTrue(api.isTag("charges"));
        assertFalse(api.isTag("fictsi"));
    }

    @Test
    void getTagTypeGivesTheConfiguredType() {
        assertEquals(Optional.of(TagType.INT), api.getTagType("arcrot"));
        assertEquals(Optional.of(TagType.STRING), api.getTagType("version"));
        assertEquals(Optional.of(TagType.BOOL), api.getTagType("can_add_charges"));
        assertEquals(Optional.empty(), api.getTagType("missing"));
    }
}
