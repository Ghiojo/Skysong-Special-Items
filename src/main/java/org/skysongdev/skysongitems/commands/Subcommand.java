package org.skysongdev.skysongitems.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One {@code /si} subcommand: the names it answers to, who may run it, and the
 * handler that does the work. {@link SubcommandRegistry} uses this description to
 * dispatch, so the handlers themselves only ever see the arguments after the
 * subcommand name.
 *
 * <p>Create one with {@link #builder(String, CommandExecutor)}.
 */
public final class Subcommand {
    private final String name;
    private final List<String> listedAliases;
    private final List<String> hiddenAliases;
    private final @Nullable String permission;
    private final boolean listedToEveryone;
    private final boolean playerOnly;
    private final CommandExecutor executor;
    private final @Nullable TabCompleter tabCompleter;

    private Subcommand(Builder builder) {
        this.name = builder.name;
        this.listedAliases = List.copyOf(builder.listedAliases);
        this.hiddenAliases = List.copyOf(builder.hiddenAliases);
        this.permission = builder.permission;
        this.listedToEveryone = builder.listedToEveryone;
        this.playerOnly = builder.playerOnly;
        this.executor = builder.executor;
        this.tabCompleter = builder.tabCompleter;
    }

    public static Builder builder(String name, CommandExecutor executor) {
        return new Builder(name, executor);
    }

    /** The name and every alias, in the order they were declared. */
    public List<String> names() {
        List<String> names = new ArrayList<>(listedNames());
        names.addAll(hiddenAliases);
        return Collections.unmodifiableList(names);
    }

    /** The names tab completion offers: the name and its listed aliases. */
    public List<String> listedNames() {
        List<String> names = new ArrayList<>();
        names.add(name);
        names.addAll(listedAliases);
        return Collections.unmodifiableList(names);
    }

    public boolean canRun(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }

    /** True when tab completion offers this subcommand to every sender, whatever their permissions. */
    public boolean isListedToEveryone() {
        return listedToEveryone;
    }

    /** True when the handler works on the sender's own player, so the console cannot run it. */
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public CommandExecutor executor() {
        return executor;
    }

    /** The completer for the arguments after the subcommand name, or null when there is none. */
    public @Nullable TabCompleter tabCompleter() {
        return tabCompleter;
    }

    public static final class Builder {
        private final String name;
        private final CommandExecutor executor;
        private final List<String> listedAliases = new ArrayList<>();
        private final List<String> hiddenAliases = new ArrayList<>();
        private @Nullable String permission;
        private boolean listedToEveryone;
        private boolean playerOnly;
        private @Nullable TabCompleter tabCompleter;

        private Builder(String name, CommandExecutor executor) {
            this.name = Objects.requireNonNull(name);
            this.executor = Objects.requireNonNull(executor);
        }

        /** Another name for the subcommand, offered by tab completion. */
        public Builder alias(String alias) {
            listedAliases.add(alias);
            return this;
        }

        /** Another name for the subcommand that tab completion does not offer. */
        public Builder hiddenAlias(String alias) {
            hiddenAliases.add(alias);
            return this;
        }

        /** The permission a sender needs to run the subcommand. Without one, anyone can run it. */
        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        /** Offers the subcommand in tab completion even to senders without the item edit permission. */
        public Builder listedToEveryone() {
            this.listedToEveryone = true;
            return this;
        }

        /** Refuses the subcommand to senders that are not players, such as the console. */
        public Builder playerOnly() {
            this.playerOnly = true;
            return this;
        }

        public Builder tabCompleter(TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
            return this;
        }

        public Subcommand build() {
            return new Subcommand(this);
        }
    }
}
