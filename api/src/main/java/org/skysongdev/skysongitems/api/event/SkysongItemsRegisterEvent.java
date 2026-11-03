package org.skysongdev.skysongitems.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.PropertyDefinition;
import org.skysongdev.skysongitems.api.TagType;

import java.util.Objects;

/**
 * Fired while SkysongItems loads its tags and properties, once all plugins have enabled
 * and again after every {@code /si reload}. Listen to it to add your plugin's own tags
 * and properties, so they don't have to be set up in SkysongItems' config.yml.
 * <p>
 * Every load starts from SkysongItems' own tags and config.yml, so register everything
 * you need each time the event fires. Nothing you register is written to disk, and it
 * is dropped when your plugin is disabled.
 * <p>
 * A registration is refused, with a warning in the console, when the id is not made of
 * lowercase letters, digits, {@code _}, {@code -} and {@code .}, when it is one of
 * SkysongItems' built-in tags, or when another plugin already registered it. A tag that
 * config.yml already defines with the same type is accepted.
 */
public final class SkysongItemsRegisterEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Registrar registrar;

    /** Fired by SkysongItems only. */
    @ApiStatus.Internal
    public SkysongItemsRegisterEvent(@NotNull Registrar registrar) {
        this.registrar = Objects.requireNonNull(registrar, "registrar");
    }

    /**
     * Registers an item tag, the same as one in the {@code tags} section of config.yml.
     * Its id is the name used by {@code /si tag set} and the API.
     *
     * @return whether the tag was registered
     */
    public boolean registerTag(@NotNull Plugin owner, @NotNull String tagId, @NotNull TagType type) {
        return registrar.registerTag(owner, tagId, type);
    }

    /**
     * Registers an item property, the same as one in the {@code properties} section of
     * config.yml. A printable property's value tags must already be registered, so
     * register your tags first.
     *
     * @return whether the property was registered
     */
    public boolean registerProperty(@NotNull Plugin owner, @NotNull PropertyDefinition property) {
        return registrar.registerProperty(owner, property);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    /** What SkysongItems does with each registration. */
    @ApiStatus.Internal
    public interface Registrar {
        boolean registerTag(@NotNull Plugin owner, @NotNull String tagId, @NotNull TagType type);

        boolean registerProperty(@NotNull Plugin owner, @NotNull PropertyDefinition property);
    }
}
