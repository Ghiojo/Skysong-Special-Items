package org.skysongdev.skysongitems.registration;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.api.TraitDefinition;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Tests for the defaults and copying of {@link TraitDefinition}. */
class TraitDefinitionTest extends MockBukkitTest {

    @Test
    void unsetFieldsHaveDefaults() {
        TraitDefinition definition = TraitDefinition.base("bearer");

        assertEquals(TraitDefinition.Kind.BASE, definition.kind());
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
        TraitDefinition definition = TraitDefinition.printable("mana_bar", "Mana: $1", valueTags);

        valueTags.add("max_mana");

        assertEquals(List.of("mana"), definition.valueTags());
    }

    @Test
    void renameKeepsItsPrefixAsTheTemplate() {
        TraitDefinition definition = TraitDefinition.rename("cantrip", "[Cantrip]");

        assertEquals(TraitDefinition.Kind.RENAME, definition.kind());
        assertEquals("[Cantrip]", definition.template());
    }
}
