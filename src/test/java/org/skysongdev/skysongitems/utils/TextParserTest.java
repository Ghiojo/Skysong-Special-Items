package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Characterisation tests for {@link TextParser}. They record how the parser
 * behaves today, including its quirks, so later refactors can prove they did
 * not change it.
 */
class TextParserTest {
    /** The legacy colour code the plugin stores on items to separate the three lore blocks. */
    private static final String BLOCK_SEPARATOR = ChatColor.of("#190898").toString();

    private static List<String> unsetBlocks() {
        return new ArrayList<>(List.of("<gray>[Not Set]", "<#424242>[Not Set]", "<#81a871>[Not Set]"));
    }

    private static List<String> plainText(List<Component> lore) {
        return lore.stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
    }

    @Nested
    class TranslateColorCodes {
        @Test
        void convertsLegacyCodesToMiniMessageTags() {
            assertEquals("<gold>Gold <bold>Bold <white>White",
                    TextParser.translateColorCodes("&6Gold &lBold &fWhite"));
        }

        @Test
        void acceptsUppercaseCodes() {
            assertEquals("<green>Go", TextParser.translateColorCodes("&AGo"));
        }

        @Test
        void leavesUnsupportedCodesUntouched() {
            assertEquals("&rReset &zNope", TextParser.translateColorCodes("&rReset &zNope"));
        }
    }

    @Nested
    class TranslateHexColorCodes {
        @Test
        void convertsHexCodesToMiniMessageTags() {
            assertEquals("<#FF00AA>Pink", TextParser.translateHexColorCodes("&#", "", "&#FF00AAPink"));
        }

        @Test
        void shiftsTheReservedSeparatorColourSoUsersCannotForgeIt() {
            assertEquals("<#190899>x", TextParser.translateHexColorCodes("&#", "", "&#190898x"));
        }

        @Test
        void ignoresIncompleteHexCodes() {
            assertEquals("&#FF00x", TextParser.translateHexColorCodes("&#", "", "&#FF00x"));
        }
    }

    @Nested
    class CheckForColor {
        @Test
        void returnsNullWhenTheWordHasNoColour() {
            assertNull(TextParser.CheckForColor("plain"));
        }

        @Test
        void returnsTheLastColourInTheWord() {
            assertEquals("&c", TextParser.CheckForColor("&6a&cb"));
        }

        @Test
        void keepsAFormatCodeThatFollowsAHexColour() {
            assertEquals("&#123456&l", TextParser.CheckForColor("&#123456&lword"));
        }
    }

    @Nested
    class RevertColor {
        @Test
        void turnsSectionSignCodesIntoMiniMessageTags() {
            assertEquals("<gold>Gold", TextParser.revertColor("§6Gold"));
        }

        @Test
        void turnsSectionSignHexIntoAmpersandHex() {
            assertEquals("&#ff00aaPink", TextParser.revertColor("§x§f§f§0§0§a§aPink"));
        }
    }

    @Nested
    class StripColor {
        @Test
        void removesMiniMessageTags() {
            assertEquals("Hello world", TextParser.stripColor("<gray>Hello <bold>world"));
        }
    }

    @Nested
    class DeleteLine {
        @Test
        void removesTheGivenOneBasedLineFromTheBlock() {
            List<String> lore = new ArrayList<>(List.of("a\nb\nc", "x"));

            assertEquals(List.of("a\nc", "x"), TextParser.deleteLine(0, 2, lore));
        }

        @Test
        void ignoresLineNumbersOutsideTheBlock() {
            List<String> lore = new ArrayList<>(List.of("a\nb", "x"));

            assertEquals(List.of("a\nb", "x"), TextParser.deleteLine(0, 0, lore));
            assertEquals(List.of("a\nb", "x"), TextParser.deleteLine(0, 3, lore));
        }
    }

    @Nested
    class ManipulateSection {
        private static final int REPLACE = 0;
        private static final int CONCATENATE = 1;
        private static final int APPEND_BELOW = 2;

        @Test
        void replaceWritesTheWordsIntoTheBlock() {
            List<String> result = TextParser.manipulateSection(unsetBlocks(), new String[]{"A", "sharp", "blade"}, 0, REPLACE);

            assertEquals("<gray><gray>A sharp blade", result.get(0));
            assertEquals("<#424242>[Not Set]", result.get(1));
        }

