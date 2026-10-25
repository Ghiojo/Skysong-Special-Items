package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Items already in circulation store their data under these keys, so every key
 * must stay exactly equal to the one the plugin built before ItemKeys existed:
 * {@code new NamespacedKey(plugin, name)} for the plugin's own keys.
 */
class ItemKeysTest extends MockBukkitTest {
    private Plugin plugin;

    @BeforeEach
    void createPluginWithTheRealName() throws IOException {
        plugin = MockBukkit.createMockPlugin(pluginNameFromPluginYml());
    }

    /** Reads the name line of the plugin.yml that ships in the jar. */
    private static String pluginNameFromPluginYml() throws IOException {
        try (InputStream stream = ItemKeysTest.class.getResourceAsStream("/plugin.yml")) {
            String yml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return yml.lines()
                    .filter(line -> line.startsWith("name:"))
                    .map(line -> line.substring("name:".length()).trim())
                    .findFirst()
                    .orElseThrow();
        }
    }

    static Stream<Arguments> pluginKeys() {
        return Stream.of(
                Arguments.of(ItemKeys.MAX_CHARGES, "max_charges"),
                Arguments.of(ItemKeys.CHARGES, "charges"),
                Arguments.of(ItemKeys.CAN_ADD_CHARGES, "can_add_charges"),
                Arguments.of(ItemKeys.VERSION, "version"),
                Arguments.of(ItemKeys.LORE, "lore"),
                Arguments.of(ItemKeys.NAME, "name"),
                Arguments.of(ItemKeys.DELETED_LINES, "deleted_lines"),
                Arguments.of(ItemKeys.PROPERTIES, "properties"),
                Arguments.of(ItemKeys.PERMISSION, "permission"),
                Arguments.of(ItemKeys.SHIFT_RIGHT_CLICKABLE, "is_shift_rightclickable"),
                Arguments.of(ItemKeys.MATERIAL_ID, "material_id"),
                Arguments.of(ItemKeys.ON_CONSUMPTION, "on_consumption"),
                Arguments.of(ItemKeys.DYE_COLOR, "dye_color"),
                Arguments.of(ItemKeys.DISABLED, "is_disabled"),
                Arguments.of(ItemKeys.BOUND_BOOK, "bound_book"),
                Arguments.of(ItemKeys.RIGHT_CLICKABLE, "is_right_clickable"),
                Arguments.of(ItemKeys.DM_ONLY, "dm_only")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("pluginKeys")
    void pluginKeyMatchesTheKeyBuiltFromThePlugin(NamespacedKey key, String name) {
        assertEquals(new NamespacedKey(plugin, name), key);
    }

    @Test
    void namespaceMatchesThePluginName() {
        assertEquals(plugin.getName().toLowerCase(Locale.ROOT), ItemKeys.NAMESPACE);
    }

    @Test
    void namedBuildsAKeyInThePluginNamespace() {
        assertEquals(new NamespacedKey(plugin, "custom_tag"), ItemKeys.named("custom_tag"));
    }

    @Test
    void furnitureModelKeyBelongsToFurnitureLib() {
        assertEquals("furniturelib:model", ItemKeys.FURNITURE_MODEL.toString());
    }
}
