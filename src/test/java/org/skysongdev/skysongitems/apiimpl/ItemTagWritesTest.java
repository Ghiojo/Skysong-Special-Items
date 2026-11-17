package org.skysongdev.skysongitems.apiimpl;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.ItemTemplates;
import org.skysongdev.skysongitems.api.ItemTimer;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.api.event.ItemTagChangeEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tag writes through {@code tags()} and turning plain items into SkysongItems items, with
 * the "arcrot" int tag from the bundled config.yml and one tag of each other type.
 */
class ItemTagWritesTest extends MockBukkitTest {
    private ItemTags tags;
    private ItemTemplates templates;
    private RecordedItemEvents events;

    @BeforeEach
    void enablePlugin() {
        SkysongItems plugin = MockBukkit.load(SkysongItems.class);
        FileConfiguration config = plugin.getConfig();
        configureTag(config, "weight", "float");
        configureTag(config, "cursed", "bool");
        configureTag(config, "origin", "string");
        plugin.getTagRegistry().load(config, plugin.getLogger());
        tags = SkysongItemsApi.get().tags();
        templates = SkysongItemsApi.get().templates();
        events = RecordedItemEvents.listenOn(server);
    }

    private static void configureTag(FileConfiguration config, String id, String type) {
        config.set("tags." + id + ".namespace", id);
        config.set("tags." + id + ".type", type);
    }

