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

    public static final NamespacedKey MAX_CHARGES = named("max_charges");
    public static final NamespacedKey CHARGES = named("charges");
    public static final NamespacedKey CAN_ADD_CHARGES = named("can_add_charges");
    public static final NamespacedKey VERSION = named("version");
    public static final NamespacedKey LORE = named("lore");
    public static final NamespacedKey NAME = named("name");
    public static final NamespacedKey DELETED_LINES = named("deleted_lines");
    public static final NamespacedKey PROPERTIES = named("properties");
    public static final NamespacedKey PERMISSION = named("permission");
    public static final NamespacedKey SHIFT_RIGHT_CLICKABLE = named("is_shift_rightclickable");
    public static final NamespacedKey MATERIAL_ID = named("material_id");
    public static final NamespacedKey ON_CONSUMPTION = named("on_consumption");
    public static final NamespacedKey DYE_COLOR = named("dye_color");
    public static final NamespacedKey DISABLED = named("is_disabled");

    public static final NamespacedKey BOUND_BOOK = named("bound_book");
    public static final NamespacedKey RIGHT_CLICKABLE = named("is_right_clickable");
    public static final NamespacedKey DM_ONLY = named("dm_only");

    /** Model key owned by the FurnitureLib plugin, not by this one. */
    public static final NamespacedKey FURNITURE_MODEL = new NamespacedKey("furniturelib", "model");

    private ItemKeys() {
    }

    /**
     * Builds a key in this plugin's namespace, for names that only exist at
     * runtime such as the tags configured in config.yml.
     */
    public static NamespacedKey named(String name) {
        return new NamespacedKey(NAMESPACE, name);
    }
}
