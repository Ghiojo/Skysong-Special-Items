package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The typed tag reads other plugins use through {@code tags()}, with one tag of each type configured. */
class ItemTagReaderTest extends MockBukkitTest {
    private SkysongItems plugin;
    private ItemTags tags;

    @BeforeEach
    void enablePluginWithATagOfEachType() {
        plugin = MockBukkit.load(SkysongItems.class);
        FileConfiguration config = plugin.getConfig();
        configureTag(config, "weight", "float");
        configureTag(config, "cursed", "bool");
        configureTag(config, "origin", "string");
        plugin.getTagRegistry().load(config, plugin.getLogger());
        tags = SkysongItemsApi.get().tags();
    }

    private static void configureTag(FileConfiguration config, String id, String type) {
        config.set("tags." + id + ".namespace", id);
        config.set("tags." + id + ".type", type);
    }

    private static ItemStack tagged() {
        return TaggedItems.stones(1)
                .with(ItemKeys.named("weight"), PersistentDataType.FLOAT, 1.5f)
                .with(ItemKeys.named("cursed"), PersistentDataType.BOOLEAN, true)
                .with(ItemKeys.named("origin"), PersistentDataType.STRING, "ruins")
                .build();
    }

    @Test
    void eachTypeIsReadWithItsOwnMethod() {
        ItemStack item = tagged();

        assertEquals(Optional.of(1.5f), tags.getFloat(item, "weight"));
        assertEquals(Optional.of(true), tags.getBool(item, "cursed"));
        assertEquals(Optional.of("ruins"), tags.getString(item, "Origin"));
    }

    @Test
    void readingATagAsAnotherTypeIsEmpty() {
        ItemStack item = tagged();

        assertEquals(Optional.empty(), tags.getString(item, "weight"));
        assertEquals(Optional.empty(), tags.getFloat(item, "origin"));
        assertTrue(tags.getInt(item, "cursed").isEmpty());
    }

    @Test
    void aValueStoredWithAnotherTypeThanConfiguredIsEmpty() {
        ItemStack item = TaggedItems.stones(1).with(ItemKeys.named("weight"), PersistentDataType.STRING, "heavy").build();

        assertEquals(Optional.empty(), tags.getFloat(item, "weight"));
    }

    @Test
    void readsAreEmptyForNoItem() {
        assertEquals(Optional.empty(), tags.getBool(null, "cursed"));
    }

    @Test
    void typesFollowTheConfig() {
        assertEquals(Optional.of(TagType.FLOAT), tags.type("weight"));
        assertEquals(Optional.of(TagType.BOOL), tags.type("cursed"));
        assertEquals(Optional.of(TagType.STRING), tags.type("origin"));
        assertEquals(Optional.of(TagType.INT), tags.type("charges"));
    }

    @Test
    void idsListConfiguredAndBuiltInTagsAndCannotBeChanged() {
        Set<String> ids = tags.ids();

        assertTrue(ids.containsAll(List.of("weight", "cursed", "origin", "arcrot", "charges", "model")));
        assertThrows(UnsupportedOperationException.class, () -> ids.add("extra"));
    }

    @Test
    void existsFollowsAReload() {
        FileConfiguration config = plugin.getConfig();
        config.createSection("tags");
        configureTag(config, "fresh", "int");

        plugin.getTagRegistry().load(config, plugin.getLogger());

        assertTrue(tags.exists("fresh"));
        assertFalse(tags.exists("weight"));
    }

    @Test
    void itemReadsOffTheMainThreadAreRefused() throws InterruptedException {
        ItemStack item = tagged();

        CompletableFuture<Optional<Float>> read = CompletableFuture.supplyAsync(() -> tags.getFloat(item, "weight"));

        ExecutionException error = assertThrows(ExecutionException.class, read::get);
        assertInstanceOf(IllegalStateException.class, error.getCause());
    }

    @Test
    void registryChecksWorkOffTheMainThread() throws ExecutionException, InterruptedException {
        assertTrue(CompletableFuture.supplyAsync(() -> tags.exists("weight")).get());
    }
}
