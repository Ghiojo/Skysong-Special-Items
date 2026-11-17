package org.skysongdev.skysongitems.transform;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Reads the rules of transformations.yml and checks each one against the saved items:
 *
 * <pre>
 * transformations:
 *   moon_blade_day:                 # the material_id of the form the item is in
 *     to: weapons/moon_blade_night  # the saved item it becomes
 *     permission: skysong.moon      # optional
 *     message: "The blade darkens." # optional
 *     parts: [functional, model]    # optional, see ItemPart; none swaps the whole item
 * messages:                         # optional, the lines /transform sends
 *   no-transformation: "This item doesn't transform."
 * </pre>
 *
 * A rule is skipped, with a warning, when it has no {@code to}, when {@code to} is not a
 * saved item, or when that item has no {@code material_id} or the same one as the rule,
 * since the {@code material_id} is what tells the item's forms apart. It is also skipped
 * when a part is unknown, or names a tag that is not registered.
 */
public final class TransformationLoader {
    public static final String FILE_NAME = "transformations.yml";

    private static final String SECTION = "transformations";
    private static final String TO = "to";
    private static final String PARTS = "parts";
    private static final String PERMISSION = "permission";
    private static final String MESSAGE = "message";
    private static final String MESSAGES = "messages";

    private static final String PART_NAMES = "name, physical, functional, skill, lore, traits, tags, "
            + "charges, hp, permission, model, cmd, material";

    private final Function<String, Optional<SkysongItem>> savedItems;
    private final Predicate<String> isTag;
    private final Logger logger;

    /**
     * @param savedItems finds a saved item by id, empty when there is none
     * @param isTag      whether a {@code tag:<id>} part names a registered tag
     * @param logger     where the skipped rules are reported
     */
    public TransformationLoader(Function<String, Optional<SkysongItem>> savedItems, Predicate<String> isTag, Logger logger) {
        this.savedItems = savedItems;
        this.isTag = isTag;
        this.logger = logger;
    }

    /** Finds the items saved in GeneratedItems right now, reading each file when asked for it. */
    public static Function<String, Optional<SkysongItem>> savedItemsOnDisk() {
        Set<String> ids = Set.copyOf(ItemTemplateRepository.savedItemIds());
        return id -> ids.contains(id)
                ? Optional.of(ItemTemplateRepository.load(ItemTemplateRepository.savedItemFile(id)))
                : Optional.empty();
    }

    /** Reads the rules from the contents of transformations.yml, logging each rule it skips. */
    public TransformationRules load(ConfigurationSection file) {
        Map<String, Transformation> rules = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        ConfigurationSection section = file.getConfigurationSection(SECTION);
        if (section != null) {
            for (String fromId : section.getKeys(false)) {
                readRule(fromId, section.getConfigurationSection(fromId), problems)
                        .ifPresent(rule -> rules.put(fromId, rule));
            }
        }
        problems.forEach(logger::warning);
        return new TransformationRules(rules, problems, TransformationMessages.from(file.getConfigurationSection(MESSAGES)));
    }

    private Optional<Transformation> readRule(String fromId, @Nullable ConfigurationSection rule, List<String> problems) {
        if (rule == null || !rule.isString(TO)) {
            return skip(problems, fromId, "it has no '" + TO + "' saved item");
        }
        Optional<List<ItemPart>> parts = readParts(rule.getStringList(PARTS));
        if (parts.isEmpty()) {
            return skip(problems, fromId, "its parts must be among " + PART_NAMES + ", or tag:<id> with a registered tag");
        }
        String targetId = rule.getString(TO);
        Optional<SkysongItem> target = savedItems.apply(targetId);
        if (target.isEmpty()) {
            return skip(problems, fromId, targetId + " is not a saved item");
        }
        String targetMaterialId = target.get().getTagValues().get(ItemKeys.MATERIAL_ID);
        if (targetMaterialId == null) {
            return skip(problems, fromId, "the saved item " + targetId + " has no material_id tag");
        }
        if (targetMaterialId.equals(fromId)) {
            return skip(problems, fromId, "the saved item " + targetId
                    + " has the same material_id, so the item would never change form");
        }
        return Optional.of(new Transformation(fromId, targetId, target.get(), targetMaterialId,
                rule.getString(PERMISSION), rule.getString(MESSAGE), parts.get()));
    }

    /** The parts named, or empty when one of them is unknown. */
    private Optional<List<ItemPart>> readParts(List<String> names) {
        List<ItemPart> parts = new ArrayList<>();
        for (String name : names) {
            Optional<ItemPart> part = ItemPart.parse(name).filter(this::isKnown);
            if (part.isEmpty()) {
                return Optional.empty();
            }
            parts.add(part.get());
        }
        return Optional.of(parts);
    }

    private boolean isKnown(ItemPart part) {
        return part.tagId() == null || isTag.test(part.tagId());
    }

    private static Optional<Transformation> skip(List<String> problems, String fromId, String reason) {
        problems.add(FILE_NAME + ": skipped the transformation of " + fromId + ": " + reason + ".");
        return Optional.empty();
    }
}
