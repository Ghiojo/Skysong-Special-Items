package org.skysongdev.skysongitems.commands.traits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraitsCommandTest extends MockBukkitTest {
    private final RecordingAddTrait addTrait = new RecordingAddTrait();
    private final RecordingRemoveTrait removeTrait = new RecordingRemoveTrait();
    private TraitsCommand traitsCommand;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        traitsCommand = new TraitsCommand(addTrait, removeTrait, new ViewTraits());
        player = server.addPlayer();
        player.addAttachment(MockBukkit.createMockPlugin(), TraitsCommand.EDIT_PERMISSION, true);
    }

    @Test
    void editPermissionIsDeclaredInPluginYml() throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(stream, "plugin.yml should be on the classpath");
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(reader);
                assertTrue(pluginYml.isConfigurationSection("permissions." + TraitsCommand.EDIT_PERMISSION),
                        TraitsCommand.EDIT_PERMISSION + " should be declared under permissions in plugin.yml");
            }
        }
    }

    @Test
    void addRunsForPlayerWithTheDeclaredPermission() {
        run("add", "glowing");

        assertEquals(List.of(List.of("glowing")), addTrait.calls);
    }

    @Test
    void removeRunsForPlayerWithTheDeclaredPermission() {
        run("remove", "glowing");

        assertEquals(List.of(List.of("glowing")), removeTrait.calls);
    }

    private void run(String... args) {
        traitsCommand.onCommand(player, new StubCommand(), "properties", args);
    }

    /** Records the arguments of every call instead of editing the held item. */
    private static final class RecordingAddTrait extends AddTrait {
        private final List<List<String>> calls = new ArrayList<>();

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            calls.add(List.of(args));
            return true;
        }
    }

    /** Records the arguments of every call instead of editing the held item. */
    private static final class RecordingRemoveTrait extends RemoveTrait {
        private final List<List<String>> calls = new ArrayList<>();

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            calls.add(List.of(args));
            return true;
        }
    }

    /** The sub-commands never look at the command object, so any instance will do. */
    private static final class StubCommand extends Command {
        private StubCommand() {
            super("properties");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            return true;
        }
    }
}
