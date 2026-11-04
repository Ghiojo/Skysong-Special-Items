package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemTemplate;
import org.skysongdev.skysongitems.api.ItemTemplates;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Reading a template file into a reusable {@code ItemTemplate}. Creating the item itself
 * sets Paper data components, which MockBukkit cannot do (see ItemFactoryTest), so the
 * created item needs the server smoke test.
 */
class ItemTemplateFromFileTest extends MockBukkitTest {
    private ItemTemplates templates;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        templates = SkysongItemsApi.get().templates();
    }

    private static Path templateFile(Path folder, String type) throws IOException {
        return Files.writeString(folder.resolve("sword.yml"),
                "name: '&6Ancient Sword'\ntype: " + type + "\nlore:\n  - physical\n");
    }

    @Test
    void theTemplateIsReadOnceAndNotAgainWhenTheFileChanges(@TempDir Path folder) throws IOException {
        Path file = templateFile(folder, "GRASS_BLOCK");

        ItemTemplate template = templates.load(file);
        templateFile(folder, "DIRT");

        assertEquals(Material.GRASS_BLOCK, template.material());
        assertEquals(Material.DIRT, templates.load(file).material());
    }

    @Test
    void aTypeThatMatchesNoMaterialBecomesABarrier(@TempDir Path folder) throws IOException {
        Path file = templateFile(folder, "NOT_A_MATERIAL");

        assertEquals(Material.BARRIER, templates.load(file).material());
    }

    @Test
    void aTypeIsMatchedIgnoringCase(@TempDir Path folder) throws IOException {
        Path file = templateFile(folder, "iron_sword");

        assertEquals(Material.IRON_SWORD, templates.load(file).material());
    }

    @Test
    void aMissingFileIsRefused(@TempDir Path folder) {
        Path missing = folder.resolve("missing.yml");

        assertThrows(IllegalArgumentException.class, () -> templates.load(missing));
        assertThrows(IllegalArgumentException.class, () -> templates.create(missing));
    }

    @Test
    void aFileWithoutANameOrATypeIsRefused(@TempDir Path folder) throws IOException {
        Path withoutName = Files.writeString(folder.resolve("nameless.yml"), "type: IRON_SWORD\n");
        Path withoutType = Files.writeString(folder.resolve("typeless.yml"), "name: Sword\n");

        assertThrows(IllegalArgumentException.class, () -> templates.load(withoutName));
        assertThrows(IllegalArgumentException.class, () -> templates.load(withoutType));
    }
}
