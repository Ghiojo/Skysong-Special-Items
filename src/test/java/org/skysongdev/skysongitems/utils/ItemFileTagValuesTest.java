package org.skysongdev.skysongitems.utils;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Item files store tags and values as two parallel lists. These tests cover
 * loading those lists back into tag/value pairs. The tags used are the ones
 * configured in the bundled config.yml: arcrot, charged_arcrot and fictis.
 */
class ItemFileTagValuesTest extends MockBukkitTest {
    @TempDir
    File folder;

    @BeforeEach
    void loadPlugin() {
        MockBukkit.load(SkysongItems.class);
    }

    private static NamespacedKey tag(String id) {
        return SkysongItems.getPlugin().getTagRegistry().getKey(id);
    }

    private static YamlConfiguration itemYaml(String type, List<String> tagIds, List<String> values) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", "Test Item");
        yaml.set("type", type);
        yaml.set("tags", tagIds);
        yaml.set("values", values);
        return yaml;
    }

    private File save(YamlConfiguration yaml) throws IOException {
        File file = new File(folder, "test_item.yml");
        yaml.save(file);
        return file;
    }

    private File writeItemFile(List<String> tagIds, List<String> values) throws IOException {
        return save(itemYaml("STONE", tagIds, values));
    }

    private static SkysongItem itemWithTags(Map<NamespacedKey, String> tagValues) {
        return new SkysongItem("Tagged Item", new ArrayList<>(List.of("physical", "functional", "skill")),
                "STONE", null, "", "", tagValues);
    }

    @Test
    void tagRemovedFromConfigDoesNotShiftLaterValues() throws IOException {
        File file = writeItemFile(List.of("arcrot", "retired_tag", "fictis"), List.of("1", "2", "3"));

        SkysongItem item = ItemTemplateRepository.load(file);

        assertEquals(Map.of(tag("arcrot"), "1", tag("fictis"), "3"), item.getTagValues());
    }

    @Test
    void tagWithoutAValueIsIgnored() throws IOException {
        File file = writeItemFile(List.of("arcrot", "fictis"), List.of("1"));

        SkysongItem item = ItemTemplateRepository.load(file);

        assertEquals(Map.of(tag("arcrot"), "1"), item.getTagValues());
    }

    @Test
    void tagIdsAreMatchedIgnoringCase() throws IOException {
        File file = writeItemFile(List.of("ARCROT"), List.of("4"));

        SkysongItem item = ItemTemplateRepository.load(file);

        assertEquals(Map.of(tag("arcrot"), "4"), item.getTagValues());
    }

    @Test
    void potionFileKeepsItsTagValuesAndColour() throws IOException {
        YamlConfiguration yaml = itemYaml("POTION", List.of("arcrot", "retired_tag", "fictis"), List.of("1", "2", "3"));
        yaml.set("potionColor", Color.RED.asRGB());

        SkysongItem item = ItemTemplateRepository.load(save(yaml));

        SkysongPotionItem potion = assertInstanceOf(SkysongPotionItem.class, item);
        assertEquals(Color.RED, potion.getColor());
        assertEquals(Map.of(tag("arcrot"), "1", tag("fictis"), "3"), potion.getTagValues());
    }

    @Test
    void skullFileKeepsItsTagValuesAndOwner() throws IOException {
        YamlConfiguration yaml = itemYaml("PLAYER_HEAD", List.of("arcrot", "retired_tag", "fictis"), List.of("1", "2", "3"));
        yaml.set("id", "00000000-0000-0000-0000-000000000001");
        yaml.set("playername", "Steve");
        yaml.set("texture", "texture-value");

        SkysongItem item = ItemTemplateRepository.load(save(yaml));

        SkysongSkullItem skull = assertInstanceOf(SkysongSkullItem.class, item);
        assertEquals("00000000-0000-0000-0000-000000000001", skull.getId());
        assertEquals("Steve", skull.getPlayerName());
        assertEquals("texture-value", skull.getTexture());
        assertEquals(Map.of(tag("arcrot"), "1", tag("fictis"), "3"), skull.getTagValues());
    }

    @Test
    void tagsAndValuesListsStayInStepWithEachOther() {
        LinkedHashMap<NamespacedKey, String> tagValues = new LinkedHashMap<>();
        tagValues.put(tag("fictis"), "3");
        tagValues.put(tag("arcrot"), "1");

        SkysongItem item = itemWithTags(tagValues);

        assertEquals(List.of("fictis", "arcrot"), item.getTagsAsStrings());
        assertEquals(List.of("3", "1"), item.getValues());
    }

    @Test
    void savedItemFileKeepsTheParallelListFormat() {
        LinkedHashMap<NamespacedKey, String> tagValues = new LinkedHashMap<>();
        tagValues.put(tag("arcrot"), "1");
        tagValues.put(tag("fictis"), "3");

        ItemTemplateRepository.save(itemWithTags(tagValues));

        YamlConfiguration saved = YamlConfiguration.loadConfiguration(savedFile());
        assertEquals(List.of("arcrot", "fictis"), saved.getStringList("tags"));
        assertEquals(List.of("1", "3"), saved.getStringList("values"));
    }

    @Test
    void savedItemLoadsBackWithTheSameTagValues() {
        Map<NamespacedKey, String> tagValues = Map.of(tag("arcrot"), "1", tag("charged_arcrot"), "2", tag("fictis"), "3");

        ItemTemplateRepository.save(itemWithTags(tagValues));

        assertEquals(tagValues, ItemTemplateRepository.load(savedFile()).getTagValues());
    }

    @Test
    void itemStackTagsAreReadWithTheirOwnValues() {
        // Built by hand: MockBukkit cannot run GenerateSkysongItem, which sets Paper data components.
        ItemStack stack = new ItemStack(Material.STONE);
        ItemMeta meta = stack.getItemMeta();
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        meta.getPersistentDataContainer().set(tag("arcrot"), PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(tag("fictis"), PersistentDataType.INTEGER, 3);
        stack.setItemMeta(meta);

        assertEquals(Map.of(tag("arcrot"), "1", tag("fictis"), "3"), new SkysongItem(stack).getTagValues());
    }

    private File savedFile() {
        return new File(ItemTemplateRepository.generatedItemsFolder(), "tagged_item.yml");
    }
}
