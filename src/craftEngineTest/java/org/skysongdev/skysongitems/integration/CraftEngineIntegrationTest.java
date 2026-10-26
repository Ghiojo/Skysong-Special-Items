package org.skysongdev.skysongitems.integration;

import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Runs the integration against a fake of CraftEngine's item manager, checking
 * the keys and custom data entry it hands CraftEngine and that the stack
 * CraftEngine returns is the one passed back.
 */
class CraftEngineIntegrationTest {
    private static final String TABLE_ID = "skysong:oak_table";

    private final RecordingItemAccess items = new RecordingItemAccess();
    private final CraftEngineIntegration integration = new CraftEngineIntegration(items);
    private final ItemStack held = mock(ItemStack.class);

    @Test
    void idOfGivesTheNamespacedId() {
        items.ids.put(held, Key.of(TABLE_ID));

        assertEquals(Optional.of(TABLE_ID), integration.idOf(held));
    }

    @Test
    void idOfIsEmptyForAnItemWithoutOne() {
        assertTrue(integration.idOf(held).isEmpty());
    }

    @Test
    void withIdWritesTheParsedKeyAndReturnsCraftEnginesStack() {
        ItemStack result = integration.withId(held, TABLE_ID);

        assertEquals(new Key("skysong", "oak_table"), items.ids.get(result));
        assertSame(items.returned, result);
    }

    @Test
    void withoutIdRemovesTheTopLevelIdEntry() {
        ItemStack result = integration.withoutId(held);

        assertEquals("craftengine:id", items.removedTag);
        assertSame(items.returned, result);
    }

    @Test
    void isLoadedAsksForTheParsedKey() {
        items.defined.add(new Key("skysong", "oak_table"));

        assertTrue(integration.isLoaded(TABLE_ID));
        assertFalse(integration.isLoaded("skysong:missing"));
    }

    @Test
    void loadedIdsAreSortedNamespacedStrings() {
        items.defined.add(Key.of(TABLE_ID));
        items.defined.add(Key.of("skysong:armchair"));

        assertEquals(List.of("skysong:armchair", TABLE_ID), integration.loadedIds());
    }

    /** Keeps ids per stack and, like CraftEngine, returns a different stack from each edit. */
    private static final class RecordingItemAccess implements CraftEngineItemAccess {
        private final Map<ItemStack, Key> ids = new IdentityHashMap<>();
        private final Set<Key> defined = new HashSet<>();
        private final ItemStack returned = mock(ItemStack.class);
        private String removedTag;

        @Override
        public Optional<Key> customId(ItemStack item) {
            return Optional.ofNullable(ids.get(item));
        }

        @Override
        public ItemStack withCustomId(ItemStack item, Key id) {
            ids.put(returned, id);
            return returned;
        }

        @Override
        public ItemStack withoutTag(ItemStack item, String tag) {
            removedTag = tag;
            return returned;
        }

        @Override
        public boolean isDefined(Key id) {
            return defined.contains(id);
        }

        @Override
        public Set<Key> definedIds() {
            return defined;
        }
    }
}
