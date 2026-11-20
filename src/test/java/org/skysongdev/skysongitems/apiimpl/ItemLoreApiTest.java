package org.skysongdev.skysongitems.apiimpl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ExtraLorePosition;
import org.skysongdev.skysongitems.api.LoreSection;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Other plugins' lore lines through {@code lore()}. The bundled config.yml defines the
 * printable charges trait, drawn as "[Charges: $1/$2]".
 */
class ItemLoreApiTest extends MockBukkitTest {
    private static final List<Component> NPC_LINES = List.of(Component.text("Holds Mira"), Component.text("Owner: Alex"));

    private SkysongItemsApi api;
    private Plugin npcs;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        api = SkysongItemsApi.get();
        npcs = MockBukkit.createMockPlugin("SkysongNPCs");
    }

    private static ItemStack skysongItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> plainLore(ItemStack item) {
        return item.getItemMeta().lore().stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
    }

    @Test
    void linesAreStoredReadBackAndDrawnWhereTheirPluginPutThem() {
        ItemStack item = skysongItem();
        ExtraLorePosition belowPhysical = ExtraLorePosition.below(LoreSection.PHYSICAL);

        api.lore().setExtra(npcs, item, belowPhysical, NPC_LINES);

        assertEquals(List.of("physical", "", "Holds Mira", "Owner: Alex", "", "functional", "", "skill"), plainLore(item));
        assertEquals(NPC_LINES, api.lore().getExtra(npcs, item));
        assertEquals(Optional.of(belowPhysical), api.lore().getExtraPosition(npcs, item));
        assertEquals(List.of("skysongnpcs"), api.lore().extraOwners(item));
    }

    @Test
    void linesAfterTheTraitsSurviveEveryTraitRedraw() {
        ItemStack item = skysongItem();
        api.charges().setMax(item, 5);
        api.lore().setExtra(npcs, item, ExtraLorePosition.afterTraits(), NPC_LINES);

        api.charges().set(item, 2);

        List<String> lore = plainLore(item);
        assertEquals(List.of("[Charges: 2/5]", "", "Holds Mira", "Owner: Alex"), lore.subList(lore.size() - 4, lore.size()));
    }

    @Test
    void pluginsAtOnePositionAreDrawnAlphabeticallyAndEachOnlyRemovesItsOwn() {
        Plugin zeta = MockBukkit.createMockPlugin("Zeta");
        Plugin alpha = MockBukkit.createMockPlugin("Alpha");
        ItemStack item = skysongItem();
        ExtraLorePosition aboveSkill = ExtraLorePosition.above(LoreSection.SKILL);
        api.lore().setExtra(zeta, item, aboveSkill, List.of(Component.text("Zeta line")));
        api.lore().setExtra(alpha, item, aboveSkill, List.of(Component.text("Alpha line")));

        assertEquals(List.of("physical", "", "functional", "", "Alpha line", "", "Zeta line", "", "skill"), plainLore(item));
        assertEquals(List.of("alpha", "zeta"), api.lore().extraOwners(item));

        assertTrue(api.lore().removeExtra(zeta, item));
        assertFalse(api.lore().removeExtra(zeta, item));
        assertEquals(List.of("physical", "", "functional", "", "Alpha line", "", "skill"), plainLore(item));
    }

    @Test
    void linesNextToAHiddenSectionStillShow() {
        ItemStack item = skysongItem();
        ItemMeta meta = item.getItemMeta();
        ItemData.from(meta).setDeletedLines("100");
        item.setItemMeta(meta);

        api.lore().setExtra(npcs, item, ExtraLorePosition.above(LoreSection.PHYSICAL), NPC_LINES);

        assertEquals(List.of("Holds Mira", "Owner: Alex", "", "functional", "", "skill"), plainLore(item));
    }

    @Test
    void anEmptyListRemovesTheLinesAndMoreThanTenAreRefused() {
        ItemStack item = skysongItem();
        api.lore().setExtra(npcs, item, ExtraLorePosition.afterTraits(), NPC_LINES);
        List<Component> eleven = Collections.nCopies(11, Component.text("line"));

        assertThrows(IllegalArgumentException.class,
                () -> api.lore().setExtra(npcs, item, ExtraLorePosition.afterTraits(), eleven));
        api.lore().setExtra(npcs, item, ExtraLorePosition.afterTraits(), List.of());

        assertEquals(List.of(), api.lore().getExtra(npcs, item));
        assertFalse(item.getPersistentDataContainer().has(ItemKeys.EXTRA_LORE), "no empty container is left behind");
        assertEquals(List.of("physical", "", "functional", "", "skill"), plainLore(item),
                "the lines leave the lore of an item without traits too");
    }

    @Test
    void onlyDetachedModeTakesPlainItemsAndItDoesNotRedraw() {
        ItemStack plain = new ItemStack(Material.STONE);

        assertThrows(IllegalArgumentException.class,
                () -> api.lore().setExtra(npcs, plain, ExtraLorePosition.afterTraits(), NPC_LINES));
        api.detached().lore().setExtra(npcs, plain, ExtraLorePosition.afterTraits(), NPC_LINES);

        assertEquals(NPC_LINES, api.detached().lore().getExtra(npcs, plain));
        assertNull(plain.getItemMeta().lore());
        assertEquals(List.of(), api.lore().getExtra(npcs, null));
    }
}
