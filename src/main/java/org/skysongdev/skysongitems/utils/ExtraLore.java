package org.skysongdev.skysongitems.utils;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ExtraLorePosition;
import org.skysongdev.skysongitems.api.LoreSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;

/**
 * The lore lines other plugins add to an item, as stored on it: a container under
 * {@link ItemKeys#EXTRA_LORE} holding, for each plugin, its lines as Adventure JSON under
 * {@code <plugin>:lines} and their position under {@code <plugin>:position}. Owners are
 * the plugins' names, lowercased, as Bukkit puts them in a plugin's keys.
 */
public final class ExtraLore {
    private static final String LINES = "lines";
    private static final String POSITION = "position";
    private static final String AFTER_TRAITS = "after_traits";
    private static final String ABOVE = "above:";
    private static final String BELOW = "below:";
    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();
    private static final ExtraLore NONE = new ExtraLore(List.of());

    /** One plugin's lines and where they go. */
    public record Entry(String owner, ExtraLorePosition position, List<Component> lines) {
    }

    private final List<Entry> entries;

    private ExtraLore(List<Entry> entries) {
        this.entries = entries;
    }

    /** No lines at all. */
    public static ExtraLore none() {
        return NONE;
    }

    /** The lines stored on an item, owners in alphabetical order; nothing for an item without any. */
    public static ExtraLore read(PersistentDataContainerView data) {
        PersistentDataContainer stored = data.get(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER);
        if (stored == null) {
            return NONE;
        }
        List<Entry> entries = new ArrayList<>();
        for (String owner : owners(stored)) {
            entry(stored, owner).ifPresent(entries::add);
        }
        entries.sort(Comparator.comparing(Entry::owner));
        return new ExtraLore(entries);
    }

    private static TreeSet<String> owners(PersistentDataContainerView stored) {
        TreeSet<String> owners = new TreeSet<>();
        stored.getKeys().forEach(key -> owners.add(key.getNamespace()));
        return owners;
    }

    private static Optional<Entry> entry(PersistentDataContainerView stored, String owner) {
        List<String> lines = stored.get(new NamespacedKey(owner, LINES), PersistentDataType.LIST.strings());
        Optional<ExtraLorePosition> position = parsePosition(stored.get(new NamespacedKey(owner, POSITION),
                PersistentDataType.STRING));
        if (lines == null || lines.isEmpty() || position.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Entry(owner, position.get(), lines.stream().map(JSON::deserialize).toList()));
    }

    /** Stores the owner's lines and position, replacing any it had; no lines removes them. */
    public static void write(PersistentDataContainer data, String owner, ExtraLorePosition position,
                             List<Component> lines) {
        if (lines.isEmpty()) {
            remove(data, owner);
            return;
        }
        PersistentDataContainer stored = data.has(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER)
                ? data.get(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER)
                : data.getAdapterContext().newPersistentDataContainer();
        stored.set(new NamespacedKey(owner, LINES), PersistentDataType.LIST.strings(),
                lines.stream().map(JSON::serialize).toList());
        stored.set(new NamespacedKey(owner, POSITION), PersistentDataType.STRING, formatPosition(position));
        data.set(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER, stored);
    }

    /**
     * Removes the owner's lines, and the whole container once no plugin has any.
     *
     * @return whether the owner had lines
     */
    public static boolean remove(PersistentDataContainer data, String owner) {
        PersistentDataContainer stored = data.get(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER);
        if (stored == null || !stored.has(new NamespacedKey(owner, LINES))) {
            return false;
        }
        stored.remove(new NamespacedKey(owner, LINES));
        stored.remove(new NamespacedKey(owner, POSITION));
        if (stored.isEmpty()) {
            data.remove(ItemKeys.EXTRA_LORE);
        } else {
            data.set(ItemKeys.EXTRA_LORE, PersistentDataType.TAG_CONTAINER, stored);
        }
        return true;
    }

    private static String formatPosition(ExtraLorePosition position) {
        if (position.isAfterTraits()) {
            return AFTER_TRAITS;
        }
        return (position.isAbove() ? ABOVE : BELOW) + position.section().name().toLowerCase(Locale.ROOT);
    }

    private static Optional<ExtraLorePosition> parsePosition(String text) {
        if (AFTER_TRAITS.equals(text)) {
            return Optional.of(ExtraLorePosition.afterTraits());
        }
        if (text == null || !(text.startsWith(ABOVE) || text.startsWith(BELOW))) {
            return Optional.empty();
        }
        String sectionName = text.substring(ABOVE.length()).toUpperCase(Locale.ROOT);
        try {
            LoreSection section = LoreSection.valueOf(sectionName);
            return Optional.of(text.startsWith(ABOVE) ? ExtraLorePosition.above(section) : ExtraLorePosition.below(section));
        } catch (IllegalArgumentException unknown) {
            return Optional.empty();
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /** Every plugin's lines, owners in alphabetical order. */
    public List<Entry> entries() {
        return entries;
    }

    /** The entry of this owner, if it has lines. */
    public Optional<Entry> of(String owner) {
        return entries.stream().filter(entry -> entry.owner().equals(owner)).findFirst();
    }

    /** The blocks of lines at this position, one per owner, owners in alphabetical order. */
    public List<List<Component>> at(ExtraLorePosition position) {
        return entries.stream().filter(entry -> entry.position().equals(position)).map(Entry::lines).toList();
    }
}
