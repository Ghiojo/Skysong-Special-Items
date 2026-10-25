package org.skysongdev.skysongitems.utils;

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

/** Tests for the material permissions read from materials.yml and the rule that applies them. */
class MaterialPermissionsTest extends MockBukkitTest {
    private SkysongItems plugin;

    @BeforeEach
    void loadPlugin() {
        plugin = MockBukkit.load(SkysongItems.class);
    }

    private File materialsFile() {
        return new File(plugin.getDataFolder(), "materials.yml");
    }

    private void reloadMaterialPermissions() {
        plugin.getMaterialPermissions().load(plugin.getDataFolder(), plugin.getLogger());
    }

    private String permissionFor(String materialId) {
        return plugin.getMaterialPermissions().getPermission(materialId);
    }

    @Test
    void enablingCreatesADefaultMaterialsFile() {
        assertTrue(materialsFile().exists());
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(materialsFile());
        assertEquals("skysongitems.use.iron", yaml.getString("materials.IRON_INGOT"));
        assertEquals("skysongitems.use.diamond", yaml.getString("materials.DIAMOND"));
    }

    @Test
    void defaultMaterialsAreLoaded() {
        assertEquals("skysongitems.use.iron", permissionFor("IRON_INGOT"));
        assertEquals("skysongitems.use.diamond", permissionFor("DIAMOND"));
    }

    @Test
    void materialsFromAnExistingFileAreLoaded() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("materials.moonstone", "skysongitems.use.moonstone");
        yaml.save(materialsFile());

        reloadMaterialPermissions();

        assertEquals("skysongitems.use.moonstone", permissionFor("moonstone"));
    }

    @Test
    void materialIdsAreCaseSensitive() {
        assertNull(permissionFor("iron_ingot"));
    }

    @Test
    void unknownMaterialHasNoPermission() {
        assertNull(permissionFor("not_a_material"));
    }

    private PlayerMock playerWith(String... permissions) {
        PlayerMock player = server.addPlayer();
        for (String permission : permissions) {
            player.addAttachment(plugin, permission, true);
        }
        return player;
    }

    @Test
    void playerWithoutTheMaterialPermissionCannotUseTheItem() {
        // Reproduces the inverted cauldron check: regular players used to pass it.
        assertFalse(plugin.getMaterialPermissions().canUse(playerWith(), "IRON_INGOT"));
    }

    @Test
    void playerWithTheMaterialPermissionCanUseTheItem() {
        assertTrue(plugin.getMaterialPermissions().canUse(playerWith("skysongitems.use.iron"), "IRON_INGOT"));
    }

    @Test
    void staffCanUseTheItemWithoutTheMaterialPermission() {
        // Reproduces the inverted cauldron check: staff used to be the only ones blocked.
        PlayerMock staff = playerWith(MaterialPermissions.STAFF_BYPASS_PERMISSION);
        assertTrue(plugin.getMaterialPermissions().canUse(staff, "IRON_INGOT"));
    }

    @Test
    void itemWithoutAConfiguredPermissionCanBeUsedByAnyone() {
        assertTrue(plugin.getMaterialPermissions().canUse(playerWith(), "not_a_material"));
    }

    @Test
    void itemWithoutAMaterialIdCanBeUsedByAnyone() {
        assertTrue(plugin.getMaterialPermissions().canUse(playerWith(), null));
    }
}
