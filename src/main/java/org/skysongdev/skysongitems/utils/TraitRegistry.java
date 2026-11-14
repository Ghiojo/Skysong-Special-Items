package org.skysongdev.skysongitems.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The item traits configured in the {@code properties} section of config.yml, and
 * the ones other plugins register through the API.
 *
 * <p>Each load builds new lists and swaps them in at once, so lookups from any thread
 * see either the old traits or the new ones, never a half-loaded registry.
 */
public class TraitRegistry {
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
        ConfigurationSection section = config.getConfigurationSection("properties");
        if (section == null) {
            logger.warning("No properties section found in the configuration.");
            publish(loaded, new HashMap<>());
            return;
        }
        for (String id : section.getKeys(false)) {
            logger.warning("Adding property: " + id);
            Trait trait = createTrait(config, id);
            loaded.add(trait);
            logger.info("Added " + trait.getClass().getSimpleName() + ": " + id);
        }
        publish(loaded, new HashMap<>());
        logger.info("Total properties added: " + loaded.size());
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
        String path = "properties." + id + ".";
        String type = config.getString(path + "type");
        String propType = config.getString(path + "proptype");
        String item = config.getString(path + "item");
        String name = config.getString(path + "name");
        String description = config.getString(path + "description");
        String printable = config.getString(path + "printable");
        ArrayList<String> valueTags = new ArrayList<>(config.getStringList(path + "valuetags"));
        int weight = config.getInt(path + "weight");

        return switch (type.toLowerCase()) {
            case "rename" -> new RenameTrait(id, propType, item, name, description, config.getString(path + "tag"));
            case "printable" -> new PrintableTrait(id, propType, item, name, description, printable, valueTags, weight);
            case "breweryprintable" -> new BreweryPrintableTrait(id, propType, item, name, description, printable, valueTags, weight);
            default -> new Trait(id, propType, item, name, description);
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
