package org.skysongdev.skysongitems.transform;

import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.SkysongItem;

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
 */
public record Transformation(String fromId, String targetId, SkysongItem target, String targetMaterialId,
                             @Nullable String permission, @Nullable String message) {
    public Transformation {
        Objects.requireNonNull(fromId, "fromId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(targetMaterialId, "targetMaterialId");
    }
}
