package org.skysongdev.skysongitems.api;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The old name of {@link TraitDefinition}, from before properties were renamed to traits.
 * Every method does the same as the {@link TraitDefinition} method of the same name.
 *
 * @deprecated use {@link TraitDefinition} with
 *             {@link org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent#registerTrait}.
 *             This name is removed in the next major version of the API.
 */
@Deprecated(since = "1.4.0", forRemoval = true)
public final class PropertyDefinition {
    /**
     * The old name of {@link TraitDefinition.Kind}.
     *
     * @deprecated use {@link TraitDefinition.Kind}.
     */
    @Deprecated(since = "1.4.0", forRemoval = true)
    public enum Kind {
        BASE,
        PRINTABLE,
        RENAME
    }

    private final TraitDefinition trait;

    private PropertyDefinition(TraitDefinition trait) {
        this.trait = trait;
    }

    /** See {@link TraitDefinition#base(String)}. */
    public static @NotNull PropertyDefinition base(@NotNull String id) {
        return new PropertyDefinition(TraitDefinition.base(id));
    }

    /** See {@link TraitDefinition#printable(String, String, List)}. */
    public static @NotNull PropertyDefinition printable(@NotNull String id, @NotNull String template,
                                                        @NotNull List<String> valueTags) {
        return new PropertyDefinition(TraitDefinition.printable(id, template, valueTags));
    }

    /** See {@link TraitDefinition#rename(String, String)}. */
    public static @NotNull PropertyDefinition rename(@NotNull String id, @NotNull String prefix) {
        return new PropertyDefinition(TraitDefinition.rename(id, prefix));
    }

    /** See {@link TraitDefinition#name(String)}. */
    public @NotNull PropertyDefinition name(@NotNull String name) {
        trait.name(name);
        return this;
    }

    /** See {@link TraitDefinition#description(String)}. */
    public @NotNull PropertyDefinition description(@NotNull String description) {
        trait.description(description);
        return this;
    }

    /** See {@link TraitDefinition#category(String)}. */
    public @NotNull PropertyDefinition category(@NotNull String category) {
        trait.category(category);
        return this;
    }

    /** See {@link TraitDefinition#icon(Material)}. */
    public @NotNull PropertyDefinition icon(@NotNull Material icon) {
        trait.icon(icon);
        return this;
    }

    /** See {@link TraitDefinition#weight(int)}. */
    public @NotNull PropertyDefinition weight(int weight) {
        trait.weight(weight);
        return this;
    }

    /**
     * The trait definition this one stands for, the one SkysongItems registers.
     * Setters called on either show in both.
     */
    public @NotNull TraitDefinition toTrait() {
        return trait;
    }

    public @NotNull Kind kind() {
        return Kind.valueOf(trait.kind().name());
    }

    public @NotNull String id() {
        return trait.id();
    }

    /** See {@link TraitDefinition#template()}. */
    public String template() {
        return trait.template();
    }

    /** See {@link TraitDefinition#valueTags()}. */
    public @NotNull List<String> valueTags() {
        return trait.valueTags();
    }

    public @NotNull String name() {
        return trait.name();
    }

    public @NotNull String description() {
        return trait.description();
    }

    public @NotNull String category() {
        return trait.category();
    }

    public @NotNull Material icon() {
        return trait.icon();
    }

    public int weight() {
        return trait.weight();
    }
}
