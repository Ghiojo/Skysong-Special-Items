package org.skysongdev.skysongitems.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once SkysongItems' tags and properties are loaded and in use, right after
 * {@link SkysongItemsRegisterEvent}: once all plugins have enabled, and again after every
 * {@code /si reload}. Check your own configuration against SkysongItems' tags here.
 */
public final class SkysongItemsReadyEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
