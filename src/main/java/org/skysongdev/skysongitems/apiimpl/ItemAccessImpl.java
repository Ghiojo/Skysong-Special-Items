package org.skysongdev.skysongitems.apiimpl;

import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.ItemAccess;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.apiimpl.ItemWrites.ItemCheck;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.PropertyRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

/**
 * One way of reading and writing items, built from the plugin's registries and its
 * {@link ItemEditor}. The API offers two: the one items a player holds are edited
 * through, and the detached one for items nobody holds, which differ only in the editor
 * they write through and in the items they accept.
 */
final class ItemAccessImpl implements ItemAccess {
    private final ItemTags tags;
    private final ItemCharges charges;
    private final ItemProperties properties;

    /** Edits as the {@code /si} commands do: events, lore redraw, SkysongItems items only. */
    static ItemAccessImpl held(TagRegistry tagRegistry, PropertyRegistry propertyRegistry, ItemEditor editor) {
        return new ItemAccessImpl(tagRegistry, propertyRegistry, editor, ItemCheck.SKYSONG_ITEM);
    }

    /** Stores the value and nothing else, for an item nobody holds. */
    static ItemAccessImpl detached(TagRegistry tagRegistry, PropertyRegistry propertyRegistry, ItemEditor editor) {
        return new ItemAccessImpl(tagRegistry, propertyRegistry, editor.silently(), ItemCheck.ANY_ITEM);
    }

    private ItemAccessImpl(TagRegistry tagRegistry, PropertyRegistry propertyRegistry, ItemEditor editor, ItemCheck check) {
        this.charges = new ItemChargesImpl(editor, check);
        this.tags = new ItemTagsImpl(tagRegistry, charges, editor, check);
        this.properties = new ItemPropertiesImpl(propertyRegistry, editor, check);
    }

    @Override
    public @NotNull ItemTags tags() {
        return tags;
    }

    @Override
    public @NotNull ItemCharges charges() {
        return charges;
    }

    @Override
    public @NotNull ItemProperties properties() {
        return properties;
    }
}
