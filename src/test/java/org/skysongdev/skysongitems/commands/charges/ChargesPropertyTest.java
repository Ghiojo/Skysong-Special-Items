package org.skysongdev.skysongitems.commands.charges;

import org.bukkit.Material;
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
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that {@code /si charges set} and {@code /si charges max} give an item
 * the {@code charges} property only when it lacks that exact id. They used to
 * search the stored text, so any id containing "charges" counted as a match.
 */
class ChargesPropertyTest extends MockBukkitTest {
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    /** An item already at the current version, so the updater leaves it alone. */
    private void holdItemWithProperties(String properties) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        if (properties != null) {
            ItemData.from(meta).setPropertiesText(properties);
        }
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
    }

    private List<String> heldProperties() {
        return ItemData.from(player.getInventory().getItemInMainHand().getItemMeta()).getProperties();
    }

    private void run(CommandExecutor executor, String amount) {
        executor.onCommand(player, new StubCommand(), "si", new String[]{amount});
    }

    @Test
    void setChargesAddsTheChargesPropertyNextToAnIdThatContainsIt() {
        holdItemWithProperties("nocharges");

        run(new SetCharges(), "3");

        assertEquals(List.of("nocharges", "charges"), heldProperties());
    }

    @Test
    void setMaxChargesAddsTheChargesPropertyNextToAnIdThatContainsIt() {
        holdItemWithProperties("nocharges");

        run(new SetMaxCharges(), "3");

        assertEquals(List.of("nocharges", "charges"), heldProperties());
    }

    @Test
    void setChargesDoesNotDuplicateTheChargesProperty() {
        holdItemWithProperties("spell;charges");

        run(new SetCharges(), "3");

        assertEquals(List.of("spell", "charges"), heldProperties());
    }

    @Test
    void setMaxChargesDoesNotDuplicateTheChargesProperty() {
        holdItemWithProperties("spell;charges");

        run(new SetMaxCharges(), "3");

        assertEquals(List.of("spell", "charges"), heldProperties());
    }

    @Test
    void setChargesAddsTheChargesPropertyToAnItemWithNone() {
        holdItemWithProperties(null);

        run(new SetCharges(), "3");

        assertEquals(List.of("charges"), heldProperties());
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
