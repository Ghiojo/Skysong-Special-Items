package org.skysongdev.skysongitems.registration;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.api.PropertyDefinition;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Tests for the defaults and copying of {@link PropertyDefinition}. */
class PropertyDefinitionTest extends MockBukkitTest {

    @Test
    void unsetFieldsHaveDefaults() {
        PropertyDefinition definition = PropertyDefinition.base("bearer");

        assertEquals(PropertyDefinition.Kind.BASE, definition.kind());
        assertEquals("bearer", definition.name());
        assertEquals("", definition.description());
        assertEquals("Other", definition.category());
        assertEquals(Material.PAPER, definition.icon());
        assertEquals(0, definition.weight());
        assertNull(definition.template());
        assertEquals(List.of(), definition.valueTags());
    }

    @Test
    void valueTagsAreCopied() {
        List<String> valueTags = new ArrayList<>(List.of("mana"));
        PropertyDefinition definition = PropertyDefinition.printable("mana_bar", "Mana: $1", valueTags);

        valueTags.add("max_mana");

        assertEquals(List.of("mana"), definition.valueTags());
    }

    @Test
    void renameKeepsItsPrefixAsTheTemplate() {
        PropertyDefinition definition = PropertyDefinition.rename("cantrip", "[Cantrip]");

        assertEquals(PropertyDefinition.Kind.RENAME, definition.kind());
        assertEquals("[Cantrip]", definition.template());
    }
}
