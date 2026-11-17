package org.skysongdev.skysongitems.registration;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.TraitDefinition;
import org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent;
import org.skysongdev.skysongitems.timer.CounterClock;
import org.skysongdev.skysongitems.timer.RealTimeClock;
import org.skysongdev.skysongitems.timer.TimeSpan;
import org.skysongdev.skysongitems.timer.TimerClocks;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.PrintableTrait;
import org.skysongdev.skysongitems.utils.RenameTrait;
import org.skysongdev.skysongitems.utils.TagRegistry;
import org.skysongdev.skysongitems.utils.TagTypes;
import org.skysongdev.skysongitems.utils.Trait;
import org.skysongdev.skysongitems.utils.TraitRegistry;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Checks and adds the tags and traits other plugins register during
 * {@link SkysongItemsRegisterEvent}, to registries that are not in use yet. A refused
 * registration logs a warning naming the plugins involved and never stops the others.
 */
final class PluginRegistrar implements SkysongItemsRegisterEvent.Registrar {
    // Ids become key names under the skysong_items namespace, so they follow the key rules.
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern UNIT_ID = Pattern.compile("[a-z]+");

    private final TagRegistry tags;
    private final TraitRegistry traits;
    private final TimerClocks clocks;
    private final Logger logger;

    PluginRegistrar(TagRegistry tags, TraitRegistry traits, TimerClocks clocks, Logger logger) {
        this.tags = tags;
        this.traits = traits;
        this.clocks = clocks;
        this.logger = logger;
    }

    @Override
    public boolean registerTimeUnit(@NotNull Plugin owner, @NotNull String unitId, @NotNull LongSupplier counter) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(counter, "counter");
        if (unitId == null || !UNIT_ID.matcher(unitId).matches()) {
            return refuse(owner, "time unit", String.valueOf(unitId), "units may only contain lowercase letters");
        }
        if (RealTimeClock.ID.equals(unitId) || TimeSpan.parse("1" + unitId).isPresent()) {
            return refuse(owner, "time unit", unitId, "it would read as real time");
        }
        String existingOwner = clocks.ownerOf(unitId);
        if (existingOwner != null) {
            return refuse(owner, "time unit", unitId, "it is already registered by " + existingOwner);
        }
        clocks.register(new CounterClock(unitId, counter), owner.getName());
        return true;
    }

    @Override
    public boolean registerTag(@NotNull Plugin owner, @NotNull String tagId, @NotNull TagType type) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(type, "type");
        if (!isValidId(owner, "tag", tagId)) {
            return false;
        }
        if (isBuiltInTag(tagId)) {
            return refuse(owner, "tag", tagId, "it is one of SkysongItems' built-in tags");
        }
        NamespacedKey key = ItemKeys.named(tagId);
        String existingOwner = tags.ownerOf(key);
        if (existingOwner != null) {
            return refuse(owner, "tag", tagId, "it is already registered by " + existingOwner);
        }
        if (tags.contains(key)) {
            return acceptConfiguredTag(owner, tagId, key, type);
        }
        tags.register(key, type, owner.getName());
        return true;
    }

    private boolean isBuiltInTag(String tagId) {
        NamespacedKey sameName = tags.getKey(tagId);
        return ItemKeys.isReserved(tagId) || (sameName != null && tags.isBuiltIn(sameName));
    }

    /** A tag config.yml already defines is kept as it is, as long as the types agree. */
    private boolean acceptConfiguredTag(Plugin owner, String tagId, NamespacedKey key, TagType type) {
        PersistentDataType<?, ?> configured = tags.getType(key);
        if (!configured.equals(TagTypes.dataType(type))) {
            return refuse(owner, "tag", tagId, "config.yml already defines it with another type");
        }
        logger.info(owner.getName() + " registered tag " + tagId + ", which config.yml already defines; using the config.yml one.");
        return true;
    }

    @Override
    public boolean registerTrait(@NotNull Plugin owner, @NotNull TraitDefinition definition) {
        Objects.requireNonNull(owner, "owner");
        String id = definition.id();
        if (!isValidId(owner, "trait", id)) {
            return false;
        }
        if (traits.contains(id)) {
            return handleExistingTrait(owner, id);
        }
        for (String valueTag : definition.valueTags()) {
            if (!tags.contains(ItemKeys.named(valueTag))) {
                return refuse(owner, "trait", id, "its value tag " + valueTag + " is not registered");
            }
        }
        traits.register(toTrait(definition), owner.getName());
        return true;
    }

    /** A trait config.yml already defines is kept as it is; one another plugin registered is refused. */
    private boolean handleExistingTrait(Plugin owner, String id) {
        String existingOwner = traits.ownerOf(id);
        if (existingOwner != null) {
            return refuse(owner, "trait", id, "it is already registered by " + existingOwner);
        }
        logger.info(owner.getName() + " registered trait " + id + ", which config.yml already defines; using the config.yml one.");
        return true;
    }

    private static Trait toTrait(TraitDefinition definition) {
        String icon = definition.icon().name();
        return switch (definition.kind()) {
            case BASE -> new Trait(definition.id(), definition.category(), icon, definition.name(),
                    definition.description());
            case PRINTABLE -> new PrintableTrait(definition.id(), definition.category(), icon, definition.name(),
                    definition.description(), definition.template(), new ArrayList<>(definition.valueTags()),
                    definition.weight());
            case RENAME -> new RenameTrait(definition.id(), definition.category(), icon, definition.name(),
                    definition.description(), definition.template());
        };
    }

    private boolean isValidId(Plugin owner, String kind, String id) {
        if (id == null || !VALID_ID.matcher(id).matches()) {
            return refuse(owner, kind, String.valueOf(id),
                    "ids may only contain lowercase letters, digits, '_', '-' and '.'");
        }
        return true;
    }

    private boolean refuse(Plugin owner, String kind, String id, String reason) {
        logger.warning("Refused " + kind + " " + id + " registered by " + owner.getName() + ": " + reason + ".");
        return false;
    }
}
