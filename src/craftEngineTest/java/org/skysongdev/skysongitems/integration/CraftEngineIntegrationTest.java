package org.skysongdev.skysongitems.integration;

import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.ItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Runs the integration against a mocked CraftEngine item manager, checking which
 * of CraftEngine's item calls each method makes and that the stack CraftEngine
 * edited is the one handed back.
 */
class CraftEngineIntegrationTest {
    private static final String TABLE_ID = "skysong:oak_table";

    private final BukkitItemManager items = mock(BukkitItemManager.class);
    private final BukkitItem wrapped = mock(BukkitItem.class);
    private final ItemStack held = mock(ItemStack.class);
    private final ItemStack edited = mock(ItemStack.class);
    private CraftEngineIntegration integration;

    @BeforeEach
    void mockCraftEngine() {
        when(items.wrap(held)).thenReturn(wrapped);
        when(wrapped.getBukkitItem()).thenReturn(edited);
        integration = new CraftEngineIntegration(items);
    }

    @Test
    void idOfReadsTheCustomId() {
        when(wrapped.customId()).thenReturn(Optional.of(Key.of(TABLE_ID)));

        assertEquals(Optional.of(TABLE_ID), integration.idOf(held));
    }

    @Test
    void idOfIsEmptyForAnItemWithoutOne() {
        when(wrapped.customId()).thenReturn(Optional.empty());

        assertTrue(integration.idOf(held).isEmpty());
    }

    @Test
    void withIdWritesTheIdAndReturnsTheEditedStack() {
        ItemStack result = integration.withId(held, TABLE_ID);

        verify(wrapped).customId(Key.of(TABLE_ID));
        assertSame(edited, result);
    }

    @Test
    void withoutIdRemovesTheTopLevelIdEntry() {
        ItemStack result = integration.withoutId(held);

        verify(wrapped).removeTag("craftengine:id");
        assertSame(edited, result);
    }

    @Test
    void isLoadedAsksCraftEngineForTheItemDefinition() {
        when(items.getItemDefinition(Key.of(TABLE_ID))).thenReturn(Optional.of(mock(ItemDefinition.class)));
        when(items.getItemDefinition(Key.of("skysong:missing"))).thenReturn(Optional.empty());

        assertTrue(integration.isLoaded(TABLE_ID));
        assertFalse(integration.isLoaded("skysong:missing"));
    }

    @Test
    void loadedIdsAreSortedNamespacedStrings() {
        ItemDefinition definition = mock(ItemDefinition.class);
        when(items.loadedItems()).thenReturn(Map.of(
                Key.of(TABLE_ID), definition,
                Key.of("skysong:armchair"), definition));

        assertEquals(List.of("skysong:armchair", TABLE_ID), integration.loadedIds());
    }
}
