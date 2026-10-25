package org.skysongdev.skysongitems.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/** Builds item stacks from {@link SkysongItem} templates. */
public final class ItemFactory {
    private ItemFactory() {
    }

    /**
     * Builds the item a template describes. Player heads get the template's skin,
     * potions get the template's colour.
     */
    public static ItemStack create(SkysongItem template) {
        if (Objects.equals(template.getType(), Material.PLAYER_HEAD.toString())) {
            SkysongSkullItem skull = (SkysongSkullItem) template;
            getPlugin().getLogger().info("Generating SkysongSkullItem with ID: " + skull.getId() + ", Player Name: " + skull.getPlayerName() + ", Texture: " + skull.getTexture());
            return create(template, skull, null);
        }
        if (isPotion(template.getType())) {
            Color color = ((SkysongPotionItem) template).getColor();
            getPlugin().getLogger().info("Generating SkysongPotionItem with color: " + color);
            return create(template, null, color);
        }
        return create(template, null, null);
    }

    /** True for items made by this plugin, which carry the {@link ItemKeys#VERSION} tag. */
    public static boolean isSkysongItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(ItemKeys.VERSION, PersistentDataType.STRING);
    }

    private static boolean isPotion(String type) {
        return Objects.equals(type, Material.POTION.toString())
                || Objects.equals(type, Material.SPLASH_POTION.toString())
                || Objects.equals(type, Material.LINGERING_POTION.toString());
    }

    /**
     * @param skull       the template as a skull, or null when the item gets no skin
     * @param potionColor the potion colour, or null when the item is not a coloured potion
     */
    private static ItemStack create(SkysongItem template, SkysongSkullItem skull, Color potionColor) {
        ItemStack item = new ItemStack(materialOf(template.getType()));
        ItemMeta meta = item.getItemMeta();
        applyTemplate(meta, template);

        if (skull != null && skull.getId() != null && !skull.getId().isEmpty()) {
            applySkin((SkullMeta) meta, skull);
            item.setItemMeta(meta);
            return item;
        }

        item.setItemMeta(meta);
        applyCustomModelData(item, template.getCustomModelData());
        if (potionColor != null) {
            applyPotionColor(item, potionColor);
        }
        return item;
    }

    /** The template's material, or {@link Material#BARRIER} when the name matches none. */
    static Material materialOf(String type) {
        Material material = Material.getMaterial(type.toUpperCase());
        return material == null ? Material.BARRIER : material;
    }

    /** Writes the template's name, lore, item model, properties, version and tags into the item meta. */
    static void applyTemplate(ItemMeta meta, SkysongItem template) {
        String name = template.getName();
        meta.displayName(getPlugin().getMiniMessage().deserialize(TextParser.translateColorCodes(TextParser.translateHexColorCodes("&#", "", name))).decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, name);

        ItemData data = ItemData.from(meta);
        data.setLoreSections(template.getLore());
        meta.lore(TextParser.TranslateToLore(template.getLore(), template.getDeleted()));

        String deleted = template.getDeleted();
        if (deleted != null && !deleted.isEmpty()) {
            data.setDeletedLines(deleted);
        }

        NamespacedKey itemModel = template.getItemModel();
        if (itemModel != null) {
            meta.setItemModel(itemModel);
        }
        getPlugin().getLogger().warning("custommodeldata: " + itemModel);

        String properties = template.getProperties();
        if (properties != null && !properties.isEmpty()) {
            data.setPropertiesText(properties);
        }

        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        applyTags(meta.getPersistentDataContainer(), template.getTagsAsStrings(), template.getValues());
    }

    /** Stores each configured tag with its value, converted to the tag's type. Unknown tags are skipped. */
    private static void applyTags(PersistentDataContainer container, List<String> tagIds, List<String> values) {
        TagRegistry tagRegistry = getPlugin().getTagRegistry();
        for (int i = 0; i < tagIds.size(); i++) {
            if (tagRegistry.contains(tagIds.get(i))) {
                applyTag(container, tagRegistry, tagIds.get(i), values.get(i));
            }
        }
    }

    private static void applyTag(PersistentDataContainer container, TagRegistry tagRegistry, String tagId, String value) {
        NamespacedKey key = tagRegistry.getKey(tagId);
        PersistentDataType<?, ?> type = tagRegistry.getType(key);
        if (PersistentDataType.BOOLEAN.equals(type)) {
            container.set(key, PersistentDataType.BOOLEAN, Boolean.valueOf(value));
        } else if (PersistentDataType.INTEGER.equals(type)) {
            container.set(key, PersistentDataType.INTEGER, Integer.valueOf(value));
        } else if (PersistentDataType.FLOAT.equals(type)) {
            container.set(key, PersistentDataType.FLOAT, Float.valueOf(value));
        } else if (PersistentDataType.STRING.equals(type)) {
            container.set(key, PersistentDataType.STRING, value);
        } else {
            getPlugin().getLogger().warning("Unknown tag type for tag: " + tagId);
        }
    }

    private static void applySkin(SkullMeta meta, SkysongSkullItem skull) {
        PlayerProfile profile = getPlugin().getServer().createProfile(UUID.fromString(skull.getId()), skull.getPlayerName());
        profile.setProperty(new ProfileProperty("textures", skull.getTexture()));
        meta.setPlayerProfile(profile);
    }

    private static void applyCustomModelData(ItemStack item, CustomModelData customModelData) {
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, io.papermc.paper.datacomponent.item.CustomModelData.customModelData()
                .addStrings(customModelData.getStrings())
                .addFloats(customModelData.getFloats())
                .addFlags(customModelData.getFlags())
                .addColors(customModelData.getColors()));
    }

    private static void applyPotionColor(ItemStack item, Color potionColor) {
        PotionContents contents = item.getData(DataComponentTypes.POTION_CONTENTS);
        PotionContents newContents = PotionContents.potionContents().potion(contents.potion()).customColor(potionColor).build();
        item.setData(DataComponentTypes.POTION_CONTENTS, newContents);
    }
}
