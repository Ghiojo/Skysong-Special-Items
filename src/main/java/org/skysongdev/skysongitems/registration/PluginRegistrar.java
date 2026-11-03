package org.skysongdev.skysongitems.registration;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.PropertyDefinition;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.PrintableProperty;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.PropertyRegistry;
import org.skysongdev.skysongitems.utils.RenameProperty;
import org.skysongdev.skysongitems.utils.TagRegistry;
import org.skysongdev.skysongitems.utils.TagTypes;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Checks and adds the tags and properties other plugins register during
 * {@link SkysongItemsRegisterEvent}, to registries that are not in use yet. A refused
 * registration logs a warning naming the plugins involved and never stops the others.
 */
final class PluginRegistrar implements SkysongItemsRegisterEvent.Registrar {
    // Ids become key names under the skysong_items namespace, so they follow the key rules.
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+");

    private final TagRegistry tags;
    private final PropertyRegistry properties;
    private final Logger logger;

    PluginRegistrar(TagRegistry tags, PropertyRegistry properties, Logger logger) {
        this.tags = tags;
        this.properties = properties;
        this.logger = logger;
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
    public boolean registerProperty(@NotNull Plugin owner, @NotNull PropertyDefinition definition) {
        Objects.requireNonNull(owner, "owner");
        String id = definition.id();
        if (!isValidId(owner, "property", id)) {
            return false;
        }
        if (properties.contains(id)) {
            return handleExistingProperty(owner, id);
        }
        for (String valueTag : definition.valueTags()) {
            if (!tags.contains(ItemKeys.named(valueTag))) {
                return refuse(owner, "property", id, "its value tag " + valueTag + " is not registered");
            }
        }
        properties.register(toProperty(definition), owner.getName());
        return true;
    }

    /** A property config.yml already defines is kept as it is; one another plugin registered is refused. */
    private boolean handleExistingProperty(Plugin owner, String id) {
        String existingOwner = properties.ownerOf(id);
        if (existingOwner != null) {
            return refuse(owner, "property", id, "it is already registered by " + existingOwner);
        }
        logger.info(owner.getName() + " registered property " + id + ", which config.yml already defines; using the config.yml one.");
        return true;
    }

    private static Property toProperty(PropertyDefinition definition) {
        String icon = definition.icon().name();
        return switch (definition.kind()) {
            case BASE -> new Property(definition.id(), definition.category(), icon, definition.name(),
                    definition.description());
            case PRINTABLE -> new PrintableProperty(definition.id(), definition.category(), icon, definition.name(),
                    definition.description(), definition.template(), new ArrayList<>(definition.valueTags()),
                    definition.weight());
            case RENAME -> new RenameProperty(definition.id(), definition.category(), icon, definition.name(),
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
