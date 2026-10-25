package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;

/**
 * Every persistent data key the plugin reads from or writes to an item.
 * <p>
 * The key names are stored on existing items, so changing a name here breaks
 * every item already in circulation.
 */
public final class ItemKeys {
    /**
     * The namespace Bukkit derives from this plugin's name in plugin.yml
     * ("Skysong_Items", lowercased). Spelled out so the keys do not depend on
     * the plugin instance being loaded first.
     */
    public static final String NAMESPACE = "skysong_items";

    public static final NamespacedKey MAX_CHARGES = of("max_charges");
    public static final NamespacedKey CHARGES = of("charges");
    public static final NamespacedKey CAN_ADD_CHARGES = of("can_add_charges");
    public static final NamespacedKey VERSION = of("version");
    public static final NamespacedKey LORE = of("lore");
    public static final NamespacedKey NAME = of("name");
    public static final NamespacedKey DELETED_LINES = of("deleted_lines");
    public static final NamespacedKey PROPERTIES = of("properties");
    public static final NamespacedKey PERMISSION = of("permission");
    public static final NamespacedKey SHIFT_RIGHT_CLICKABLE = of("is_shift_rightclickable");
    public static final NamespacedKey MATERIAL_ID = of("material_id");
    public static final NamespacedKey ON_CONSUMPTION = of("on_consumption");
    public static final NamespacedKey DYE_COLOR = of("dye_color");
    public static final NamespacedKey DISABLED = of("is_disabled");

    public static final NamespacedKey BOUND_BOOK = of("bound_book");
    public static final NamespacedKey RIGHT_CLICKABLE = of("is_right_clickable");
    public static final NamespacedKey DM_ONLY = of("dm_only");

    /** Model key owned by the FurnitureLib plugin, not by this one. */
    public static final NamespacedKey FURNITURE_MODEL = new NamespacedKey("furniturelib", "model");

    private ItemKeys() {
    }

    /**
     * Builds a key in this plugin's namespace, for names that only exist at
     * runtime such as the tags configured in config.yml.
     */
    public static NamespacedKey of(String name) {
        return new NamespacedKey(NAMESPACE, name);
    }
}
