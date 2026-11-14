package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.TransformResult;
import org.skysongdev.skysongitems.api.event.ItemTransformEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.transform.TransformationLoader;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests {@code SkysongItemsApi.transformations()} and {@link ItemTransformEvent}, with a real transformations.yml. */
class ItemTransformationsApiTest extends MockBukkitTest {
    private SkysongItemsApi api;
    private PlayerMock player;
    private final List<ItemTransformEvent> events = new ArrayList<>();
    private boolean cancel;

    @BeforeEach
    void setUp() throws IOException {
        SkysongItems plugin = MockBukkit.load(SkysongItems.class);
        api = SkysongItemsApi.get();
        player = server.addPlayer();
        deleteSavedItems();
        ItemTemplateRepository.save(new SkysongItem(blade("Night Blade", "night_blade")));
        YamlConfiguration rules = new YamlConfiguration();
        rules.set("transformations.day_blade.to", "night_blade");
        rules.save(new File(plugin.getDataFolder(), TransformationLoader.FILE_NAME));
        plugin.reloadRegistries();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onTransform(ItemTransformEvent event) {
                events.add(event);
                event.setCancelled(cancel);
            }
        }, plugin);
    }

    /** Starts each test without templates another test may have left behind. */
    private static void deleteSavedItems() throws IOException {
        try (Stream<Path> paths = Files.walk(ItemTemplateRepository.generatedItemsFolder().toPath())) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    private static ItemStack blade(String name, String materialId) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, name);
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        meta.getPersistentDataContainer().set(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, materialId);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        return item;
    }

    private String heldForm() {
        return player.getInventory().getItemInMainHand().getPersistentDataContainer()
                .get(ItemKeys.MATERIAL_ID, PersistentDataType.STRING);
    }

    @Test
    void targetOfNamesTheSavedItemTheFormBecomes() {
        assertEquals(Optional.of("night_blade"), api.transformations().targetOf(blade("Day Blade", "day_blade")));
        assertTrue(api.transformations().targetOf(blade("Night Blade", "night_blade")).isEmpty());
        assertTrue(api.transformations().targetOf(null).isEmpty());
    }

    @Test
    void transformFiresTheEventThenChangesTheItem() {
        player.getInventory().setItemInMainHand(blade("Day Blade", "day_blade"));

        assertEquals(TransformResult.TRANSFORMED, api.transformations().transform(player));

        assertEquals("night_blade", heldForm());
        ItemTransformEvent event = events.get(0);
        assertEquals(player, event.getHolder());
        assertEquals("day_blade", event.getFromForm());
        assertEquals("night_blade", event.getToForm());
        assertEquals("night_blade", event.getTargetItemId());
        assertEquals("day_blade", event.getItem().getPersistentDataContainer().get(ItemKeys.MATERIAL_ID, PersistentDataType.STRING));
    }

    @Test
    void aCancelledEventLeavesTheItemAndTheCommandSaysNothing() {
        cancel = true;
        player.getInventory().setItemInMainHand(blade("Day Blade", "day_blade"));

        assertEquals(TransformResult.CANCELLED, api.transformations().transform(player));
        assertEquals("day_blade", heldForm());

        player.performCommand("transform");
        assertEquals("day_blade", heldForm());
        assertNull(player.nextComponentMessage());
    }

    @Test
    void noEventIsFiredForAnItemThatDoesNotTransform() {
        player.getInventory().setItemInMainHand(blade("Night Blade", "night_blade"));

        assertEquals(TransformResult.NO_TRANSFORMATION, api.transformations().transform(player));
        assertTrue(events.isEmpty());
    }
}
