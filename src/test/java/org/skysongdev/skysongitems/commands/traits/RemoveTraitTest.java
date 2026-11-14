package org.skysongdev.skysongitems.commands.traits;

import net.kyori.adventure.text.Component;
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
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@code /si trait remove}. Uses the traits configured in the
 * bundled config.yml: "test" (base), "charges" (printable, "[Charges: $1/$2]")
 * and "spell" (rename, "[Spell]").
 */
class RemoveTraitTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of("physical", "functional", "skill");
    private static final List<String> LORE_WITHOUT_TRAITS = List.of("physical", "", "functional", "", "skill");

    private final RemoveTrait removeTrait = new RemoveTrait();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    /**
     * Puts in the player's hand an item at the current version, so the lore
     * updater leaves it alone, with its lore and name already rendered.
     * A null {@code traits} leaves the item without a traits entry.
     */
    private void holdItemWithTraits(String traits) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        container.set(ItemKeys.NAME, PersistentDataType.STRING, "Sword");
        container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 3);
        container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, 5);
        ItemData data = ItemData.from(meta);
        data.setLoreSections(SECTIONS);
        if (traits != null) {
            data.setTraitsText(traits);
        }
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(ItemLoreRenderer.refresh(item));
    }

    private ItemMeta heldMeta() {
        return player.getInventory().getItemInMainHand().getItemMeta();
    }

    private List<String> heldLore() {
        return heldMeta().lore().stream().map(RemoveTraitTest::plain).toList();
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private void remove(String traitId) {
        run(traitId);
    }

    private void run(String... args) {
        removeTrait.onCommand(player, new StubCommand(), "si", args);
    }

    private String nextMessage() {
        return plain(player.nextComponentMessage());
    }

    @Test
    void noTraitIdShowsTheRemoveUsage() {
        run();

        assertEquals("[SkysongItems] Usage: /si trait remove <trait>", nextMessage());
    }

    @Test
    void removingATraitTheItemDoesNotHaveSaysSo() {
        holdItemWithTraits("test");

        remove("charges");

        assertEquals("[SkysongItems] This item does not have this trait.", nextMessage());
    }

    @Test
    void removingFromAnItemWithoutTraitsSaysSo() {
        holdItemWithTraits(null);

        remove("test");

        assertEquals("[SkysongItems] This item has no traits.", nextMessage());
    }

    @Test
    void removingATraitConfirms() {
        holdItemWithTraits("test");

        remove("test");

        assertEquals("[SkysongItems] Trait removed successfully!", nextMessage());
    }

    @Test
    void removingTheLastTraitLeavesNoBlankLoreLine() {
        holdItemWithTraits("test");

        remove("test");

        assertEquals(LORE_WITHOUT_TRAITS, heldLore());
    }

    @Test
    void removingTheLastRenameTraitRemovesItsTag() {
        holdItemWithTraits("spell");

        remove("spell");

        assertEquals("Sword", plain(heldMeta().displayName()));
    }

    @Test
    void removingOneTraitKeepsTheOthersAndTheirLore() {
        holdItemWithTraits("charges;test");

        remove("test");

        assertEquals(List.of("charges"), ItemData.from(heldMeta()).getTraits());
        assertEquals(List.of("physical", "", "functional", "", "skill", "", "[Charges: 3/5]"), heldLore());
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
