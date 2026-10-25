package org.skysongdev.skysongitems.commands;

import org.bukkit.command.CommandExecutor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubcommandRegistryTest {
    private static final CommandExecutor NO_OP = (sender, command, label, args) -> true;

    private final SubcommandRegistry registry = new SubcommandRegistry();

    @Test
    void findsSubcommandByNameAndEveryAliasIgnoringCase() {
        Subcommand disable = Subcommand.builder("disable", NO_OP).alias("off").hiddenAlias("dis").build();
        registry.register(disable);

        assertSame(disable, registry.find("disable").orElseThrow());
        assertSame(disable, registry.find("OFF").orElseThrow());
        assertSame(disable, registry.find("Dis").orElseThrow());
    }

    @Test
    void unknownNameFindsNothing() {
        registry.register(Subcommand.builder("disable", NO_OP).build());

        assertTrue(registry.find("enable").isEmpty());
    }

    @Test
    void hiddenAliasesAreNotListed() {
        Subcommand disable = Subcommand.builder("disable", NO_OP).alias("off").hiddenAlias("dis").build();

        assertEquals(List.of("disable", "off"), disable.listedNames());
        assertEquals(List.of("disable", "off", "dis"), disable.names());
    }

    @Test
    void nameTakenByAnotherSubcommandIsRejected() {
        registry.register(Subcommand.builder("disable", NO_OP).hiddenAlias("dis").build());
        Subcommand clash = Subcommand.builder("discard", NO_OP).alias("DIS").build();

        assertThrows(IllegalArgumentException.class, () -> registry.register(clash));
        assertTrue(registry.find("discard").isEmpty(), "a rejected subcommand must not be half registered");
    }

    @Test
    void allKeepsRegistrationOrder() {
        Subcommand first = Subcommand.builder("first", NO_OP).build();
        Subcommand second = Subcommand.builder("second", NO_OP).build();
        registry.register(first);
        registry.register(second);

        assertEquals(List.of(first, second), registry.all());
    }
}
