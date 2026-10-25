package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@code /si reload} re-reading properties and tags from config.yml and permissions from materials.yml. */
class ReloadCommandTest extends MockBukkitTest {
    private SkysongItems plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    private void runReload() {
        Command command = plugin.getCommand("skysongitems");
        new ReloadCommand().onCommand(player, command, "si", new String[0]);
    }

    /** Writes a config.yml with one extra property and one extra tag next to the bundled ones. */
    private void saveConfigWithAdditions() {
        FileConfiguration config = plugin.getConfig();
        config.set("properties.added.type", "base");
        config.set("properties.added.proptype", "Weapon");
        config.set("properties.added.item", "STONE");
        config.set("properties.added.name", "Added");
        config.set("properties.added.description", "Added on disk");
        config.set("tags.added_tag.namespace", "added_tag");
        config.set("tags.added_tag.type", "int");
        plugin.saveConfig();
    }

    @Test
    void reloadLoadsPropertiesAndTagsSavedToConfig() {
        saveConfigWithAdditions();
        player.addAttachment(plugin, "skysongitems.reload", true);

        runReload();

        assertTrue(plugin.getPropertyRegistry().contains("added"));
        assertTrue(plugin.getTagRegistry().contains("added_tag"));
        assertTrue(plugin.getPropertyRegistry().contains("charges"));
    }

    @Test
    void reloadWithoutPermissionLoadsNothing() {
        saveConfigWithAdditions();

        runReload();

        assertFalse(plugin.getPropertyRegistry().contains("added"));
        assertFalse(plugin.getTagRegistry().contains("added_tag"));
    }

    /** Replaces materials.yml with a single moonstone entry. */
    private void saveMaterialsWithOnlyMoonstone() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("materials.moonstone", "skysongitems.use.moonstone");
        yaml.save(new File(plugin.getDataFolder(), "materials.yml"));
    }

    @Test
    void reloadRereadsMaterialPermissions() throws IOException {
        // Reproduces the registry quirk: materials.yml used to be read only on enable.
        saveMaterialsWithOnlyMoonstone();
        player.addAttachment(plugin, "skysongitems.reload", true);

        runReload();

        assertEquals("skysongitems.use.moonstone", plugin.getMaterialPermissions().getPermission("moonstone"));
        assertNull(plugin.getMaterialPermissions().getPermission("IRON_INGOT"));
    }

    @Test
    void reloadWithoutPermissionKeepsMaterialPermissions() throws IOException {
        saveMaterialsWithOnlyMoonstone();

        runReload();

        assertNull(plugin.getMaterialPermissions().getPermission("moonstone"));
        assertEquals("skysongitems.use.iron", plugin.getMaterialPermissions().getPermission("IRON_INGOT"));
    }
}
