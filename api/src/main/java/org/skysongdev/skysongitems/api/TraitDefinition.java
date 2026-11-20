package org.skysongdev.skysongitems.api;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * A trait another plugin registers through {@link org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent},
 * built like the three trait kinds in SkysongItems' config.yml:
 * <ul>
 *     <li>{@link #base(String)}: a plain trait, shown in {@code /si trait view} and on the item's details;</li>
 *     <li>{@link #printable(String, String, List)}: also prints a lore line, where {@code $1}, {@code $2}...
 *     are replaced by the values of the listed tags;</li>
 *     <li>{@link #rename(String, String)}: also adds a text in front of the item's name.</li>
 * </ul>
 * The setters return this definition so they can be chained. SkysongItems copies the values
 * when the trait is registered, so changing a definition afterwards has no effect.
 */
public final class TraitDefinition {
    /** The kinds of trait, matching the {@code type} values in config.yml. */
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
    private OnTransform onTransform = OnTransform.DROP;

    private TraitDefinition(Kind kind, String id, String template, List<String> valueTags) {
        this.kind = kind;
        this.id = Objects.requireNonNull(id, "id");
        this.template = template;
        this.valueTags = List.copyOf(valueTags);
        this.name = id;
    }

    /** A plain trait with this id. */
    public static @NotNull TraitDefinition base(@NotNull String id) {
        return new TraitDefinition(Kind.BASE, id, null, List.of());
    }

    /**
     * A trait that prints {@code template} as a lore line, with {@code $1}, {@code $2}...
     * replaced by the item's values of {@code valueTags}, in order. Every value tag must be
     * registered when the trait is.
     */
    public static @NotNull TraitDefinition printable(@NotNull String id, @NotNull String template,
                                                        @NotNull List<String> valueTags) {
        return new TraitDefinition(Kind.PRINTABLE, id, Objects.requireNonNull(template, "template"), valueTags);
    }

    /** A trait that adds {@code prefix} (MiniMessage) in front of the item's name. */
    public static @NotNull TraitDefinition rename(@NotNull String id, @NotNull String prefix) {
        return new TraitDefinition(Kind.RENAME, id, Objects.requireNonNull(prefix, "prefix"), List.of());
    }

    /** The name shown in menus; the id when not set. */
    public @NotNull TraitDefinition name(@NotNull String name) {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    /** The description shown in menus; empty when not set. */
    public @NotNull TraitDefinition description(@NotNull String description) {
        this.description = Objects.requireNonNull(description, "description");
        return this;
    }

    /** The group the trait is listed under, config.yml's {@code traittype}; "Other" when not set. */
    public @NotNull TraitDefinition category(@NotNull String category) {
        this.category = Objects.requireNonNull(category, "category");
        return this;
    }

    /** The item shown for the trait in menus; paper when not set. */
    public @NotNull TraitDefinition icon(@NotNull Material icon) {
        this.icon = Objects.requireNonNull(icon, "icon");
        return this;
    }

    /** For printable traits, where the line goes among the item's printed lines; 0 when not set. */
    public @NotNull TraitDefinition weight(int weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Whether the trait stays on an item when it transforms; {@link OnTransform#DROP} when not set.
     *
     * @since 1.16.0
     */
    public @NotNull TraitDefinition onTransform(@NotNull OnTransform onTransform) {
        this.onTransform = Objects.requireNonNull(onTransform, "onTransform");
        return this;
    }

    public @NotNull Kind kind() {
        return kind;
    }

    public @NotNull String id() {
        return id;
    }

    /** The printed line of a printable trait or the name prefix of a rename trait; null for a base one. */
    public String template() {
        return template;
    }

    /** The tags whose values a printable trait prints; empty for the other kinds. */
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

    /**
     * Whether the trait stays on an item when it transforms.
     *
     * @since 1.16.0
     */
    public @NotNull OnTransform onTransform() {
        return onTransform;
    }
}
