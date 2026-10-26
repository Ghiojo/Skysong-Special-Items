package org.skysongdev.skysongitems.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The subcommands of one command, such as {@code /si}, looked up by name or alias. Names are matched
 * without regard to case, and each name may belong to only one subcommand.
 */
public final class SubcommandRegistry {
    private final List<Subcommand> subcommands = new ArrayList<>();
    private final Map<String, Subcommand> byName = new HashMap<>();

    /**
     * @throws IllegalArgumentException if one of the subcommand's names is already taken
     */
    public void register(Subcommand subcommand) {
        for (String name : subcommand.names()) {
            String key = normalise(name);
            if (byName.containsKey(key)) {
                throw new IllegalArgumentException("Subcommand " + name + " is already registered");
            }
        }
        for (String name : subcommand.names()) {
            byName.put(normalise(name), subcommand);
        }
        subcommands.add(subcommand);
    }

    public Optional<Subcommand> find(String name) {
        return Optional.ofNullable(byName.get(normalise(name)));
    }

    /** Every registered subcommand, in registration order. */
    public List<Subcommand> all() {
        return Collections.unmodifiableList(subcommands);
    }

    private static String normalise(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
