package org.skysongdev.skysongitems.transform;

import org.bukkit.NamespacedKey;
import org.skysongdev.skysongitems.utils.PrintableTrait;
import org.skysongdev.skysongitems.utils.TagRegistry;
import org.skysongdev.skysongitems.utils.Trait;
import org.skysongdev.skysongitems.utils.TraitRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * The tags and traits of one item that stay on it through one transform: the ones marked
 * {@code keep_on_transform}, the ones the rule's {@code keep} list names, and the value
 * tags of every kept trait. SkysongItems' built-in tags are never among them, since
 * charges, HP and the others carry over by their own rules.
 */
final class KeptOnTransform {
    private final List<String> keptTraits;
    private final Set<NamespacedKey> keptTags;

    /**
     * @param heldTraits the traits of the item before it transforms
     * @param ruleKeep   the rule's {@code keep} list, lowercased
     */
    KeptOnTransform(TagRegistry tags, TraitRegistry traits, List<String> heldTraits, List<String> ruleKeep) {
        this.keptTraits = heldTraits.stream().filter(id -> isKept(traits.get(id), id, ruleKeep)).toList();
        this.keptTags = new HashSet<>();
        for (NamespacedKey key : tags.getKeys()) {
            if (tags.isKeptOnTransform(key) || ruleKeep.contains(normalise(key.getKey()))) {
                keptTags.add(key);
            }
        }
        for (String traitId : keptTraits) {
            if (traits.get(traitId) instanceof PrintableTrait printable) {
                printable.getValuetags().stream().map(tags::getKey).filter(Objects::nonNull).forEach(keptTags::add);
            }
        }
        keptTags.removeIf(tags::isBuiltIn);
    }

    private static boolean isKept(Trait trait, String traitId, List<String> ruleKeep) {
        return (trait != null && trait.isKeptOnTransform()) || ruleKeep.contains(normalise(traitId));
    }

    private static String normalise(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    /** The held item's traits that stay on it, in its order. */
    List<String> traits() {
        return keptTraits;
    }

    /** Whether the tag under this key stays on the item, with the held item's value. */
    boolean keepsTag(NamespacedKey key) {
        return keptTags.contains(key);
    }
}
