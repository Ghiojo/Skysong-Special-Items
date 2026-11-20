package org.skysongdev.skysongitems.transform;

import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.List;
import java.util.Objects;

/**
 * One rule of transformations.yml: an item whose {@code material_id} is {@link #fromId()}
 * transforms into the saved item {@link #targetId()}.
 *
 * @param fromId           the {@code material_id} of the form the item is in
 * @param targetId         the saved item it becomes, as {@code /si loaditem} names it
 * @param target           that saved item, read when the rules were loaded
 * @param targetMaterialId the target's {@code material_id}, never equal to {@code fromId}
 * @param permission       the permission a player needs to use this rule, or null for none
 * @param message          the line sent when the item transforms, or null for the default one
 * @param scope            the parts taken from the target and the tags and traits kept
 */
public record Transformation(String fromId, String targetId, SkysongItem target, String targetMaterialId,
                             @Nullable String permission, @Nullable String message, TransformScope scope) {
    public Transformation {
        Objects.requireNonNull(fromId, "fromId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(targetMaterialId, "targetMaterialId");
        Objects.requireNonNull(scope, "scope");
    }

    /** A rule that copies these parts, or swaps the whole item when there are none, keeping nothing extra. */
    public Transformation(String fromId, String targetId, SkysongItem target, String targetMaterialId,
                          @Nullable String permission, @Nullable String message, List<ItemPart> parts) {
        this(fromId, targetId, target, targetMaterialId, permission, message, new TransformScope(parts, List.of()));
    }

    /** A rule that swaps the whole item. */
    public Transformation(String fromId, String targetId, SkysongItem target, String targetMaterialId,
                          @Nullable String permission, @Nullable String message) {
        this(fromId, targetId, target, targetMaterialId, permission, message, TransformScope.WHOLE_ITEM);
    }

    /** The parts taken from the target; empty to swap the whole item. */
    public List<ItemPart> parts() {
        return scope.parts();
    }

    /** The ids of the tags and traits this rule keeps on top of the ones marked to be kept, lowercased. */
    public List<String> keep() {
        return scope.keep();
    }

    /** Whether the whole item is swapped, rather than single parts copied. */
    public boolean isWholeItem() {
        return scope.parts().isEmpty();
    }
}
