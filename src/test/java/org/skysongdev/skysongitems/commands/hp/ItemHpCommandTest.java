package org.skysongdev.skysongitems.commands.hp;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.commands.timer.LaterEdits;
import org.skysongdev.skysongitems.commands.timer.TimingArgs;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests who may run each {@code /si hp} subcommand, and what it tells them. */
class ItemHpCommandTest extends MockBukkitTest {
    private PlayerMock player;
    private ItemHpCommand command;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
        command = new ItemHpCommand();
    }

    /** Holds an item at the current version with 6 of 10 HP. */
    private void holdDamagedItem() {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        ItemData data = ItemData.from(meta);
        data.setLoreSections(List.of("physical", "functional", "skill"));
        container.set(ItemKeys.ITEM_HP, PersistentDataType.INTEGER, 6);
        container.set(ItemKeys.MAX_ITEM_HP, PersistentDataType.INTEGER, 10);
        data.setTraitsText("item_hp");
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
    }

    private void makeStaff() {
        player.addAttachment(MockBukkit.createMockPlugin(), ItemHpCommand.STAFF_PERMISSION, true);
    }

    private void run(String... args) {
        command.onCommand(player, new StubCommand(), "si", args);
    }

    private String nextMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
    }

    private Integer heldHp() {
        return player.getInventory().getItemInMainHand().getPersistentDataContainer()
                .get(ItemKeys.ITEM_HP, PersistentDataType.INTEGER);
    }

    @Test
    void aPlayerCanReduceTheHp() {
        holdDamagedItem();

        run("reduce", "2");

        assertEquals(4, heldHp());
    }

    @Test
    void aPlayerCannotRestoreTheHp() {
        holdDamagedItem();

        run("add", "2");

        assertEquals("[SkysongItems] You don't have permissions to do that command!", nextMessage());
        assertEquals(6, heldHp());
    }

    @Test
    void reducingByANegativeAmountIsRefused() {
        holdDamagedItem();

        run("reduce", "-2");

        assertEquals("[SkysongItems] \"-2\" is out of range!", nextMessage());
        assertEquals(6, heldHp());
    }

    @Test
    void staffCanRestoreTheHp() {
        holdDamagedItem();
        makeStaff();

        run("add", "3");

        assertEquals(9, heldHp());
    }

    @Test
    void staffCanRemoveTheHp() {
        holdDamagedItem();
        makeStaff();

        run("remove");

        assertFalse(player.getInventory().getItemInMainHand().getPersistentDataContainer().has(ItemKeys.ITEM_HP));
    }

    @Test
    void reducingAnItemWithoutHpExplainsIt() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.STICK));

        run("reduce", "1");

        assertEquals("[SkysongItems] This item doesn't have HP!", nextMessage());
    }

    @Test
    void aMissingAmountIsExplained() {
        holdDamagedItem();

        run("reduce");

        assertEquals("[SkysongItems] Please type in a valid number!", nextMessage());
    }

    private List<ItemTimer> heldTimers() {
        return ItemTimerStore.read(player.getInventory().getItemInMainHand().getPersistentDataContainer());
    }

    @Test
    void staffCanReduceTheHpOverTimeWhichOnlyStoresATimer() {
        holdDamagedItem();
        makeStaff();

        run("reduce", "1", "every", "1d");

        assertEquals("[SkysongItems] Timer set: hp add -1 every 1d.", nextMessage());
        assertEquals(6, heldHp());
        assertEquals("add_hp", heldTimers().get(0).action());
        assertEquals("1d", heldTimers().get(0).every());
    }

    @Test
    void onlyStaffCanChangeTheHpOverTime() {
        holdDamagedItem();

        run("reduce", "1", "after", "1d");

        assertEquals("[SkysongItems] " + TimingArgs.STAFF_ONLY, nextMessage());
        assertEquals(6, heldHp());
        assertTrue(heldTimers().isEmpty());
    }

    @Test
    void staffCanReduceTheHpForATimeAndItComesBackLater() {
        holdDamagedItem();
        makeStaff();

        run("reduce", "2", "24h");

        assertEquals(4, heldHp());
        assertEquals("[SkysongItems] Undone after 24h: hp add 2.", nextMessage());
        assertEquals("add_hp", heldTimers().get(0).action());
    }

    @Test
    void settingTheHpForATimeIsRefused() {
        holdDamagedItem();
        makeStaff();

        run("set", "2", "24h");

        assertEquals("[SkysongItems] " + LaterEdits.LATER_ONLY, nextMessage());
        assertEquals(6, heldHp());
    }

    @Test
    void tabCompletionOnlyOffersWhatThePlayerMayRun() {
        assertEquals(List.of("reduce"), command.onTabComplete(player, new StubCommand(), "si", new String[]{""}));

        makeStaff();

        assertEquals(List.of("set", "setmax"), command.onTabComplete(player, new StubCommand(), "si", new String[]{"se"}));
        assertEquals(List.of("after"), command.onTabComplete(player, new StubCommand(), "si", new String[]{"remove", "a"}));
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
