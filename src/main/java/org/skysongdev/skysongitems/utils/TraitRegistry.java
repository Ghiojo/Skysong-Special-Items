package org.skysongdev.skysongitems.utils;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The item traits configured in the {@code traits} section of config.yml, and
 * the ones other plugins register through the API.
 *
 * <p>Configs written before traits were renamed still load: a {@code properties}
 * section is read when there is no {@code traits} one, and a trait's
 * {@code proptype} key is read when it has no {@code traittype}. Either logs a
 * warning naming the key to rename.
 *
 * <p>Each load builds new lists and swaps them in at once, so lookups from any thread
 * see either the old traits or the new ones, never a half-loaded registry.
 */
public class TraitRegistry {
    private static final String SECTION = "traits";
    private static final String LEGACY_SECTION = "properties";
    private static final String TRAIT_TYPE_KEY = "traittype";
    private static final String LEGACY_TRAIT_TYPE_KEY = "proptype";
    /**
     * Traits the plugin needs that were added after servers had their config.yml.
     * When the file lacks one, it is loaded from the bundled config.yml instead.
     */
    private static final List<String> BUILT_IN_TRAITS = List.of(ItemEditor.ITEM_HP_TRAIT);

    private volatile Snapshot snapshot = new Snapshot(List.of(), List.of(), Map.of());

    /**
     * The loaded traits and the trait types they use, published together, with
     * the plugin that registered each trait by lowercase id (config.yml ones have none).
     */
    private record Snapshot(List<Trait> traits, List<String> traitTypes, Map<String, String> owners) {
    }

    /** Replaces the loaded traits, and the trait types they use, with the ones in the config. */
    public void load(ConfigurationSection config, Logger logger) {
        List<Trait> loaded = new ArrayList<>();
        String sectionName = sectionName(config, logger);
        ConfigurationSection section = config.getConfigurationSection(sectionName);
        if (section == null) {
            logger.warning("No traits section found in the configuration.");
            publish(loaded, new HashMap<>());
            return;
        }
        List<String> legacyTypeKeys = new ArrayList<>();
        for (String id : section.getKeys(false)) {
            logger.warning("Adding trait: " + id);
            ConfigurationSection traitConfig = section.getConfigurationSection(id);
            if (usesLegacyTypeKey(traitConfig)) {
                legacyTypeKeys.add(id);
            }
            Trait trait = createTrait(traitConfig, id);
            loaded.add(trait);
            logger.info("Added " + trait.getClass().getSimpleName() + ": " + id);
        }
        addMissingBuiltIns(config, loaded, logger);
        publish(loaded, new HashMap<>());
        if (!legacyTypeKeys.isEmpty()) {
            logger.warning("These traits set their type with the old '" + LEGACY_TRAIT_TYPE_KEY
                    + "' key; rename it to '" + TRAIT_TYPE_KEY + "': " + String.join(", ", legacyTypeKeys));
        }
        logger.info("Total traits added: " + loaded.size());
    }

    /** Adds each built-in trait the config doesn't define, as the bundled config.yml defines it. */
    private static void addMissingBuiltIns(ConfigurationSection config, List<Trait> loaded, Logger logger) {
        Configuration defaults = config.getRoot() == null ? null : config.getRoot().getDefaults();
        if (defaults == null) {
            return;
        }
        for (String id : BUILT_IN_TRAITS) {
            ConfigurationSection builtIn = defaults.getConfigurationSection(SECTION + "." + id);
            if (builtIn != null && loaded.stream().noneMatch(trait -> trait.getId().equalsIgnoreCase(id))) {
                loaded.add(createTrait(builtIn, id));
                logger.info("config.yml has no '" + id + "' trait; using the built-in one.");
            }
        }
    }

    /**
     * The section the traits are read from: {@code traits}, or the old {@code properties}
     * when only that one is in the file. Only the file itself counts, not the bundled
     * defaults, since those always have a {@code traits} section.
     */
    private static String sectionName(ConfigurationSection config, Logger logger) {
        boolean hasTraits = config.contains(SECTION, true);
        boolean hasLegacy = config.contains(LEGACY_SECTION, true);
        if (hasTraits && hasLegacy) {
            logger.warning("config.yml has both a '" + SECTION + "' and an old '" + LEGACY_SECTION
                    + "' section; only '" + SECTION + "' is loaded.");
        } else if (hasLegacy) {
            logger.warning("config.yml uses the old '" + LEGACY_SECTION + "' section for traits; rename it to '"
                    + SECTION + "'. It still loads for now.");
            return LEGACY_SECTION;
        }
        return SECTION;
    }

