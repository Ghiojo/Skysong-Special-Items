package org.skysongdev.skysongitems.timer;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemData;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests when the plugin applies items' due timers: only when players deal with the items.
 * The bundled config.yml defines the trait test.
 */
class ItemTimerTriggersTest extends MockBukkitTest {
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    /** An item with the trait test and a timer that removed it a minute ago. */
    private static ItemStack expiredItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        ItemData data = new ItemData(meta.getPersistentDataContainer());
        data.setLoreSections(List.of("physical", "functional", "skill"));
        data.addTrait("test");
        item.setItemMeta(meta);
        ItemTimerStore.write(item, List.of(new ItemTimer("real", Instant.now().minusSeconds(60).toEpochMilli(),
                "remove_trait", "test", null, null, null)));
        return item;
    }

    private static boolean hasTrait(ItemStack item) {
        return ItemData.from(item.getItemMeta()).hasTrait("test");
    }

    @Test
    void openingAContainerAppliesItsTimersAndThePlayers() {
        Inventory chest = server.createInventory(null, InventoryType.CHEST);
        chest.setItem(0, expiredItem());
        player.getInventory().setItem(5, expiredItem());

        player.openInventory(chest);

        assertFalse(hasTrait(chest.getItem(0)));
        assertFalse(hasTrait(player.getInventory().getItem(5)));
    }

    @Test
    void clickingInAnInventoryAppliesTheTimersOnTheNextTick() {
        Inventory chest = server.createInventory(null, InventoryType.CHEST);
        player.openInventory(chest);
        // Put in after opening, so only the click can apply them.
        chest.setItem(0, expiredItem());
        player.getInventory().setItem(5, expiredItem());

        server.getPluginManager().callEvent(new InventoryClickEvent(player.getOpenInventory(),
                InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.PICKUP_ALL));
        assertTrue(hasTrait(chest.getItem(0)));
        server.getScheduler().performOneTick();

        assertFalse(hasTrait(chest.getItem(0)));
        assertFalse(hasTrait(player.getInventory().getItem(5)));
    }

    @Test
    void usingAnItemAppliesItsTimers() {
        player.getInventory().setItemInMainHand(expiredItem());

        server.getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR,
                player.getInventory().getItemInMainHand(), null, BlockFace.SELF, EquipmentSlot.HAND));

        assertFalse(hasTrait(player.getInventory().getItemInMainHand()));
    }

    @Test
    void switchingToAnItemAppliesItsTimers() {
        player.getInventory().setItem(3, expiredItem());

        server.getPluginManager().callEvent(new PlayerItemHeldEvent(player, 0, 3));

        assertFalse(hasTrait(player.getInventory().getItem(3)));
    }

    @Test
    void itemsNobodyDealsWithKeepTheirDueTimers() {
        player.getInventory().setItem(5, expiredItem());

        server.getScheduler().performTicks(100);

        assertTrue(hasTrait(player.getInventory().getItem(5)));
    }
}
