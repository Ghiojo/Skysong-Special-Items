package org.skysongdev.skysongitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.BreweryPrintableProperty;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TheBrewingProject and LuckPerms are soft dependencies, and neither is on the
 * test classpath, so these tests run the plugin exactly as a server without
 * them would.
 */
class WithoutSoftDependenciesTest extends MockBukkitTest {
    private SkysongItems plugin;

    @BeforeEach
    void loadPlugin() {
        plugin = MockBukkit.load(SkysongItems.class);
    }

    @Test
    void pluginEnablesWithBothIntegrationsSkipped() {
        assertTrue(plugin.isEnabled());
        assertTrue(plugin.getBrewingProject().isEmpty());
        assertTrue(plugin.getLuckPerms().isEmpty());
    }

    @Test
    void permissionItemsGrantNothingWithoutLuckPerms() {
        PlayerMock player = server.addPlayer();

        assertDoesNotThrow(() -> Utils.updatePlayerPermissions(player, true, "skysongitems.test"));
        assertFalse(player.hasPermission("skysongitems.test"));
    }

    @Test
    void breweryPropertiesAreLeftOutOfTheLoreWithoutTheBrewingProject() {
        BreweryPrintableProperty brewQuality = new BreweryPrintableProperty(
                "brew_quality", "quality", "POTION", "Quality", "Brew quality", "Quality: ;!", new ArrayList<>(), 1);
        Constants.properties.add(brewQuality);
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        data.setLoreSections(List.of("physical", "functional", "skill"));
        data.setPropertiesText("brew_quality");
        item.setItemMeta(meta);

        ItemStack updated = assertDoesNotThrow(() -> Utils.updateItemLore(item));

        List<Component> lore = updated.getItemMeta().lore();
        assertTrue(lore.stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .noneMatch(line -> line.contains("Quality")));
    }
}