        @Test
        void replaceWrapsLinesLongerThanTheCharacterLimit() {
            String[] words = new String[10];
            Arrays.fill(words, "aaaaa");

            List<String> result = TextParser.manipulateSection(unsetBlocks(), words, 0, REPLACE);

            assertEquals("<gray><gray>aaaaa aaaaa aaaaa aaaaa aaaaa aaaaa aaaaa\n<gray>aaaaa aaaaa aaaaa", result.get(0));
        }

        @Test
        void wrappedLinesStartWithTheBlockColourAndTheLastUserColour() {
            String[] words = {"&cred", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "next"};

            List<String> result = TextParser.manipulateSection(unsetBlocks(), words, 0, REPLACE);

            assertEquals("<gray><gray><red><red>red aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n<gray><red>next", result.get(0));
        }

        @Test
        void concatenateContinuesTheLastLine() {
            List<String> blocks = new ArrayList<>(List.of("<gray>Line one", "f", "s"));

            List<String> result = TextParser.manipulateSection(blocks, new String[]{"more"}, 0, CONCATENATE);

            assertEquals("<gray>Line one more", result.get(0));
        }

        @Test
        void appendBelowStartsANewLine() {
            List<String> blocks = new ArrayList<>(List.of("<gray>Line one", "f", "s"));

            List<String> result = TextParser.manipulateSection(blocks, new String[]{"two"}, 0, APPEND_BELOW);

            assertEquals("<gray>Line one\n<gray>two", result.get(0));
        }

        @Test
        void usesTheColourOfTheTargetBlock() {
            List<String> result = TextParser.manipulateSection(unsetBlocks(), new String[]{"skill"}, 2, REPLACE);

            assertEquals("<#81a871><#81a871>skill", result.get(2));
        }
    }

    @Nested
    class TranslateToLore {
        private final List<String> blocks = List.of("<gray>A\n<gray>B", "<#424242>F", "<#81a871>S");

        @Test
        void rendersEveryBlockWithABlankLineBetweenThem() {
            assertEquals(List.of("A", "B", "", "F", "", "S"), plainText(TextParser.TranslateToLore(blocks, "000")));
        }

        @Test
        void treatsAMissingDeletionMaskAsNothingDeleted() {
            assertEquals(plainText(TextParser.TranslateToLore(blocks, "000")), plainText(TextParser.TranslateToLore(blocks, null)));
            assertEquals(plainText(TextParser.TranslateToLore(blocks, "000")), plainText(TextParser.TranslateToLore(blocks, "")));
        }

        @Test
        void skipsBlocksMarkedAsDeleted() {
            assertEquals(List.of("A", "B", "", "S"), plainText(TextParser.TranslateToLore(blocks, "010")));
        }

        @Test
        void returnsNoLinesWhenEveryBlockIsDeleted() {
            assertEquals(List.of(), TextParser.TranslateToLore(blocks, "111"));
        }

        @Test
        void rendersLinesWithoutItalics() {
            Component firstLine = TextParser.TranslateToLore(blocks, "000").getFirst();

            assertEquals(TextDecoration.State.FALSE, firstLine.decoration(TextDecoration.ITALIC));
        }
    }

    @Nested
    class TranslateFromLore {
        @Test
        void returnsUnsetBlocksWhenThereIsNoLore() {
            assertEquals(unsetBlocks(), TextParser.TranslateFromLore(null, null));
        }

        @Test
        void returnsUnsetBlocksWhenEveryBlockIsDeleted() {
            List<String> lore = List.of("§7Hello", BLOCK_SEPARATOR, "§7Func", BLOCK_SEPARATOR, "§7Skill");

            assertEquals(unsetBlocks(), TextParser.TranslateFromLore(lore, "111"));
        }

        @Test
        void splitsStoredLoreIntoItsThreeBlocks() {
            List<String> lore = List.of(
                    "§7Hello", "§7World", BLOCK_SEPARATOR,
                    "§x§4§2§4§2§4§2Func", BLOCK_SEPARATOR,
                    "§x§8§1§a§8§7§1Skill");

            assertEquals(List.of("<gray>Hello\n<gray>World", "&#424242Func", "&#81a871Skill"),
                    TextParser.TranslateFromLore(lore, "000"));
        }

        @Test
        void leavesADeletedFirstBlockUnset() {
            List<String> lore = List.of("§7Func", BLOCK_SEPARATOR, "§7Skill");

            assertEquals(List.of("<gray>[Not Set]", "<gray>Func", "<gray>Skill"),
                    TextParser.TranslateFromLore(lore, "100"));
        }
    }
}
