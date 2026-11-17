package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.timer.Timing;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.Trait;
import org.skysongdev.skysongitems.utils.TraitRegistry;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the traits stored on items and the ones configured in the plugin, and adds
 * or removes them through the {@link ItemEditor}. It implements the deprecated
 * {@link ItemProperties}, which extends {@link org.skysongdev.skysongitems.api.ItemTraits},
 * so plugins still built against the old name get the same object.
 */
@SuppressWarnings("removal")
final class ItemTraitsImpl implements ItemProperties {
    private final TraitRegistry registry;
    private final ItemEditor editor;
    private final ItemWrites.ItemCheck check;
    private final TimedWrites timed;

    ItemTraitsImpl(TraitRegistry registry, ItemEditor editor, ItemWrites.ItemCheck check, TimedWrites timed) {
        this.registry = registry;
        this.editor = editor;
        this.check = check;
        this.timed = timed;
    }

    @Override
    public boolean add(Player holder, ItemStack item, String traitId) {
        return add(holder, item, traitId, null);
    }

    @Override
    public boolean addFor(Player holder, ItemStack item, String traitId, String time) {
        return add(holder, item, traitId, timed.timing(time));
    }

    /** Adds the trait, then removes it after the time, or keeps it for good without one. */
    private boolean add(Player holder, ItemStack item, String traitId, Timing timing) {
        check.require(item);
        ItemEditor.Result result = editorFor(holder).addTrait(item, traitId);
        boolean added = !ItemEditor.Result.ALREADY_PRESENT.equals(result);
        if (added) {
            ItemWrites.requireDone(result, "trait " + traitId);
        }
        timed.then(item, timing, BuiltInTimerActions.REMOVE_TRAIT.id(), traitId);
        return added;
    }

    @Override
    public boolean remove(Player holder, ItemStack item, String traitId) {
        check.require(item);
        return ItemEditor.Result.DONE.equals(editorFor(holder).removeTrait(item, traitId));
    }

    private ItemEditor editorFor(Player holder) {
        return editor.editedBy(holder, ItemChangeCause.API);
    }

    @Override
    public List<String> get(ItemStack item) {
        StoredValues.requireMainThread();
        if (item == null || item.getType().isAir()) {
            return List.of();
        }
        return List.copyOf(ItemData.parseTraits(ItemData.readTraitsText(item.getPersistentDataContainer())));
    }

    @Override
    public boolean has(ItemStack item, String traitId) {
        return get(item).contains(traitId);
    }

    @Override
    public boolean exists(String traitId) {
        return registry.contains(traitId);
    }

    @Override
    public Set<String> ids() {
        Set<String> ids = new LinkedHashSet<>();
        for (Trait trait : registry.getAll()) {
            ids.add(trait.getId());
        }
        return Collections.unmodifiableSet(ids);
    }
}
