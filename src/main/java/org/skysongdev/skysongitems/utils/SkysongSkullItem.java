package org.skysongdev.skysongitems.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Map;

public class SkysongSkullItem extends SkysongItem{
    private String id;
    private String playerName;
    private String texture;

    public SkysongSkullItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, Map<NamespacedKey, String> tagValues, String uuid, String playerName, String texture) {
        super(name, lore, type, itemModel, deleted, properties, tagValues);
        this.id = uuid;
        this.playerName = playerName;
        this.texture = texture;
    }

    public SkysongSkullItem(String name, ArrayList<String> lore, String type, NamespacedKey itemModel, String deleted, String properties, Map<NamespacedKey, String> tagValues, String uuid, String playerName, String texture, CustomModelData customModelData) {
        super(name, lore, type, itemModel, deleted, properties, tagValues, customModelData);
        this.id = uuid;
        this.playerName = playerName;
        this.texture = texture;
    }

    public SkysongSkullItem(ItemStack item){
        super(item);

        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = skullMeta.getPlayerProfile();

        this.id = profile.getId() != null ? profile.getId().toString() : null;
        this.playerName = profile.getName() != null ? profile.getName() : null;
        this.texture = profile.getProperties().stream().filter(property -> property.getName().equals("textures"))
                .map(ProfileProperty::getValue)
                .findFirst()
                .orElse(null);;
    }

    public String getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTexture() {
        return texture;
    }
}
