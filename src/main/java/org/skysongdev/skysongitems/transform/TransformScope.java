package org.skysongdev.skysongitems.transform;

import java.util.List;
import java.util.Locale;

/**
 * What one rule of transformations.yml takes from the new form and what it keeps from
 * the held item.
 *
 * @param parts the parts taken from the new form; empty to swap the whole item
 * @param keep  the ids of the tags and traits this rule keeps on the item, on top of the
 *              ones marked {@code keep_on_transform}; lowercased
 */
public record TransformScope(List<ItemPart> parts, List<String> keep) {
    /** Swaps the whole item and keeps only what is marked {@code keep_on_transform}. */
    public static final TransformScope WHOLE_ITEM = new TransformScope(List.of(), List.of());

    public TransformScope {
        parts = List.copyOf(parts);
        keep = keep.stream().map(id -> id.toLowerCase(Locale.ROOT)).toList();
    }
}
