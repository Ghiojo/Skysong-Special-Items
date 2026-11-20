package org.skysongdev.skysongitems.commands.lore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.skysongdev.skysongitems.api.ExtraLorePosition;
import org.skysongdev.skysongitems.api.LoreSection;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests {@code /si extralore} on items other plugins put lore lines on. */
class ExtraLoreCommandTest extends MockBukkitTest {
    private final ExtraLoreCommand extraLoreCommand = new ExtraLoreCommand();
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

    private void putLines(String pluginName, String line) {
        SkysongItemsApi.get().lore().setExtra(MockBukkit.createMockPlugin(pluginName), heldItem(),
                ExtraLorePosition.below(LoreSection.SKILL), List.of(Component.text(line)));
    }

    @Test
    void listShowsEachPluginsLinesAndWhereTheyGo() {
        putLines("SkysongNPCs", "Owner: Alex");

        run("list");

        assertEquals("[SkysongItems] skysongnpcs (below skill):", nextPlainMessage());
        assertEquals("  Owner: Alex", nextPlainMessage());
    }

    @Test
    void listOnAnItemWithoutLinesSaysSo() {
        run();

        assertEquals("[SkysongItems] No plugin has put lore lines on this item.", nextPlainMessage());
    }

    @Test
    void clearRemovesOnePluginsLinesAndRedrawsTheLore() {
        putLines("SkysongNPCs", "Owner: Alex");

        run("clear", "SkysongNPCs");

        assertEquals("[SkysongItems] Removed the lore lines from skysongnpcs.", nextPlainMessage());
        assertEquals(List.of("physical", "", "functional", "", "skill"), plainLore());
    }

    @Test
    void clearAllRemovesEveryPluginsLines() {
        putLines("SkysongNPCs", "Owner: Alex");
        putLines("SkysongPets", "Pet: Biscuit");

        run("clear", "all");

        assertEquals(List.of(), SkysongItemsApi.get().lore().extraOwners(heldItem()));
        assertEquals(List.of("physical", "", "functional", "", "skill"), plainLore());
    }

    @Test
    void clearingAPluginWithoutLinesSaysSo() {
        run("clear", "skysongnpcs");

        assertEquals("[SkysongItems] No lore lines from skysongnpcs on this item.", nextPlainMessage());
    }

    @Test
    void tabCompletionOffersTheActionsThenThePluginsOnTheItem() {
        putLines("SkysongNPCs", "Owner: Alex");

        assertEquals(List.of("clear"), tabComplete("c"));
        assertEquals(List.of("skysongnpcs", "all"), tabComplete("clear", ""));
    }

    private ItemStack heldItem() {
        return player.getInventory().getItemInMainHand();
    }

    private List<String> plainLore() {
        return heldItem().getItemMeta().lore().stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
    }

    private void run(String... args) {
        extraLoreCommand.onCommand(player, new StubCommand(), "si", args);
    }

    private List<String> tabComplete(String... args) {
        return extraLoreCommand.onTabComplete(player, new StubCommand(), "si", args);
    }

    private String nextPlainMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
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
