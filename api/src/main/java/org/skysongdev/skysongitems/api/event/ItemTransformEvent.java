package org.skysongdev.skysongitems.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Fired before a held item transforms into its next form, through {@code /transform} or
 * the API. Cancelling it leaves the item as it is; {@code /transform} then sends no
 * message, so the listener that cancels says why if the player should know.
 *
 * <p>Fired on the server's main thread, only once the player may use the transformation.
 */
public final class ItemTransformEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player holder;
    private final ItemStack item;
    private final String fromForm;
    private final String toForm;
    private final String targetItemId;
    private boolean cancelled;

    /**
     * @param holder       the player holding the item
     * @param item         the item before it transforms; the event keeps its own copy
     * @param fromForm     the item's {@code material_id} now
     * @param toForm       its {@code material_id} after the transformation
     * @param targetItemId the saved item of the new form, as {@code /si loaditem} names it
     */
    public ItemTransformEvent(@NotNull Player holder, @NotNull ItemStack item, @NotNull String fromForm,
                              @NotNull String toForm, @NotNull String targetItemId) {
        this.holder = Objects.requireNonNull(holder, "holder");
        this.item = item.clone();
        this.fromForm = Objects.requireNonNull(fromForm, "fromForm");
        this.toForm = Objects.requireNonNull(toForm, "toForm");
        this.targetItemId = Objects.requireNonNull(targetItemId, "targetItemId");
    }

    /** The player holding the item. */
    public @NotNull Player getHolder() {
        return holder;
    }

    /** A copy of the item before it transforms. Changing it doesn't change the real item. */
    public @NotNull ItemStack getItem() {
        return item.clone();
    }

    /** The item's form now: its {@code material_id}. */
    public @NotNull String getFromForm() {
        return fromForm;
    }

    /** The item's form after the transformation: the new {@code material_id}. */
    public @NotNull String getToForm() {
        return toForm;
    }

    /** The saved item of the new form, by the id {@code /si loaditem} takes. */
    public @NotNull String getTargetItemId() {
        return targetItemId;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
