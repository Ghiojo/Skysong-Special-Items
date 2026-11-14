package org.skysongdev.skysongitems.transform;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.CustomModelData;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests rules that take only some parts of the new form. The held day blade and the saved
 * night blade differ in every part, so each test can tell which parts moved. The bundled
 * config.yml defines the int tags arcrot and fictis and the printable charges trait.
 */
class ItemPartsTransformTest extends MockBukkitTest {
    private static final NamespacedKey ARCROT = ItemKeys.named("arcrot");
    private static final NamespacedKey FICTIS = ItemKeys.named("fictis");
    private static final NamespacedKey RANDOM_ID = ItemKeys.named("random_id");
    private static final NamespacedKey NIGHT_MODEL = new NamespacedKey("skysong", "night_blade");

    private SkysongItems plugin;
    private ItemTransformer transformer;

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        transformer = new ItemTransformer(TransformationRules::empty, plugin.getTagRegistry(), ignored -> { },
                plugin.getUpdater()::upgrade);
    }

    private static SkysongItem nightBlade() {
        Map<NamespacedKey, String> tags = new LinkedHashMap<>();
        tags.put(ItemKeys.MATERIAL_ID, "night_blade");
        tags.put(ARCROT, "3");
        tags.put(ItemKeys.MAX_CHARGES, "4");
        tags.put(ItemKeys.CHARGES, "4");
        tags.put(ItemKeys.PERMISSION, "skysong.night");
        SkysongItem night = new SkysongItem("Night Blade", new ArrayList<>(List.of("Dark steel", "Cuts shadows", "Umbral strike")),
                "NETHERITE_SWORD", NIGHT_MODEL, "", "charges", tags);
        return night.withCustomModelData(new CustomModelData(List.of(2.0f), List.of(), List.of("night"), List.of()));
    }

    private static ItemStack dayBlade() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(ItemKeys.NAME, PersistentDataType.STRING, "Day Blade");
        data.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        data.set(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, "day_blade");
        data.set(FICTIS, PersistentDataType.INTEGER, 1);
        data.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, 8);
        data.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 6);
        ItemData.from(meta).setLoreSections(List.of("Bright steel", "Cuts light", "Solar strike"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack transform(ItemStack held, String... parts) {
        List<ItemPart> parsed = Arrays.stream(parts).map(part -> ItemPart.parse(part).orElseThrow()).toList();
        Transformation rule = new Transformation("day_blade", "weapons/night_blade", nightBlade(), "night_blade",
                null, null, parsed);
        return transformer.transformed(held, rule);
    }

    private static <C> C read(ItemStack item, NamespacedKey key, PersistentDataType<?, C> type) {
        return item.getPersistentDataContainer().get(key, type);
    }

    private static List<String> loreSections(ItemStack item) {
        return ItemData.from(item.getItemMeta()).getLoreSections();
    }

    private static String displayName(ItemStack item) {
        return PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
    }

    @Test
    void onlyTheListedLoreSectionsChangeAndTheFormAlwaysDoes() {
        ItemStack result = transform(dayBlade(), "functional", "skill");

        assertEquals(List.of("Bright steel", "Cuts shadows", "Umbral strike"), loreSections(result));
        assertEquals("night_blade", read(result, ItemKeys.MATERIAL_ID, PersistentDataType.STRING));
        assertEquals("Day Blade", read(result, ItemKeys.NAME, PersistentDataType.STRING));
        assertEquals(Material.IRON_SWORD, result.getType());
        assertEquals(1, read(result, FICTIS, PersistentDataType.INTEGER));
    }

    @Test
    void physicalAndNameTakeTheFirstSectionAndTheName() {
        ItemStack result = transform(dayBlade(), "physical", "name");

        assertEquals(List.of("Dark steel", "Cuts light", "Solar strike"), loreSections(result));
        assertEquals("Night Blade", displayName(result));
    }

    @Test
    void traitsAndChargesTakeTheNewMaximumAndKeepTheChargesLeftUpToIt() {
        ItemStack result = transform(dayBlade(), "traits", "charges");

        assertEquals(List.of("charges"), ItemData.from(result.getItemMeta()).getTraits());
        assertEquals(4, read(result, ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER));
        assertEquals(4, read(result, ItemKeys.CHARGES, PersistentDataType.INTEGER), "6 charges left, capped at 4");
        List<String> lore = result.getItemMeta().lore().stream()
                .map(PlainTextComponentSerializer.plainText()::serialize).toList();
        assertEquals("[Charges: 4/4]", lore.get(lore.size() - 1), "the charges line is redrawn");
    }

    @Test
    void tagsReplaceTheItemsTagsButKeepOtherPluginsOnes() {
        plugin.getTagRegistry().register(RANDOM_ID, TagType.STRING, "SkysongCrafting");
        ItemStack held = dayBlade();
        ItemMeta meta = held.getItemMeta();
        meta.getPersistentDataContainer().set(RANDOM_ID, PersistentDataType.STRING, "a1b2");
        held.setItemMeta(meta);

        ItemStack result = transform(held, "tags");

        assertEquals(3, read(result, ARCROT, PersistentDataType.INTEGER));
        assertFalse(result.getPersistentDataContainer().has(FICTIS));
        assertEquals("a1b2", read(result, RANDOM_ID, PersistentDataType.STRING));
        assertEquals(6, read(result, ItemKeys.CHARGES, PersistentDataType.INTEGER), "charges are their own part");
        assertNull(read(result, ItemKeys.PERMISSION, PersistentDataType.STRING), "so is the permission");
    }

    @Test
    void aSingleTagIsCopiedOrRemovedToMatchTheNewForm() {
        ItemStack result = transform(dayBlade(), "tag:arcrot", "tag:fictis");

        assertEquals(3, read(result, ARCROT, PersistentDataType.INTEGER));
        assertFalse(result.getPersistentDataContainer().has(FICTIS));
    }

    @Test
    void permissionCmdAndMaterialComeFromTheNewForm() {
        // MockBukkit keeps no item model on item meta, so the model part is left to the smoke test.
        ItemStack result = transform(dayBlade(), "permission", "model", "cmd", "material");

        assertEquals("skysong.night", read(result, ItemKeys.PERMISSION, PersistentDataType.STRING));
        assertEquals(List.of("night"), result.getData(DataComponentTypes.CUSTOM_MODEL_DATA).strings());
        assertEquals(Material.NETHERITE_SWORD, result.getType());
        assertEquals("Day Blade", read(result, ItemKeys.NAME, PersistentDataType.STRING), "the material keeps the item's data");
    }

    @Test
    void anItemOfAnOlderVersionIsUpdatedFirst() {
        ItemStack old = dayBlade();
        ItemMeta meta = old.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(ItemKeys.VERSION, PersistentDataType.STRING, "1.1");
        data.set(ItemKeys.LEGACY_TRAITS, PersistentDataType.STRING, "spell");
        old.setItemMeta(meta);

        ItemStack result = transform(old, "skill");

        assertEquals(Constants.ITEM_VERSION, read(result, ItemKeys.VERSION, PersistentDataType.STRING));
        assertEquals(List.of("spell"), ItemData.from(result.getItemMeta()).getTraits());
        assertFalse(result.getPersistentDataContainer().has(ItemKeys.LEGACY_TRAITS));
    }
}
