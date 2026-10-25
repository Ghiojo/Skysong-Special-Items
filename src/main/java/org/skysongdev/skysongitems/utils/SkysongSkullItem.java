package org.skysongdev.skysongitems.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkysongSkullItem extends SkysongItem{
    private String id;
    private String playerName;
    private String texture;

    /** Adds the skull's owner and texture to an item loaded from a file. */
    public SkysongSkullItem(SkysongItem base, String uuid, String playerName, String texture) {
        super(base);
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
