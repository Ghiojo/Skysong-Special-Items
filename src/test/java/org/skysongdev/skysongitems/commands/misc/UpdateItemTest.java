package org.skysongdev.skysongitems.commands.misc;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@code /si update}. The bundled config.yml defines a base property
 * {@code test} named "Test", which the command used to add to any item whose
 * functional lore contained that word.
 */
class UpdateItemTest extends MockBukkitTest {
    private final UpdateItem updateItem = new UpdateItem();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    /** An item already at the current version, so the lore updater leaves its stored sections alone. */
    private static ItemStack itemWithFunctionalLore(String functional) {
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        meta.lore(List.of(Component.text("physical")));
        ItemData data = ItemData.from(meta);
        data.setLoreSections(List.of("physical", functional, "skill"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemData heldItemData() {
        return ItemData.from(player.getInventory().getItemInMainHand().getItemMeta());
    }

    private void run() {
        updateItem.onCommand(player, new StubCommand(), "si", new String[0]);
    }

    @Test
    void doesNotAddAPropertyNamedInTheFunctionalLore() {
        player.getInventory().setItemInMainHand(itemWithFunctionalLore("Test"));

        run();

        assertNull(heldItemData().getPropertiesText());
    }

    @Test
    void doesNotAddAPropertyWhoseNameIsPartOfAWord() {
        player.getInventory().setItemInMainHand(itemWithFunctionalLore("A testing ground"));

        run();

        assertNull(heldItemData().getPropertiesText());
    }

    @Test
    void keepsTheStoredLoreSections() {
        player.getInventory().setItemInMainHand(itemWithFunctionalLore("Test"));

        run();

        assertEquals(List.of("physical", "Test", "skill"), heldItemData().getLoreSections());
    }

    @Test
    void asksForAnItemWhenTheHandIsEmpty() {
        run();

        assertNotNull(player.nextComponentMessage());
        assertEquals(Material.AIR, player.getInventory().getItemInMainHand().getType());
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
