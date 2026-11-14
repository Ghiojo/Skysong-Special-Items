package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraitTest extends MockBukkitTest {

    @Test
    void displayItemUsesTheConfiguredMaterial() {
        Trait trait = new Trait("sharp", "weapon", "DIAMOND_SWORD", "Sharp", "Cuts well");

        assertEquals(Material.DIAMOND_SWORD, trait.getItem().getType());
        assertEquals(1, trait.getItem().getAmount());
    }

    @Test
    void displayItemFallsBackToBarrierForAnUnknownMaterial() {
        Trait trait = new Trait("sharp", "weapon", "NOT_A_MATERIAL", "Sharp", "Cuts well");

        assertEquals(Material.BARRIER, trait.getItem().getType());
    }

    @Test
    void materialNameIsCaseSensitive() {
        Trait trait = new Trait("sharp", "weapon", "diamond_sword", "Sharp", "Cuts well");

        assertEquals(Material.BARRIER, trait.getItem().getType());
    }

    @Test
    void eachTraitKindReportsItsType() {
        ArrayList<String> valueTags = new ArrayList<>(List.of("charges"));

        assertEquals("base", new Trait("a", "t", "STONE", "n", "d").getType());
        assertEquals("rename", new RenameTrait("a", "t", "STONE", "n", "d", "tag").getType());
        assertEquals("printable", new PrintableTrait("a", "t", "STONE", "n", "d", "$1", valueTags, 3).getType());
    }

    @Test
    void printableTraitKeepsItsWeightAndValueTags() {
        ArrayList<String> valueTags = new ArrayList<>(List.of("charges", "max_charges"));
        PrintableTrait trait = new PrintableTrait("a", "t", "STONE", "n", "d", "$1/$2", valueTags, 3);

        assertEquals(3, trait.getWeight());
        assertEquals(List.of("charges", "max_charges"), trait.getValuetags());
    }

    @Test
    void renameTraitKeepsItsTag() {
        RenameTrait trait = new RenameTrait("a", "t", "STONE", "n", "d", "custom_name");

        assertEquals("custom_name", trait.getTag());
    }
}
