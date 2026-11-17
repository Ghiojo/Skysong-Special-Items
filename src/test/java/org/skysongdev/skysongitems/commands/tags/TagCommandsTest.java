package org.skysongdev.skysongitems.commands.tags;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.api.event.ItemTagChangeEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedItemEvents;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@code /si tag set}, {@code remove}, {@code add} and {@code reduce} on the held item, using
 * the "arcrot" int tag from the bundled config.yml.
 */
class TagCommandsTest extends MockBukkitTest {
    private static final NamespacedKey ARCROT = ItemKeys.named("arcrot");

    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
    }

    private void run(CommandExecutor executor, String... args) {
        executor.onCommand(player, new StubCommand(), "si", args);
    }

    private String nextMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    private Integer heldArcrot() {
        return player.getInventory().getItemInMainHand().getItemMeta()
                .getPersistentDataContainer().get(ARCROT, PersistentDataType.INTEGER);
    }

    @Test
    void tagWithoutSubcommandShowsTheTagUsage() {
        run(new TagCommand());

        assertEquals("[SkysongItems] Usage: /si tag <sub-command>", nextMessage());
    }

    @Test
    void removeTagWithoutTagIdShowsTheRemoveUsage() {
        run(new RemoveTag());

        assertEquals("[SkysongItems] Usage: /si tag remove <tag> [after <time> | every <time>]", nextMessage());
    }

    @Test
    void setTagStoresTheValueAndConfirms() {
        run(new SetTag(), "arcrot", "4");

        assertEquals(4, heldArcrot());
        assertEquals("[SkysongItems] The tag arcrot set to 4 successfully!", nextMessage());
    }

    @Test
    void setTagForATimeSchedulesItsRemovalAndSettingItWithoutOneCancelsIt() {
        run(new SetTag(), "arcrot", "4", "24h");
        assertEquals("[SkysongItems] The tag arcrot set to 4 for 24h successfully!", nextMessage());
        List<ItemTimer> timers = ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer());
        assertEquals(1, timers.size());
        assertEquals("remove_tag", timers.get(0).action());

        run(new SetTag(), "arcrot", "5");

        assertFalse(ItemTimerStore.hasTimers(player.getInventory().getItemInMainHand().getPersistentDataContainer()));
    }

    @Test
    void setTagAfterATimeOnlyStoresATimer() {
        run(new SetTag(), "arcrot", "4", "after", "1d");

        assertNull(heldArcrot());
        assertEquals("[SkysongItems] Timer set: tag set arcrot 4 after 1d.", nextMessage());
        List<ItemTimer> timers = ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer());
        assertEquals("set_tag", timers.get(0).action());
        assertNull(timers.get(0).every());
    }

    @Test
    void reduceEveryStoresARepeatingNegativeAddAndAValueOfTheWrongKindIsRefused() {
        run(new AdjustTag(true), "arcrot", "1", "every", "1d");
        assertEquals("[SkysongItems] Timer set: tag add arcrot -1 every 1d.", nextMessage());

        run(new AdjustTag(false), "arcrot", "0.5", "every", "1d");
        assertEquals("[SkysongItems] Invalid amount for this tag! (Requires a number of the tag's kind)", nextMessage());
        List<ItemTimer> timers = ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer());
        assertEquals(1, timers.size());
        assertEquals("1d", timers.get(0).every());
    }

    @Test
    void addingForATimeUndoesTheSameAmountLaterAndEachAddUndoesItsOwn() {
        run(new AdjustTag(false), "arcrot", "2", "24h");
        run(new AdjustTag(false), "arcrot", "2", "24h");

        assertEquals(4, heldArcrot());
        assertEquals("[SkysongItems] The tag arcrot is now 2.", nextMessage());
        assertEquals("[SkysongItems] Undone after 24h: tag add arcrot -2.", nextMessage());
        List<ItemTimer> timers = ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer());
        assertEquals(List.of("-2", "-2"), timers.stream().map(ItemTimer::value).toList());
        assertNull(timers.get(0).every());
    }

    @Test
    void aBareTimeOnRemoveIsRefused() {
        run(new RemoveTag(), "arcrot", "1d");

        assertEquals("[SkysongItems] Use after <time> or every <time> with this command, such as after 1d.", nextMessage());
    }

    @Test
    void addAndReduceCountTheTagUpAndDownAndConfirm() {
        run(new TagCommand(), "add", "arcrot", "5");
        assertEquals(5, heldArcrot());
        assertEquals("[SkysongItems] The tag arcrot is now 5.", nextMessage());

        run(new TagCommand(), "reduce", "arcrot", "2");
        assertEquals(3, heldArcrot());
        assertEquals("[SkysongItems] The tag arcrot is now 3.", nextMessage());
    }

    @Test
    void addRefusesAnAmountThatIsNotANumberOfTheTagsKind() {
        run(new AdjustTag(false), "arcrot", "1.5");

        assertNull(heldArcrot());
        assertEquals("[SkysongItems] Invalid amount for this tag! (Requires a number of the tag's kind)", nextMessage());
    }

    @Test
    void setTagFiresATagEventNamingThePlayer() {
        RecordedItemEvents events = RecordedItemEvents.listenOn(server);

        run(new SetTag(), "arcrot", "4");

        ItemTagChangeEvent event = events.onlyTagChange();
        assertSame(player, event.getHolder());
        assertEquals(ItemChangeCause.COMMAND, event.getCause());
        assertEquals("arcrot", event.getTagId());
        assertEquals(4, event.getNewValue());
    }

    @Test
    void setTagExplainsAValueOfTheWrongType() {
        run(new SetTag(), "arcrot", "four");

        assertEquals("[SkysongItems] Invalid value for tag! (Requires a Number)", nextMessage());
    }

    @Test
    void setTagRejectsAnUnknownTag() {
        run(new SetTag(), "nope", "4");

        assertEquals("[SkysongItems] Invalid Tag!", nextMessage());
    }

    @Test
    void removeTagRemovesTheValueAndConfirms() {
        run(new SetTag(), "arcrot", "4");
        nextMessage();

        run(new RemoveTag(), "arcrot");

        assertFalse(player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(ARCROT));
        assertEquals("[SkysongItems] Tag removed successfully!", nextMessage());
    }

    @Test
    void removeTagReportsATagTheItemDoesNotHave() {
        run(new RemoveTag(), "arcrot");

        assertEquals("[SkysongItems] This item does not have this tag.", nextMessage());
    }

    /** The commands never look at the command object, so any instance will do. */
    private static final class StubCommand extends Command {
        private StubCommand() {
            super("si");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            return true;
        }
    }
}
