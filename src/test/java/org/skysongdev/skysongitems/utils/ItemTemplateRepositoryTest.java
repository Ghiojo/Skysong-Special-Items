package org.skysongdev.skysongitems.utils;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterisation tests for saving and loading item templates. Tag/value pairing is
 * covered separately by {@link ItemFileTagValuesTest}.
 */
class ItemTemplateRepositoryTest extends MockBukkitTest {
    private static final String NAME = "Template Item";
    private static final String FILE_NAME = "template_item.yml";

    @BeforeEach
    void loadPlugin() {
        MockBukkit.load(SkysongItems.class);
    }

    private static SkysongItem item(String type, NamespacedKey itemModel) {
        return new SkysongItem(NAME, new ArrayList<>(List.of("physical", "functional", "skill")),
                type, itemModel, "1,3", "charges", Map.of());
    }

    private static File savedFile() {
        return new File(ItemTemplateRepository.generatedItemsFolder(), FILE_NAME);
    }

    private static SkysongItem saveAndLoad(SkysongItem item) {
        ItemTemplateRepository.save(item);
        return ItemTemplateRepository.load(savedFile());
    }

    @Test
    void generatedItemsFolderIsCreatedWhenMissing() {
        File folder = new File(SkysongItems.getPlugin().getDataFolder(), "GeneratedItems");
        File[] leftovers = folder.listFiles();
        for (File leftover : leftovers == null ? new File[0] : leftovers) {
            leftover.delete();
        }
        folder.delete();
        assertFalse(folder.exists());

        File returned = ItemTemplateRepository.generatedItemsFolder();

        assertEquals(folder, returned);
        assertTrue(returned.isDirectory());
    }

    @Test
    void saveNamesTheFileAfterTheFormattedItemName() {
        ItemTemplateRepository.save(item("STONE", null));

        assertTrue(savedFile().isFile());
    }

    @Test
    void plainFieldsSurviveSaveAndLoad() {
        SkysongItem loaded = saveAndLoad(item("STONE", null));

        assertEquals(NAME, loaded.getName());
        assertEquals(List.of("physical", "functional", "skill"), loaded.getLore());
        assertEquals("STONE", loaded.getType());
        assertEquals("1,3", loaded.getDeleted());
        assertEquals("charges", loaded.getProperties());
    }

    @Test
    void itemModelSurvivesSaveAndLoad() {
        NamespacedKey model = new NamespacedKey("skysong", "wand");

        assertEquals(model, saveAndLoad(item("STONE", model)).getItemModel());
    }

    @Test
    void templateWithoutItemModelLoadsWithNone() {
        assertNull(saveAndLoad(item("STONE", null)).getItemModel());
    }

    @Test
    void itemModelWithoutAKeyLoadsAsNone() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", NAME);
        yaml.set("type", "STONE");
        yaml.set("itemModel", "skysong:");
        yaml.save(savedFile());

        assertNull(ItemTemplateRepository.load(savedFile()).getItemModel());
    }

    @Test
    void missingDeletedLinesAndPropertiesLoadAsEmpty() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", NAME);
        yaml.set("type", "STONE");
        yaml.save(savedFile());

        SkysongItem loaded = ItemTemplateRepository.load(savedFile());

        assertEquals("", loaded.getDeleted());
        assertEquals("", loaded.getProperties());
    }

    @Test
    void customModelDataSurvivesSaveAndLoad() {
        CustomModelData data = new CustomModelData(List.of(1.5f, 2f), List.of(true, false),
                List.of("glow"), List.of(Color.fromRGB(10, 20, 30)));

        CustomModelData loaded = saveAndLoad(item("STONE", null).withCustomModelData(data)).getCustomModelData();

        assertEquals(List.of(1.5f, 2f), loaded.getFloats());
        assertEquals(List.of(true, false), loaded.getFlags());
        assertEquals(List.of("glow"), loaded.getStrings());
        assertEquals(List.of(Color.fromRGB(10, 20, 30)), loaded.getColors());
    }

    @Test
    void malformedCustomModelDataColoursAreSkipped() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", NAME);
        yaml.set("type", "STONE");
        yaml.set("customModelData.colors", List.of("1,2", "a,b,c", " 4, 5, 6 "));
        yaml.save(savedFile());

        CustomModelData loaded = ItemTemplateRepository.load(savedFile()).getCustomModelData();

        assertEquals(List.of(Color.fromRGB(4, 5, 6)), loaded.getColors());
    }

    @Test
    void skullOwnerSurvivesSaveAndLoad() {
        SkysongItem skull = new SkysongSkullItem(item("PLAYER_HEAD", null),
                "00000000-0000-0000-0000-000000000001", "Steve", "texture-value");

        SkysongSkullItem loaded = assertInstanceOf(SkysongSkullItem.class, saveAndLoad(skull));

        assertEquals("00000000-0000-0000-0000-000000000001", loaded.getId());
        assertEquals("Steve", loaded.getPlayerName());
        assertEquals("texture-value", loaded.getTexture());
    }

    @Test
    void potionColourSurvivesSaveAndLoad() {
        SkysongItem potion = new SkysongPotionItem(item("SPLASH_POTION", null), Color.AQUA);

        SkysongPotionItem loaded = assertInstanceOf(SkysongPotionItem.class, saveAndLoad(potion));

        assertEquals(Color.AQUA, loaded.getColor());
    }
}
