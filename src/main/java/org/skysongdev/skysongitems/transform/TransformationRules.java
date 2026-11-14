package org.skysongdev.skysongitems.transform;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The transformations loaded from transformations.yml, looked up by the {@code material_id}
 * of the form an item is in. Immutable: a reload builds new rules and swaps them in.
 */
public final class TransformationRules {
    private static final TransformationRules EMPTY = new TransformationRules(Map.of(), List.of());

    private final Map<String, Transformation> byFromId;
    private final List<String> problems;

    /**
     * @param byFromId the rules, keyed by the {@code material_id} they start from, in file order
     * @param problems why each skipped rule was skipped, in file order
     */
    public TransformationRules(Map<String, Transformation> byFromId, List<String> problems) {
        this.byFromId = new LinkedHashMap<>(byFromId);
        this.problems = List.copyOf(problems);
    }

    /** No rules, as before transformations.yml is first loaded. */
    public static TransformationRules empty() {
        return EMPTY;
    }

    /** The rule for an item in this form. Empty when the id is null or no rule starts from it. */
    public Optional<Transformation> find(@Nullable String materialId) {
        return materialId == null ? Optional.empty() : Optional.ofNullable(byFromId.get(materialId));
    }

    /** Every loaded rule, in the order transformations.yml lists them. */
    public List<Transformation> all() {
        return List.copyOf(byFromId.values());
    }

    /** Why each rule that was skipped on load was skipped. */
    public List<String> problems() {
        return problems;
    }
}
