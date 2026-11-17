package org.skysongdev.skysongitems.apiimpl;

import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.ItemAccess;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemHp;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.api.ItemTraits;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.ItemTimers;
import org.skysongdev.skysongitems.apiimpl.ItemWrites.ItemCheck;
import org.skysongdev.skysongitems.timer.TimerActions;
import org.skysongdev.skysongitems.timer.TimerClocks;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.TraitRegistry;
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
    private final ItemHp itemHp;
    private final ItemTimers timers;
    // ItemProperties extends ItemTraits, so the one object serves traits() and the deprecated properties().
    @SuppressWarnings("removal")
    private final ItemProperties traits;

    /**
     * What the views read and write through.
     *
     * @param clocks  reads the times given to timed writes
     * @param actions the changes timers can make
     */
    record Writers(TagRegistry tagRegistry, TraitRegistry traitRegistry, ItemEditor editor, TimerClocks clocks,
                   TimerActions actions) {
        private Writers silently() {
            return new Writers(tagRegistry, traitRegistry, editor.silently(), clocks, actions);
        }
    }

    /** Edits as the {@code /si} commands do: events, lore redraw, SkysongItems items only. */
    static ItemAccessImpl held(Writers writers) {
        return new ItemAccessImpl(writers, ItemCheck.SKYSONG_ITEM);
    }

    /** Stores the value and nothing else, for an item nobody holds. */
    static ItemAccessImpl detached(Writers writers) {
        return new ItemAccessImpl(writers.silently(), ItemCheck.ANY_ITEM);
    }

    private ItemAccessImpl(Writers writers, ItemCheck check) {
        this.charges = new ItemChargesImpl(writers.editor(), check);
        this.itemHp = new ItemHpImpl(writers.editor(), check);
        TimedWrites timed = new TimedWrites(writers.clocks());
        this.timers = new ItemTimersImpl(check, writers.editor(), writers.actions(), timed);
        this.tags = new ItemTagsImpl(writers.tagRegistry(), charges, writers.editor(), check, timed);
        this.traits = new ItemTraitsImpl(writers.traitRegistry(), writers.editor(), check, timed);
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
    public @NotNull ItemHp itemHp() {
        return itemHp;
    }

    @Override
    public @NotNull ItemTimers timers() {
        return timers;
    }

    @Override
    public @NotNull ItemTraits traits() {
        return traits;
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ItemProperties properties() {
        return traits;
    }
}
