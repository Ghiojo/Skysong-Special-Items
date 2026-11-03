package org.skysongdev.skysongitems.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.api.TagType;

import java.util.Objects;

/**
 * Fired after a tag on an item was set, changed or removed, whether through
 * {@code /si tag set|remove}, another {@code /si} command that writes a tag, or the API.
 * Changes to {@code charges} fire {@link ItemChargesChangeEvent} instead.
 *
 * <p>Fired on the server's main thread, once for every tag whose value is different
 * afterwards. Values are typed by {@link #getType()}: {@code Integer} for
 * {@link TagType#INT}, {@code Float}, {@code Boolean} or {@code String}. Not cancellable:
 * the change has already happened.
 */
public final class ItemTagChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player holder;
    private final ItemStack item;
    private final String tagId;
    private final TagType type;
    private final Object oldValue;
    private final Object newValue;
    private final ItemChangeCause cause;

    /**
     * @param holder   the player holding the item, or null when unknown
     * @param item     the item after the change; the event keeps its own copy
     * @param tagId    the tag's id, as {@code /si tag set} takes it
     * @param type     the type of value the tag holds
     * @param oldValue the value before the change, null when the item didn't have the tag
     * @param newValue the value after the change, null when the tag was removed
     * @param cause    what changed the tag
     */
    public ItemTagChangeEvent(@Nullable Player holder, @NotNull ItemStack item, @NotNull String tagId,
                              @NotNull TagType type, @Nullable Object oldValue, @Nullable Object newValue,
                              @NotNull ItemChangeCause cause) {
        this.holder = holder;
        this.item = item.clone();
        this.tagId = Objects.requireNonNull(tagId, "tagId");
        this.type = Objects.requireNonNull(type, "type");
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /** The player holding the item, or null when the change didn't name one, for example an API write without a holder. */
    public @Nullable Player getHolder() {
        return holder;
    }

    /** A copy of the item after the change. Changing it doesn't change the real item. */
    public @NotNull ItemStack getItem() {
        return item.clone();
    }

    /** The tag's id, as {@code /si tag set} and the API take it. */
    public @NotNull String getTagId() {
        return tagId;
    }

    /** The type of value the tag holds. */
    public @NotNull TagType getType() {
        return type;
    }

    /** The value before the change, null when the item didn't have the tag. */
    public @Nullable Object getOldValue() {
        return oldValue;
    }

    /** The value after the change, null when the tag was removed. */
    public @Nullable Object getNewValue() {
        return newValue;
    }

    /** What changed the tag. */
    public @NotNull ItemChangeCause getCause() {
        return cause;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /** The handlers of this event, as every Bukkit event needs. */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
