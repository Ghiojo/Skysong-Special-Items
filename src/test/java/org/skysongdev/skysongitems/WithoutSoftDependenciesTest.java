package org.skysongdev.skysongitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;
import org.skysongdev.skysongitems.utils.PermissionItems;

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

        assertDoesNotThrow(() -> PermissionItems.grant(player, "skysongitems.test"));
        assertFalse(player.hasPermission("skysongitems.test"));
    }

    @Test
    void permissionItemsRevokeNothingWithoutLuckPerms() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "skysongitems.test", true);

        assertDoesNotThrow(() -> PermissionItems.revoke(player, "skysongitems.test"));
        assertTrue(player.hasPermission("skysongitems.test"));
    }

    @Test
    void joiningWithAPermissionItemGrantsNothingWithoutLuckPerms() {
        PlayerMock player = new PlayerMock(server, "Holder");
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.PERMISSION, PersistentDataType.STRING, "skysongitems.test");
        item.setItemMeta(meta);
        player.getInventory().addItem(item);

        assertDoesNotThrow(() -> server.addPlayer(player));
        assertFalse(player.hasPermission("skysongitems.test"));
    }

    @Test
    void breweryPropertiesAreLeftOutOfTheLoreWithoutTheBrewingProject() {
        FileConfiguration config = plugin.getConfig();
        config.set("properties.brew_quality.type", "breweryprintable");
        config.set("properties.brew_quality.proptype", "quality");
        config.set("properties.brew_quality.item", "POTION");
        config.set("properties.brew_quality.name", "Quality");
        config.set("properties.brew_quality.description", "Brew quality");
        config.set("properties.brew_quality.printable", "Quality: ;!");
        config.set("properties.brew_quality.weight", 1);
        plugin.getPropertyRegistry().load(config, plugin.getLogger());
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        data.setLoreSections(List.of("physical", "functional", "skill"));
        data.setPropertiesText("brew_quality");
        item.setItemMeta(meta);

        ItemStack updated = assertDoesNotThrow(() -> ItemLoreRenderer.refresh(item));

        List<Component> lore = updated.getItemMeta().lore();
        assertTrue(lore.stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .noneMatch(line -> line.contains("Quality")));
    }
}