    /**
     * Whether the trait sets its type only with the old {@code proptype} key. Like the
     * section, only the file counts: the bundled defaults set {@code traittype}.
     */
    private static boolean usesLegacyTypeKey(ConfigurationSection traitConfig) {
        return traitConfig != null && !traitConfig.contains(TRAIT_TYPE_KEY, true)
                && traitConfig.contains(LEGACY_TRAIT_TYPE_KEY, true);
    }

    private static String traitType(ConfigurationSection traitConfig) {
        String key = usesLegacyTypeKey(traitConfig) ? LEGACY_TRAIT_TYPE_KEY : TRAIT_TYPE_KEY;
        return traitConfig.getString(key);
    }

    /** Swaps in these traits, with the trait types they use in order, in one step. */
    private void publish(List<Trait> loaded, Map<String, String> owners) {
        List<String> types = new ArrayList<>();
        for (Trait trait : loaded) {
            if (!types.contains(trait.getPropType())) {
                types.add(trait.getPropType());
            }
        }
        snapshot = new Snapshot(Collections.unmodifiableList(loaded), Collections.unmodifiableList(types),
                Collections.unmodifiableMap(owners));
    }

    /**
     * Adds a trait registered by another plugin, after the configured ones. The caller
     * checks no trait with the same id is loaded first.
     */
    public void register(Trait trait, String owner) {
        Snapshot current = snapshot;
        List<Trait> traits = new ArrayList<>(current.traits());
        Map<String, String> owners = new HashMap<>(current.owners());
        traits.add(trait);
        owners.put(normalise(trait.getId()), owner);
        publish(traits, owners);
    }

    /** Removes every trait the named plugin registered, and the trait types only they used. */
    public void removeOwnedBy(String owner) {
        Snapshot current = snapshot;
        if (!current.owners().containsValue(owner)) {
            return;
        }
        List<Trait> traits = new ArrayList<>(current.traits());
        Map<String, String> owners = new HashMap<>(current.owners());
        traits.removeIf(trait -> owner.equals(owners.get(normalise(trait.getId()))));
        owners.values().removeIf(owner::equals);
        publish(traits, owners);
    }

    /**
     * Puts the other registry's traits in use here in one step, so code holding this
     * registry sees a load built elsewhere without ever seeing it half-built.
     */
    public void replaceWith(TraitRegistry other) {
        snapshot = other.snapshot;
    }

    /** The name of the plugin that registered the trait, or null for a configured or unknown one. */
    public String ownerOf(String id) {
        return snapshot.owners().get(normalise(id));
    }

    private static String normalise(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    /** Builds the trait of the class named by its {@code type}; unknown types give a plain trait. */
    private static Trait createTrait(ConfigurationSection config, String id) {
        String type = config.getString("type");
        String traitType = traitType(config);
        String item = config.getString("item");
        String name = config.getString("name");
        String description = config.getString("description");
        String printable = config.getString("printable");
        ArrayList<String> valueTags = new ArrayList<>(config.getStringList("valuetags"));
        int weight = config.getInt("weight");

        return switch (type.toLowerCase()) {
            case "rename" -> new RenameTrait(id, traitType, item, name, description, config.getString("tag"));
            case "printable" -> new PrintableTrait(id, traitType, item, name, description, printable, valueTags, weight);
            case "breweryprintable" -> new BreweryPrintableTrait(id, traitType, item, name, description, printable, valueTags, weight);
            default -> new Trait(id, traitType, item, name, description);
        };
    }

    /** Whether a trait with this id is loaded, ignoring case. */
    public boolean contains(String id) {
        return get(id) != null;
    }

    /** The trait with this id, ignoring case, or null when there is none. */
    public Trait get(String id) {
        for (Trait trait : snapshot.traits()) {
            if (trait.getId().equalsIgnoreCase(id)) {
                return trait;
            }
        }
        return null;
    }

    /** Every loaded trait, in config order. */
    public List<Trait> getAll() {
        return snapshot.traits();
    }

    /** Every trait type used by a loaded trait, in config order. */
    public List<String> getTraitTypes() {
        return snapshot.traitTypes();
    }
}
