package org.skysongdev.skysongitems.commands.lore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.TextParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterisation tests for /physdesc (/pd), /funcdesc (/fd) and /skilldesc (/sd).
 * Every command is run the way a player runs it, through the server's command
 * map, so these tests cover plugin.yml's names, aliases and permissions too.
 */
class LoreSectionCommandsTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of(
            "<gray>Physical one\n<gray>Physical two",
            "<#424242>Functional one\n<#424242>Functional two",
            "<#81a871>Skill one\n<#81a871>Skill two");
    private static final String HOLD_ITEM = "Please hold the item you wish to lore!";
    private static final String NOT_A_NUMBER = "[ERROR] Please type an actual number of line!";
    private static final String PLUGIN_TAG = "[SkysongItems] ";
    private static final int REPLACE = 0;
    private static final int CONCATENATE = 1;
    private static final int APPEND_BELOW = 2;

    private PlayerMock player;

    /** Command name, alias, section index, and the section that "dl" deletes from. */
    static Stream<Arguments> commands() {
        return Stream.of(
                Arguments.of("physdesc", "pd", 0, 0),
                Arguments.of("funcdesc", "fd", 1, 1),
                // Known bug, kept as is: /skilldesc dl deletes from the functional section.
                Arguments.of("skilldesc", "sd", 2, 1));
    }

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
        player.setOp(true);
        player.getInventory().setItemInMainHand(loredItem());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void setReplacesOnlyItsSection(String name, String alias, int index) {
        player.performCommand(name + " set A sharp blade");

        assertEquals(edited(index, REPLACE, "A", "sharp", "blade"), storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void aliasRunsTheSameCommand(String name, String alias, int index) {
        player.performCommand(alias + " set A sharp blade");

        assertEquals(edited(index, REPLACE, "A", "sharp", "blade"), storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void addContinuesTheLastLine(String name, String alias, int index) {
        player.performCommand(name + " add more");

        assertEquals(edited(index, CONCATENATE, "more"), storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void addWithNewLineFlagStartsANewLine(String name, String alias, int index) {
        player.performCommand(name + " add -n more");

        assertEquals(edited(index, APPEND_BELOW, "more"), storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void delHidesTheSectionAndDelAgainShowsIt(String name, String alias, int index) {
        player.performCommand(name + " del");
        String hiddenMask = deletedLines();
        player.performCommand(name + " delete");

        StringBuilder expectedMask = new StringBuilder("000");
        expectedMask.setCharAt(index, '1');
        assertEquals(expectedMask.toString(), hiddenMask);
        assertEquals("000", deletedLines());
        assertEquals(SECTIONS, storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void deleteLineRemovesTheGivenLine(String name, String alias, int index, int deleteLineIndex) {
        player.performCommand(name + " dl 1");

        List<String> expected = TextParser.deleteLine(deleteLineIndex, 1, new ArrayList<>(SECTIONS));
        assertEquals(expected, storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void deleteLineLongFormRemovesTheGivenLine(String name, String alias, int index, int deleteLineIndex) {
        player.performCommand(name + " delline 2");

        List<String> expected = TextParser.deleteLine(deleteLineIndex, 2, new ArrayList<>(SECTIONS));
        assertEquals(expected, storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void deleteLineRejectsANonNumber(String name, String alias, int index) {
        player.performCommand(name + " dl first");

        assertTrue(receivedMessages().contains(NOT_A_NUMBER));
        assertEquals(SECTIONS, storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void deleteLineWithoutANumberDoesNothing(String name, String alias, int index) {
        player.performCommand(name + " dl");

        assertTrue(receivedMessages().isEmpty());
        assertEquals(SECTIONS, storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void unknownSubcommandShowsTheUsage(String name, String alias, int index) {
        player.performCommand(name + " rename x");

        assertTrue(receivedMessages().contains("/" + name + " add [LORE]"));
        assertEquals(SECTIONS, storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void playerWithoutPermissionCannotEdit(String name, String alias, int index) {
        player.setOp(false);

        player.performCommand(name + " set A sharp blade");

        assertEquals(SECTIONS, storedSections());
    }

    @ParameterizedTest
    @MethodSource("commands")
    void delAsksForAnItemWhenTheHandIsEmpty(String name) {
        player.getInventory().setItemInMainHand(null);

        player.performCommand(name + " del");

        assertTrue(receivedMessages().contains(HOLD_ITEM));
    }

    @ParameterizedTest
    @MethodSource("commands")
    void deleteLineAsksForAnItemWhenTheHandIsEmpty(String name) {
        player.getInventory().setItemInMainHand(null);

        player.performCommand(name + " dl 1");

        assertTrue(receivedMessages().contains(HOLD_ITEM));
    }

    @ParameterizedTest
    @MethodSource("commands")
    void addAsksForAnItemWithThePluginTagWhenTheHandIsEmpty(String name) {
        player.getInventory().setItemInMainHand(null);

        player.performCommand(name + " add more");

        assertTrue(receivedMessages().contains(PLUGIN_TAG + HOLD_ITEM));
    }

    @Test
    void functionalAndSkillSetAskForAnItemWithThePluginTag() {
        player.getInventory().setItemInMainHand(null);

        player.performCommand("funcdesc set text");
        player.performCommand("skilldesc set text");

        assertEquals(2, receivedMessages().stream().filter((PLUGIN_TAG + HOLD_ITEM)::equals).count());
    }

    @Test
    void physicalSetAsksForAnItemWithoutThePluginTag() {
        player.getInventory().setItemInMainHand(null);

        player.performCommand("physdesc set text");

        assertTrue(receivedMessages().contains(HOLD_ITEM));
    }

    @Test
    void physicalAddAndSetWithoutTextShowTheUsage() {
        player.performCommand("physdesc add");
        player.performCommand("physdesc set");

        assertEquals(2, receivedMessages().stream().filter("/physdesc add [LORE]"::equals).count());
    }

    @Test
    void functionalAndSkillAddAndSetWithoutTextDoNothing() {
        player.performCommand("funcdesc add");
        player.performCommand("funcdesc set");
        player.performCommand("skilldesc add");
        player.performCommand("skilldesc set");

        assertTrue(receivedMessages().isEmpty());
        assertEquals(SECTIONS, storedSections());
    }

    private static ItemStack loredItem() {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        ItemData.from(meta).setLoreSections(SECTIONS);
        // Marks the item as current so the commands' item updater leaves it alone.
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> edited(int index, int mode, String... words) {
        return TextParser.manipulateSection(new ArrayList<>(SECTIONS), words, index, mode);
    }

    private List<String> storedSections() {
        return ItemData.from(player.getInventory().getItemInMainHand().getItemMeta()).getLoreSections();
    }

    private String deletedLines() {
        return ItemData.from(player.getInventory().getItemInMainHand().getItemMeta()).getDeletedLines();
    }

    /** Drains the player's chat as plain text, one entry per message. */
    private List<String> receivedMessages() {
        List<String> messages = new ArrayList<>();
        Component message;
        while ((message = player.nextComponentMessage()) != null) {
            messages.add(PlainTextComponentSerializer.plainText().serialize(message));
        }
        return messages;
    }
}
