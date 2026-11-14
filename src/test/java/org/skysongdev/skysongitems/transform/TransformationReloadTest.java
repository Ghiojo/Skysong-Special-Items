package org.skysongdev.skysongitems.transform;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.commands.misc.SaveItem;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests that the plugin loads transformations.yml with every registry reload and after /si saveitem. */
class TransformationReloadTest extends MockBukkitTest {
    private SkysongItems plugin;
    private File rulesFile;

    @BeforeEach
    void setUp() throws IOException {
        plugin = MockBukkit.load(SkysongItems.class);
        rulesFile = new File(plugin.getDataFolder(), TransformationLoader.FILE_NAME);
        Files.deleteIfExists(rulesFile.toPath());
        deleteSavedItems();
    }

    /** Starts each test without templates another test may have left behind. */
    private static void deleteSavedItems() throws IOException {
        try (Stream<Path> paths = Files.walk(ItemTemplateRepository.generatedItemsFolder().toPath())) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    private void writeRule(String fromId, String targetId) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("transformations." + fromId + ".to", targetId);
        yaml.save(rulesFile);
    }

    private static ItemStack bladeInForm(String name, String materialId) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, name);
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        meta.getPersistentDataContainer().set(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, materialId);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        return item;
    }

    @Test
    void theFirstLoadCreatesTheBundledFileWithNoRules() {
        plugin.reloadRegistries();

        assertTrue(rulesFile.isFile());
        assertTrue(plugin.getTransformations().all().isEmpty());
        assertTrue(plugin.getTransformations().problems().isEmpty());
    }

    @Test
    void aRegistryReloadReadsTheRulesAgainstTheSavedItems() throws IOException {
        PlayerMock player = server.addPlayer();
        player.getInventory().setItemInMainHand(bladeInForm("Night Blade", "night_blade"));
        ItemTemplateRepository.save(new SkysongItem(player.getInventory().getItemInMainHand()));
        writeRule("day_blade", "night_blade");

        plugin.reloadRegistries();

        Transformation rule = plugin.getTransformations().find("day_blade").orElseThrow();
        assertEquals("night_blade", rule.targetMaterialId());
    }

    @Test
    void savingTheTargetMakesItsRuleLoadWithoutAReload() throws IOException {
        writeRule("day_blade", "night_blade");
        plugin.reloadRegistries();
        assertTrue(plugin.getTransformations().find("day_blade").isEmpty());
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "skysongitems.save", true);
        player.getInventory().setItemInMainHand(bladeInForm("Night Blade", "night_blade"));

        new SaveItem().onCommand(player, plugin.getCommand("skysongitems"), "si", new String[0]);

        assertTrue(plugin.getTransformations().find("day_blade").isPresent());
    }
}