    private static ItemStack skysongItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        item.setItemMeta(meta);
        return item;
    }

    @Test
    void setWritesEachTypeAndReadsBack() {
        ItemStack item = skysongItem();

        tags.set(item, "arcrot", 3);
        tags.set(item, "weight", 1.5);
        tags.set(item, "cursed", true);
        tags.set(item, "origin", "ruins");

        assertEquals(3, tags.getInt(item, "arcrot").orElseThrow());
        assertEquals(1.5f, tags.getFloat(item, "weight").orElseThrow());
        assertTrue(tags.getBool(item, "cursed").orElseThrow());
        assertEquals("ruins", tags.getString(item, "origin").orElseThrow());
    }

    @Test
    void setFiresATagEventWithTheHolderAndApiCause() {
        PlayerMock player = server.addPlayer();
        ItemStack item = skysongItem();

        tags.set(player, item, "ARCROT", 2);

        ItemTagChangeEvent event = events.onlyTagChange();
        assertSame(player, event.getHolder());
        assertEquals(ItemChangeCause.API, event.getCause());
        assertEquals("arcrot", event.getTagId());
        assertEquals(2, event.getNewValue());
    }

    @Test
    void setRefusesAValueOfTheWrongType() {
        ItemStack item = skysongItem();

        assertThrows(IllegalArgumentException.class, () -> tags.set(item, "arcrot", "three"));
        assertThrows(IllegalArgumentException.class, () -> tags.set(item, "arcrot", 3L));
        assertThrows(IllegalArgumentException.class, () -> tags.set(item, "cursed", "true"));
        assertThrows(IllegalArgumentException.class, () -> tags.set(item, "origin", null));
        assertFalse(tags.getInt(item, "arcrot").isPresent());
    }

    @Test
    void setForRemovesTheTagLaterAndAPlainSetKeepsItForGood() {
        ItemStack item = skysongItem();

        tags.setFor(item, "arcrot", 3, "24h");
        assertEquals(3, tags.getInt(item, "arcrot").orElseThrow());
        List<ItemTimer> timers = SkysongItemsApi.get().timers().list(item);
        assertEquals(1, timers.size());
        assertEquals("remove_tag", timers.get(0).action());
        assertEquals("arcrot", timers.get(0).target());

        tags.set(item, "arcrot", 4);
        assertTrue(SkysongItemsApi.get().timers().list(item).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> tags.setFor(item, "arcrot", 3, "1x"));
    }

    @Test
    void addCountsAMissingTagAsZeroAndNeverGoesBelowZero() {
        ItemStack item = skysongItem();

        tags.add(item, "arcrot", 2);
        tags.add(item, "weight", 0.5);
        assertEquals(2, tags.getInt(item, "arcrot").orElseThrow());
        assertEquals(0.5f, tags.getFloat(item, "weight").orElseThrow());

        tags.add(item, "arcrot", -5);
        tags.add(item, "weight", -1);
        assertEquals(0, tags.getInt(item, "arcrot").orElseThrow());
        assertEquals(0f, tags.getFloat(item, "weight").orElseThrow());
    }

    @Test
    void addRefusesTagsThatDoNotHoldNumbersAndAmountsOfTheWrongKind() {
        ItemStack item = skysongItem();

        assertThrows(IllegalArgumentException.class, () -> tags.add(item, "cursed", 1));
        assertThrows(IllegalArgumentException.class, () -> tags.add(item, "origin", 1));
        assertThrows(IllegalArgumentException.class, () -> tags.add(item, "arcrot", 1.5));
        assertFalse(tags.getInt(item, "arcrot").isPresent());
    }

    @Test
    void setRefusesAnUnknownTag() {
        assertThrows(IllegalArgumentException.class, () -> tags.set(skysongItem(), "nope", 1));
    }

    @Test
    void thePermissionTagCanBeWrittenAndRemoved() {
        ItemStack item = skysongItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.PERMISSION, PersistentDataType.STRING, "some.node");
        item.setItemMeta(meta);

        tags.set(item, "permission", "skysong.crafted.sword");

        assertEquals("skysong.crafted.sword", tags.getString(item, "permission").orElseThrow());
        assertTrue(tags.remove(item, "permission"));
        assertEquals(Optional.empty(), tags.getString(item, "permission"));
    }

    @Test
    void theStoredNameAndDescriptionCanBeWritten() {
        ItemStack item = skysongItem();

        tags.set(item, "name", "&6Renamed");
        tags.set(item, "lore", "physical");

        assertEquals("&6Renamed", tags.getString(item, "name").orElseThrow());
        assertEquals("physical", tags.getString(item, "lore").orElseThrow());
    }

    @Test
    void theVersionTagCanNotBeWritten() {
        ItemStack item = skysongItem();

        assertThrows(IllegalArgumentException.class, () -> tags.set(item, "version", "0"));
        assertThrows(IllegalArgumentException.class, () -> tags.remove(item, "version"));
        assertEquals(Constants.ITEM_VERSION, tags.getString(item, "version").orElseThrow());
    }

    @Test
    void removeReportsWhetherTheItemHadTheTag() {
        ItemStack item = skysongItem();
        tags.set(item, "arcrot", 3);

        assertTrue(tags.remove(item, "arcrot"));
        assertFalse(tags.remove(item, "arcrot"));
        assertFalse(tags.getInt(item, "arcrot").isPresent());
    }

    @Test
    void writesRefuseAPlainItemUntilItIsConverted() {
        ItemStack item = new ItemStack(Material.STONE);

        assertThrows(IllegalArgumentException.class, () -> tags.set(item, "arcrot", 1));

        assertSame(item, templates.toSkysongItem(item));
        tags.set(item, "arcrot", 1);
        assertEquals(1, tags.getInt(item, "arcrot").orElseThrow());
    }

    @Test
    void writesRefuseNullAndAir() {
        assertThrows(IllegalArgumentException.class, () -> tags.set(null, "arcrot", 1));
        assertThrows(IllegalArgumentException.class, () -> tags.set(new ItemStack(Material.AIR), "arcrot", 1));
        assertThrows(IllegalArgumentException.class, () -> templates.toSkysongItem(null));
    }

    @Test
    void toSkysongItemStoresAPlainItemsNameAndMarksItCurrent() {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Old Rock"));
        item.setItemMeta(meta);

        templates.toSkysongItem(item);

        assertTrue(templates.isSkysongItem(item));
        assertEquals(Constants.ITEM_VERSION, tags.getString(item, "version").orElseThrow());
        assertTrue(tags.getString(item, "name").orElseThrow().contains("Old Rock"));
    }

    @Test
    void toSkysongItemUpgradesAnItemFromAnOlderVersion() {
        ItemStack item = skysongItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, "1.0");
        item.setItemMeta(meta);

        templates.toSkysongItem(item);

        assertEquals(Constants.ITEM_VERSION, tags.getString(item, "version").orElseThrow());
    }

    @Test
    void toSkysongItemLeavesACurrentItemAlone() {
        ItemStack item = skysongItem();
        ItemStack before = item.clone();

        templates.toSkysongItem(item);

        assertEquals(before, item);
    }

    @Test
    void writesOffTheMainThreadAreRefused() {
        ItemStack item = skysongItem();

        CompletableFuture<Void> write = CompletableFuture.runAsync(() -> tags.set(item, "arcrot", 1));

        ExecutionException error = assertThrows(ExecutionException.class, write::get);
        assertInstanceOf(IllegalStateException.class, error.getCause());
    }
}
