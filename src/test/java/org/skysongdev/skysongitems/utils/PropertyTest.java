package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyTest extends MockBukkitTest {

    @Test
    void displayItemUsesTheConfiguredMaterial() {
        Property property = new Property("sharp", "weapon", "DIAMOND_SWORD", "Sharp", "Cuts well");

        assertEquals(Material.DIAMOND_SWORD, property.getItem().getType());
        assertEquals(1, property.getItem().getAmount());
    }

    @Test
    void displayItemFallsBackToBarrierForAnUnknownMaterial() {
        Property property = new Property("sharp", "weapon", "NOT_A_MATERIAL", "Sharp", "Cuts well");

        assertEquals(Material.BARRIER, property.getItem().getType());
    }

    @Test
    void materialNameIsCaseSensitive() {
        Property property = new Property("sharp", "weapon", "diamond_sword", "Sharp", "Cuts well");

        assertEquals(Material.BARRIER, property.getItem().getType());
    }

    @Test
    void gettersReturnTheConstructorValues() {
        Property property = new Property("sharp", "weapon", "STONE", "Sharp", "Cuts well");

        assertEquals("sharp", property.getId());
        assertEquals("weapon", property.getPropType());
        assertEquals("Sharp", property.getName());
        assertEquals("Cuts well", property.getDescription());
    }

    @Test
    void eachPropertyKindReportsItsType() {
        ArrayList<String> valueTags = new ArrayList<>(List.of("charges"));

        assertEquals("base", new Property("a", "t", "STONE", "n", "d").getType());
        assertEquals("rename", new RenameProperty("a", "t", "STONE", "n", "d", "tag").getType());
        assertEquals("printable", new PrintableProperty("a", "t", "STONE", "n", "d", "$1", valueTags, 3).getType());
    }

    @Test
    void printablePropertyKeepsItsWeightAndValueTags() {
        ArrayList<String> valueTags = new ArrayList<>(List.of("charges", "max_charges"));
        PrintableProperty property = new PrintableProperty("a", "t", "STONE", "n", "d", "$1/$2", valueTags, 3);

        assertEquals(3, property.getWeight());
        assertEquals(List.of("charges", "max_charges"), property.getValuetags());
    }

    @Test
    void renamePropertyKeepsItsTag() {
        RenameProperty property = new RenameProperty("a", "t", "STONE", "n", "d", "custom_name");

        assertEquals("custom_name", property.getTag());
    }
}
