package org.skysongdev.skysongitems.transform;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.TransformResult;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests turning a held item into its next form. The bundled config.yml defines the int
 * tags arcrot and fictis.
 */
class ItemTransformerTest extends MockBukkitTest {
    private static final NamespacedKey ARCROT = ItemKeys.named("arcrot");
    private static final NamespacedKey FICTIS = ItemKeys.named("fictis");
    private static final NamespacedKey RANDOM_ID = ItemKeys.named("random_id");

    private SkysongItems plugin;
    private PlayerMock player;
    private final List<Player> synced = new ArrayList<>();
    private TransformationRules rules;
    private ItemTransformer transformer;

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
        rules = rulesFor(rule("day_blade", form("Night Blade", "night_blade", Map.of(ARCROT, "3"))),
                rule("night_blade", form("Day Blade", "day_blade", Map.of(FICTIS, "1"))));
        transformer = new ItemTransformer(() -> rules, plugin.getTagRegistry(), synced::add,
                plugin.getUpdater()::upgrade);
    }

    private static SkysongItem form(String name, String materialId, Map<NamespacedKey, String> tags) {
        Map<NamespacedKey, String> withId = new LinkedHashMap<>(tags);
        withId.put(ItemKeys.MATERIAL_ID, materialId);
        return new SkysongItem(name, new ArrayList<>(List.of(name + " physical", "functional", "skill")),
                "IRON_SWORD", null, "", "", withId);
    }

    private static Transformation rule(String fromId, SkysongItem target) {
        String targetId = target.getTagValues().get(ItemKeys.MATERIAL_ID);
        return new Transformation(fromId, "weapons/" + targetId, target, targetId, null, null);
    }

    private static TransformationRules rulesFor(Transformation... all) {
        Map<String, Transformation> byFromId = new LinkedHashMap<>();
        for (Transformation rule : all) {
            byFromId.put(rule.fromId(), rule);
        }
        return new TransformationRules(byFromId, List.of());
    }

    /** A day blade as a player would hold it. */
    private static ItemStack dayBlade() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(ItemKeys.NAME, PersistentDataType.STRING, "Day Blade");
        data.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        data.set(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, "day_blade");
        data.set(FICTIS, PersistentDataType.INTEGER, 1);
        ItemData.from(meta).setLoreSections(List.of("Day Blade physical", "functional", "skill"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack held() {
        return player.getInventory().getItemInMainHand();
    }

    private static <C> C read(ItemStack item, NamespacedKey key, PersistentDataType<?, C> type) {
        return item.getPersistentDataContainer().get(key, type);
    }

    private static ItemStack edited(ItemStack item, Consumer<ItemMeta> edit) {
        ItemMeta meta = item.getItemMeta();
        edit.accept(meta);
        item.setItemMeta(meta);
        return item;
    }

    @Test
    void anItemCyclesThroughItsFormsAndTheNewFormsTagsReplaceTheOldOnes() {
        player.getInventory().setItemInMainHand(dayBlade());

        assertEquals(TransformResult.TRANSFORMED, transformer.transform(player));
        assertEquals("night_blade", read(held(), ItemKeys.MATERIAL_ID, PersistentDataType.STRING));
        assertEquals("Night Blade", read(held(), ItemKeys.NAME, PersistentDataType.STRING));
        assertEquals(3, read(held(), ARCROT, PersistentDataType.INTEGER));
        assertFalse(held().getPersistentDataContainer().has(FICTIS), "the night form has no fictis");
        assertEquals(List.of(player), synced, "permission items are resynced after the change");

        assertEquals(TransformResult.TRANSFORMED, transformer.transform(player));
        assertEquals("day_blade", read(held(), ItemKeys.MATERIAL_ID, PersistentDataType.STRING));
        assertFalse(held().getPersistentDataContainer().has(ARCROT));
    }

    @Test
    void chargesAreKeptButNeverAboveTheNewFormsMaximum() {
        SkysongItem night = form("Night Blade", "night_blade", Map.of(ItemKeys.MAX_CHARGES, "5", ItemKeys.CHARGES, "5"));
        Transformation rule = rule("day_blade", night);
        ItemStack spent = edited(dayBlade(), meta -> meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 2));
        ItemStack full = edited(dayBlade(), meta -> meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 9));
        ItemStack none = dayBlade();

        assertEquals(2, read(transformer.transformed(spent, rule), ItemKeys.CHARGES, PersistentDataType.INTEGER));
        assertEquals(5, read(transformer.transformed(full, rule), ItemKeys.CHARGES, PersistentDataType.INTEGER));
        assertEquals(5, read(transformer.transformed(none, rule), ItemKeys.CHARGES, PersistentDataType.INTEGER),
                "an item without charges gets the new form's");
    }

    @Test
    void hpIsKeptButNeverAboveTheNewFormsMaximum() {
        SkysongItem night = form("Night Blade", "night_blade", Map.of(ItemKeys.MAX_ITEM_HP, "10", ItemKeys.ITEM_HP, "10"));
        Transformation rule = rule("day_blade", night);
        ItemStack damaged = edited(dayBlade(), meta -> meta.getPersistentDataContainer().set(ItemKeys.ITEM_HP, PersistentDataType.INTEGER, 3));
        ItemStack sturdy = edited(dayBlade(), meta -> meta.getPersistentDataContainer().set(ItemKeys.ITEM_HP, PersistentDataType.INTEGER, 15));

        assertEquals(3, read(transformer.transformed(damaged, rule), ItemKeys.ITEM_HP, PersistentDataType.INTEGER),
                "transforming never repairs an item");
        assertEquals(10, read(transformer.transformed(sturdy, rule), ItemKeys.ITEM_HP, PersistentDataType.INTEGER));
    }

    @Test
    void aFormWithoutChargesDropsThem() {
        ItemStack charged = edited(dayBlade(), meta -> meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 2));

        ItemStack night = transformer.transformed(charged, rules.find("day_blade").orElseThrow());

        assertNull(read(night, ItemKeys.CHARGES, PersistentDataType.INTEGER));
    }

    @Test
    void stackSizeDamageAndTheBoundBookCarryOver() {
        ItemStack worn = edited(dayBlade(), meta -> {
            ((Damageable) meta).setDamage(40);
            meta.getPersistentDataContainer().set(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER, 12);
            meta.getPersistentDataContainer().set(ItemKeys.RIGHT_CLICKABLE, PersistentDataType.BOOLEAN, true);
        });
        worn.setAmount(3);

        ItemStack night = transformer.transformed(worn, rules.find("day_blade").orElseThrow());

        assertEquals(3, night.getAmount());
        assertEquals(40, ((Damageable) night.getItemMeta()).getDamage());
        assertEquals(12, read(night, ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER));
        assertEquals(true, read(night, ItemKeys.RIGHT_CLICKABLE, PersistentDataType.BOOLEAN));
        assertEquals("day_blade", read(worn, ItemKeys.MATERIAL_ID, PersistentDataType.STRING), "the held item itself is not changed");
    }

    @Test
    void tagsOtherPluginsOwnAreAlwaysKept() {
        plugin.getTagRegistry().register(RANDOM_ID, TagType.STRING, "SkysongCrafting");
        ItemStack crafted = edited(dayBlade(), meta -> meta.getPersistentDataContainer().set(RANDOM_ID, PersistentDataType.STRING, "a1b2"));

        ItemStack night = transformer.transformed(crafted, rules.find("day_blade").orElseThrow());

        assertEquals("a1b2", read(night, RANDOM_ID, PersistentDataType.STRING));
    }

    @Test
    void aFormWithoutARuleIsLeftAsItIs() {
        ItemStack plain = new ItemStack(Material.STONE);
        player.getInventory().setItemInMainHand(plain);

        assertEquals(TransformResult.NO_TRANSFORMATION, transformer.transform(player));
        assertEquals(plain, held());
        assertTrue(synced.isEmpty());
    }

    @Test
    void anEmptyHandHasNothingToTransform() {
        assertEquals(TransformResult.NOT_HOLDING, transformer.transform(player));
    }

    @Test
    void aRulesPermissionIsRequired() {
        Transformation guarded = new Transformation("day_blade", "weapons/night_blade",
                form("Night Blade", "night_blade", Map.of()), "night_blade", "skysong.transform.blade", null);
        rules = rulesFor(guarded);
        ItemStack day = dayBlade();
        player.getInventory().setItemInMainHand(day);

        assertEquals(TransformResult.NO_PERMISSION, transformer.transform(player));
        assertEquals(day, held());

        player.addAttachment(plugin, "skysong.transform.blade", true);
        assertEquals(TransformResult.TRANSFORMED, transformer.transform(player));
        assertSame(player, synced.get(0));
    }
}
