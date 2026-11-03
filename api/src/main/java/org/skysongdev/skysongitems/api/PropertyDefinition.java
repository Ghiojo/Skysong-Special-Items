package org.skysongdev.skysongitems.api;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * A property another plugin registers through {@link org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent},
 * built like the three property kinds in SkysongItems' config.yml:
 * <ul>
 *     <li>{@link #base(String)}: a plain property, shown in {@code /si prop view} and on the item's details;</li>
 *     <li>{@link #printable(String, String, List)}: also prints a lore line, where {@code $1}, {@code $2}...
 *     are replaced by the values of the listed tags;</li>
 *     <li>{@link #rename(String, String)}: also adds a text in front of the item's name.</li>
 * </ul>
 * The setters return this definition so they can be chained. SkysongItems copies the values
 * when the property is registered, so changing a definition afterwards has no effect.
 */
public final class PropertyDefinition {
    /** The kinds of property, matching the {@code type} values in config.yml. */
    public enum Kind {
        BASE,
        PRINTABLE,
        RENAME
    }

    private final Kind kind;
    private final String id;
    private final String template;
    private final List<String> valueTags;
    private String name;
    private String description = "";
    private String category = "Other";
    private Material icon = Material.PAPER;
    private int weight;

    private PropertyDefinition(Kind kind, String id, String template, List<String> valueTags) {
        this.kind = kind;
        this.id = Objects.requireNonNull(id, "id");
        this.template = template;
        this.valueTags = List.copyOf(valueTags);
        this.name = id;
    }

    /** A plain property with this id. */
    public static @NotNull PropertyDefinition base(@NotNull String id) {
        return new PropertyDefinition(Kind.BASE, id, null, List.of());
    }

    /**
     * A property that prints {@code template} as a lore line, with {@code $1}, {@code $2}...
     * replaced by the item's values of {@code valueTags}, in order. Every value tag must be
     * registered when the property is.
     */
    public static @NotNull PropertyDefinition printable(@NotNull String id, @NotNull String template,
                                                        @NotNull List<String> valueTags) {
        return new PropertyDefinition(Kind.PRINTABLE, id, Objects.requireNonNull(template, "template"), valueTags);
    }

    /** A property that adds {@code prefix} (MiniMessage) in front of the item's name. */
    public static @NotNull PropertyDefinition rename(@NotNull String id, @NotNull String prefix) {
        return new PropertyDefinition(Kind.RENAME, id, Objects.requireNonNull(prefix, "prefix"), List.of());
    }

    /** The name shown in menus; the id when not set. */
    public @NotNull PropertyDefinition name(@NotNull String name) {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /** The description shown in menus; empty when not set. */
    public @NotNull PropertyDefinition description(@NotNull String description) {
        this.description = Objects.requireNonNull(description, "description");
        return this;
    }

    /** The group the property is listed under, config.yml's {@code proptype}; "Other" when not set. */
    public @NotNull PropertyDefinition category(@NotNull String category) {
        this.category = Objects.requireNonNull(category, "category");
        return this;
    }

    /** The item shown for the property in menus; paper when not set. */
    public @NotNull PropertyDefinition icon(@NotNull Material icon) {
        this.icon = Objects.requireNonNull(icon, "icon");
        return this;
    }

    /** For printable properties, where the line goes among the item's printed lines; 0 when not set. */
    public @NotNull PropertyDefinition weight(int weight) {
        this.weight = weight;
        return this;
    }

    public @NotNull Kind kind() {
        return kind;
    }

    public @NotNull String id() {
        return id;
    }

    /** The printed line of a printable property or the name prefix of a rename property; null for a base one. */
    public String template() {
        return template;
    }

    /** The tags whose values a printable property prints; empty for the other kinds. */
    public @NotNull List<String> valueTags() {
        return valueTags;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String description() {
        return description;
    }

    public @NotNull String category() {
        return category;
    }

    public @NotNull Material icon() {
        return icon;
    }

    public int weight() {
        return weight;
    }
}
