package org.skysongdev.skysongitems.apiimpl;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemTemplate;
import org.skysongdev.skysongitems.api.ItemTemplates;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.TaggedItems;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rebuilding an item from the data SkysongItems stores on it, through
 * {@code templates().snapshot} and {@code templates().rebuild}.
 */
class ItemRebuildTest extends MockBukkitTest {
    /** A tag no config registers, so a rebuilt item must not carry it. */
    private static final NamespacedKey UNREGISTERED = ItemKeys.named("not_a_tag");

    private ItemTemplates templates;

    @BeforeEach
    void enablePlugin() {
        MockBukkit.load(SkysongItems.class);
        templates = SkysongItemsApi.get().templates();
    }

    /** An item as SkysongItems generates them, carrying one registered and one unregistered tag. */
    private static ItemStack storedItem() {
        return TaggedItems.stones(3)
                .with(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION)
                .with(ItemKeys.NAME, PersistentDataType.STRING, "&6Ancient Sword")
                .with(ItemKeys.LORE, PersistentDataType.STRING, "physical§functional")
                .with(ItemKeys.PROPERTIES, PersistentDataType.STRING, "charges")
                .with(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, "ancient_sword")
                .with(ItemKeys.CHARGES, PersistentDataType.INTEGER, 5)
                .with(UNREGISTERED, PersistentDataType.STRING, "dropped")
                .build();
    }

    private static PersistentDataContainer dataOf(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer();
    }

    private static String plainName(ItemStack item) {
        return PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
    }

    @Test
    void theStoredNameDescriptionAndPropertiesAreRebuilt() {
        ItemStack rebuilt = templates.rebuild(storedItem());

        PersistentDataContainer data = dataOf(rebuilt);
        assertEquals(Material.STONE, rebuilt.getType());
        assertEquals("Ancient Sword", plainName(rebuilt));
        assertEquals("&6Ancient Sword", data.get(ItemKeys.NAME, PersistentDataType.STRING));
        assertEquals("physical§functional", data.get(ItemKeys.LORE, PersistentDataType.STRING));
        assertEquals("charges", data.get(ItemKeys.PROPERTIES, PersistentDataType.STRING));
        assertEquals(Constants.ITEM_VERSION, data.get(ItemKeys.VERSION, PersistentDataType.STRING));
    }

    @Test
    void registeredTagsAreKeptAndOtherTagsAreDropped() {
        ItemStack rebuilt = templates.rebuild(storedItem());

        PersistentDataContainer data = dataOf(rebuilt);
        assertEquals("ancient_sword", data.get(ItemKeys.MATERIAL_ID, PersistentDataType.STRING));
        assertEquals(5, data.get(ItemKeys.CHARGES, PersistentDataType.INTEGER));
        assertNull(data.get(UNREGISTERED, PersistentDataType.STRING));
    }

    @Test
    void everythingOutsideTheStoredDataIsLeftBehind() {
        ItemStack item = storedItem();
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

        ItemStack rebuilt = templates.rebuild(item);

        assertTrue(rebuilt.getEnchantments().isEmpty());
        assertEquals(1, rebuilt.getAmount());
    }

    @Test
    void theItemItselfIsNotChanged() {
        ItemStack item = storedItem();

        templates.rebuild(item);

        assertEquals(3, item.getAmount());
        assertEquals("dropped", dataOf(item).get(UNREGISTERED, PersistentDataType.STRING));
    }

    @Test
    void aTemplateIsReadOnceAndCreatesAFreshItemEachTime() {
        ItemStack item = storedItem();
        ItemTemplate template = templates.snapshot(item);
        rename(item, "&cRenamed");

        ItemStack first = template.create();
        ItemStack second = template.create();

        assertEquals("Ancient Sword", plainName(first));
        assertEquals("Ancient Sword", plainName(second));
        assertNotSame(first, second);
    }

    private static void rename(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, name);
        item.setItemMeta(meta);
    }

    @Test
    void itemsWithNothingToRebuildFromAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> templates.rebuild(null));
        assertThrows(IllegalArgumentException.class, () -> templates.rebuild(new ItemStack(Material.AIR)));
        assertThrows(IllegalArgumentException.class, () -> templates.rebuild(new ItemStack(Material.STONE)));
    }

    @Test
    void aSkysongItemWithoutAStoredNameOrDescriptionIsRefused() {
        ItemStack withoutDescription = TaggedItems.stones(1)
                .with(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION)
                .with(ItemKeys.NAME, PersistentDataType.STRING, "&6Ancient Sword")
                .build();
        ItemStack withoutName = TaggedItems.stones(1)
                .with(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION)
                .with(ItemKeys.LORE, PersistentDataType.STRING, "physical")
                .build();

        assertThrows(IllegalArgumentException.class, () -> templates.snapshot(withoutDescription));
        assertThrows(IllegalArgumentException.class, () -> templates.snapshot(withoutName));
    }
}
