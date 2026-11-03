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
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@code /si tag set} and {@code /si tag remove} on the held item, using
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
    void setTagStoresTheValueAndConfirms() {
        run(new SetTag(), "arcrot", "4");

        assertEquals(4, heldArcrot());
        assertEquals("[SkysongItems] The tag arcrot set to 4 successfully!", nextMessage());
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
