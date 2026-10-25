package org.skysongdev.skysongitems.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkysongItemTest {

    private static SkysongItem itemNamed(String name) {
        return new SkysongItem(name, new ArrayList<>(), "STONE", null, null, null, Map.of());
    }

    @Test
    void formattedNameIsLowercaseWithUnderscores() {
        assertEquals("ancient_sword", itemNamed("Ancient Sword").getFormattedName());
    }

    @Test
    void formattedNameStripsLegacyAndHexColourCodes() {
        assertEquals("ancient_sword", itemNamed("&6Ancient &#FF00AASword").getFormattedName());
    }

    @Test
    void formattedNameStripsMiniMessageTags() {
        assertEquals("ancient_sword", itemNamed("<gold>Ancient</gold> <bold>Sword").getFormattedName());
    }

    @Test
    void formattedNameDropsDigitsAndPunctuation() {
        assertEquals("kings_sword", itemNamed("King's Sword!").getFormattedName());
    }

    @Test
    void formattedNameDefaultsWhenTheItemHasNoName() {
        assertEquals("no-name", itemNamed(null).getFormattedName());
    }
}
