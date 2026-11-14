package org.skysongdev.skysongitems.transform;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.SkysongItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests reading transformations.yml and checking its rules against the saved items. */
class TransformationLoaderTest {
    private static final SkysongItem NIGHT_BLADE = savedItem("night_blade");
    private static final SkysongItem DAY_BLADE = savedItem("day_blade");
    private static final SkysongItem PLAIN_SWORD = new SkysongItem("Plain", new ArrayList<>(List.of("physical")),
            "IRON_SWORD", null, "", "", Map.of());

    private final TransformationLoader loader = new TransformationLoader(TransformationLoaderTest::findSavedItem,
            Logger.getAnonymousLogger());

    private static SkysongItem savedItem(String materialId) {
        Map<NamespacedKey, String> tags = Map.of(ItemKeys.MATERIAL_ID, materialId);
        return new SkysongItem(materialId, new ArrayList<>(List.of("physical")), "IRON_SWORD", null, "", "", tags);
    }

    private static Optional<SkysongItem> findSavedItem(String id) {
        return Optional.ofNullable(switch (id) {
            case "weapons/night_blade" -> NIGHT_BLADE;
            case "weapons/day_blade" -> DAY_BLADE;
            case "weapons/plain_sword" -> PLAIN_SWORD;
            default -> null;
        });
    }

    private static YamlConfiguration file(String... lines) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(String.join("\n", lines));
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        return yaml;
    }

    @Test
    void aRuleIsLoadedWithItsTargetPermissionAndMessage() {
        TransformationRules rules = loader.load(file(
                "transformations:",
                "  day_blade:",
                "    to: weapons/night_blade",
                "    permission: skysong.transform.blade",
                "    message: The blade darkens.",
                "  night_blade:",
                "    to: weapons/day_blade"));

        Transformation rule = rules.find("day_blade").orElseThrow();
        assertEquals("weapons/night_blade", rule.targetId());
        assertSame(NIGHT_BLADE, rule.target());
        assertEquals("night_blade", rule.targetMaterialId());
        assertEquals("skysong.transform.blade", rule.permission());
        assertEquals("The blade darkens.", rule.message());
        Transformation back = rules.find("night_blade").orElseThrow();
        assertNull(back.permission());
        assertNull(back.message());
        assertTrue(rules.problems().isEmpty());
    }

    @Test
    void aRuleWithoutATargetIsSkipped() {
        TransformationRules rules = loader.load(file("transformations:", "  day_blade:", "    permission: x"));

        assertTrue(rules.all().isEmpty());
        assertEquals(List.of("transformations.yml: skipped the transformation of day_blade: it has no 'to' saved item."),
                rules.problems());
    }

    @Test
    void aTargetThatIsNotSavedIsSkippedAndTheOtherRulesStillLoad() {
        TransformationRules rules = loader.load(file(
                "transformations:",
                "  day_blade:",
                "    to: weapons/missing",
                "  night_blade:",
                "    to: weapons/day_blade"));

        assertTrue(rules.find("day_blade").isEmpty());
        assertTrue(rules.find("night_blade").isPresent());
        assertEquals(List.of("transformations.yml: skipped the transformation of day_blade: weapons/missing is not a saved item."),
                rules.problems());
    }

    @Test
    void aTargetWithoutAMaterialIdIsSkipped() {
        TransformationRules rules = loader.load(file("transformations:", "  day_blade:", "    to: weapons/plain_sword"));

        assertTrue(rules.all().isEmpty());
        assertEquals(List.of("transformations.yml: skipped the transformation of day_blade: "
                + "the saved item weapons/plain_sword has no material_id tag."), rules.problems());
    }

    @Test
    void aTargetInTheSameFormIsSkipped() {
        TransformationRules rules = loader.load(file("transformations:", "  day_blade:", "    to: weapons/day_blade"));

        assertTrue(rules.all().isEmpty());
        assertEquals(List.of("transformations.yml: skipped the transformation of day_blade: "
                + "the saved item weapons/day_blade has the same material_id, so the item would never change form."),
                rules.problems());
    }

    @Test
    void aRuleListingPartsIsSkippedUntilPartsAreSupported() {
        TransformationRules rules = loader.load(file(
                "transformations:",
                "  day_blade:",
                "    to: weapons/night_blade",
                "    parts: [model]"));

        assertTrue(rules.all().isEmpty());
        assertEquals(List.of("transformations.yml: skipped the transformation of day_blade: 'parts' is not supported yet."),
                rules.problems());
    }

    @Test
    void aFileWithoutTransformationsLoadsNoRules() {
        TransformationRules rules = loader.load(file("other: 1"));

        assertTrue(rules.all().isEmpty());
        assertTrue(rules.problems().isEmpty());
        assertTrue(rules.find(null).isEmpty());
    }
}
