package org.skysongdev.skysongitems.commands.properties;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@code /si details}. The bundled config.yml defines the base property
 * {@code test} ("Test") and the printable property {@code charges} ("Charges").
 */
class DetailsCommandTest extends MockBukkitTest {
    private final DetailsCommand detailsCommand = new DetailsCommand();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    @Test
    void emptyHandIsRefused() {
        run();

        assertEquals("You must hold an item in your main hand to use this command.", player.nextMessage());
        assertNull(player.nextMessage());
    }

    @Test
    void itemWithoutPropertiesSaysSo() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.STONE));

        run();

        assertEquals("This item has no properties.", player.nextMessage());
        assertNull(player.nextMessage());
    }

    @Test
    void listsEachPropertyByNameSeparatedByCommas() {
        player.getInventory().setItemInMainHand(itemWithProperties("test", "charges"));

        run();

        assertEquals("Item properties:", nextPlainMessage());
        assertEquals("Test, Charges", nextPlainMessage());
    }

    @Test
    void hoverShowsTheTypeAndDescription() {
        player.getInventory().setItemInMainHand(itemWithProperties("test"));

        run();
        player.nextComponentMessage();

        assertEquals(List.of("Test\nbase property\n\nDescription"), hoverTexts(player.nextComponentMessage()));
    }

    @Test
    void propertyNoLongerInTheConfigIsLeftOut() {
        player.getInventory().setItemInMainHand(itemWithProperties("removed", "test"));

        run();

        assertEquals("Item properties:", nextPlainMessage());
        assertEquals("Test", nextPlainMessage());
    }

    @Test
    void itemWithOnlyUnknownPropertiesSaysItHasNone() {
        player.getInventory().setItemInMainHand(itemWithProperties("removed"));

        run();

        assertEquals("This item has no properties.", player.nextMessage());
    }

    private static ItemStack itemWithProperties(String... propertyIds) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        ItemData.from(meta).setProperties(List.of(propertyIds));
        item.setItemMeta(meta);
        return item;
    }

    private void run() {
        detailsCommand.onCommand(player, new StubCommand(), "si", new String[0]);
    }

    private String nextPlainMessage() {
        return plain(player.nextComponentMessage());
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /** The plain text of every hover in the message, in order. */
    private static List<String> hoverTexts(Component component) {
        List<String> texts = new ArrayList<>();
        HoverEvent<?> hover = component.hoverEvent();
        if (hover != null && hover.value() instanceof Component shown) {
            texts.add(plain(shown));
        }
        for (Component child : component.children()) {
            texts.addAll(hoverTexts(child));
        }
        return texts;
    }

    /** The command never looks at the command object, so any instance will do. */
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
