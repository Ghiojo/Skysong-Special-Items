package org.skysongdev.skysongitems.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.PropertyDefinition;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.TraitDefinition;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Fired while SkysongItems loads its tags and traits, once all plugins have enabled
 * and again after every {@code /si reload}. Listen to it to add your plugin's own tags,
 * traits and units of time, so they don't have to be set up in SkysongItems' config.yml.
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
     * Registers an item trait, the same as one in the {@code traits} section of
     * config.yml. A printable trait's value tags must already be registered, so
     * register your tags first.
     *
     * @return whether the trait was registered
     */
    public boolean registerTrait(@NotNull Plugin owner, @NotNull TraitDefinition trait) {
        return registrar.registerTrait(owner, trait);
    }

    /**
     * Registers a unit of time that your plugin counts, such as the seasons of a calendar,
     * so item timers can count in it: {@code /si transform every 1season}, or
     * {@code timers().every(item, change, "2season")}.
     *
     * <p>The counter only says how many units have passed so far, and must never go down.
     * SkysongItems asks for it only when an item's timers are checked, never on a schedule,
     * so the plugin doesn't have to tick it: a calendar can work it out from the date or
     * from an admin's command. A timer in the unit is due once the counter reaches its
     * count, and catches up on every unit it missed, the same as real time.
     *
     * <p>Timers in a unit no plugin has registered, such as after your plugin is removed,
     * are kept on items and wait; commands using it are refused.
     *
     * @param unitId the unit as staff type it after a number, lowercase letters only, such
     *               as {@code season}; it can't be read as real time ({@code s}, {@code m},
     *               {@code h}, {@code d}, {@code w}, {@code mo}, {@code y}) or be {@code real}
     * @param counter how many units have passed so far
     * @return whether the unit was registered
     * @since 1.13.0
     */
    public boolean registerTimeUnit(@NotNull Plugin owner, @NotNull String unitId, @NotNull LongSupplier counter) {
        return registrar.registerTimeUnit(owner, unitId, counter);
    }

    /**
     * Same as {@link #registerTrait(Plugin, TraitDefinition)}, under its name from before
     * properties were renamed to traits.
     *
     * @return whether the trait was registered
     * @deprecated use {@link #registerTrait(Plugin, TraitDefinition)}. This method is
     *             removed in the next major version of the API.
     */
    @Deprecated(since = "1.4.0", forRemoval = true)
    public boolean registerProperty(@NotNull Plugin owner, @NotNull PropertyDefinition property) {
        return registerTrait(owner, property.toTrait());
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

        boolean registerTrait(@NotNull Plugin owner, @NotNull TraitDefinition trait);

        boolean registerTimeUnit(@NotNull Plugin owner, @NotNull String unitId, @NotNull LongSupplier counter);
    }
}
