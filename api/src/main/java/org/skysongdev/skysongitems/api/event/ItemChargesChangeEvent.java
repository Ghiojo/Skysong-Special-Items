package org.skysongdev.skysongitems.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Fired after an item's charges changed, whether through a {@code /si charges} or
 * {@code /si tag set charges} command, consumption, or the API.
 *
 * <p>Fired on the server's main thread, once per change, and only when the number of
 * charges is different afterwards. An item without charges counts as 0, so taking the
 * charges off an item reports its old charges and 0. Not cancellable: the change has
 * already happened.
 */
public final class ItemChargesChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player holder;
    private final ItemStack item;
    private final int oldCharges;
    private final int newCharges;
    private final ItemChangeCause cause;

    /**
     * @param holder     the player holding the item, or null when unknown
     * @param item       the item after the change; the event keeps its own copy
     * @param oldCharges the charges before the change
     * @param newCharges the charges after the change
     * @param cause      what changed the charges
     */
    public ItemChargesChangeEvent(@Nullable Player holder, @NotNull ItemStack item, int oldCharges, int newCharges,
                                  @NotNull ItemChangeCause cause) {
        this.holder = holder;
        this.item = item.clone();
        this.oldCharges = oldCharges;
        this.newCharges = newCharges;
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

    /** The charges before the change, 0 when the item had none. */
    public int getOldCharges() {
        return oldCharges;
    }

    /** The charges after the change, 0 when the item has none left. */
    public int getNewCharges() {
        return newCharges;
    }

    /** What changed the charges. */
    public @NotNull ItemChangeCause getCause() {
        return cause;
    }

    /** Whether consuming the item used up its last charge, so the item is gone. */
    public boolean isUsedUp() {
        return ItemChangeCause.CONSUMED.equals(cause) && newCharges <= 0;
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
