package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemTemplates;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The template reads that can run under MockBukkit. Building an item from a valid
 * template sets Paper data components, which MockBukkit cannot do (see ItemFactoryTest),
 * so the successful create paths need the server smoke test.
 */
class ItemTemplateReaderTest extends MockBukkitTest {
    private ItemTemplates templates;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        templates = SkysongItemsApi.get().templates();
    }

    @Test
    void idsAreTheLoadedTemplateNames() {
        Constants.itemFileNames.add("sword");

        assertTrue(templates.ids().contains("sword"));
        assertThrows(UnsupportedOperationException.class, () -> templates.ids().add("other"));
    }

    @Test
    void anUnknownIdCreatesNothing() {
        assertEquals(Optional.empty(), templates.create("missing"));
        assertEquals(Optional.empty(), templates.create((String) null));
    }

    @Test
    void anIdOutsideTheTemplatesFolderCreatesNothing() {
        assertEquals(Optional.empty(), templates.create("../config"));
    }

    @Test
    void aMissingFileIsRefused(@TempDir Path folder) {
        Path missing = folder.resolve("missing.yml");

        assertThrows(IllegalArgumentException.class, () -> templates.create(missing));
    }

    @Test
    void aFileThatIsNotATemplateIsRefused(@TempDir Path folder) throws IOException {
        Path notATemplate = Files.writeString(folder.resolve("other.yml"), "database:\n  host: localhost\n");

        assertThrows(IllegalArgumentException.class, () -> templates.create(notATemplate));
    }

    @Test
    void skysongItemsAreRecognisedByTheirVersion() {
        ItemStack item = TaggedItems.stones(1).with(ItemKeys.VERSION, PersistentDataType.STRING, "2").build();

        assertTrue(templates.isSkysongItem(item));
        assertFalse(templates.isSkysongItem(new ItemStack(Material.STONE)));
        assertFalse(templates.isSkysongItem(null));
    }
}
