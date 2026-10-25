package org.skysongdev.skysongitems.commands.charges;

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
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests {@code /si charges remove}. It used to leave the "[Charges: x/y]" lore
 * line behind when charges was the item's last property, because the lore
 * refresh after an edit leaves items without properties alone. Uses the
 * properties configured in the bundled config.yml: "test" (base) and
 * "charges" (printable, "[Charges: $1/$2]").
 */
class RemoveChargesTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of("physical", "functional", "skill");
    private static final List<String> LORE_WITHOUT_PROPERTIES = List.of("physical", "", "functional", "", "skill");

    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    /**
     * Puts in the player's hand an item at the current version, so the lore
     * updater leaves it alone, with 3 of 5 charges and its lore already rendered.
     */
    private void holdChargedItemWithProperties(String properties) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, 3);
        container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, 5);
        ItemData data = ItemData.from(meta);
        data.setLoreSections(SECTIONS);
        data.setPropertiesText(properties);
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(ItemLoreRenderer.refresh(item));
    }

    private ItemMeta heldMeta() {
        return player.getInventory().getItemInMainHand().getItemMeta();
    }

    private List<String> heldLore() {
        return heldMeta().lore().stream().map(RemoveChargesTest::plain).toList();
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private void removeCharges() {
        new RemoveCharges().onCommand(player, new StubCommand(), "si", new String[0]);
    }

    @Test
    void removingChargesWhenItIsTheLastPropertyRemovesItsLoreLine() {
        holdChargedItemWithProperties("charges");

        removeCharges();

        assertEquals(LORE_WITHOUT_PROPERTIES, heldLore());
    }

    @Test
    void removingChargesWhenItIsTheLastPropertyLeavesNoPropertiesEntry() {
        holdChargedItemWithProperties("charges");

        removeCharges();

        assertFalse(heldMeta().getPersistentDataContainer().has(ItemKeys.PROPERTIES));
    }

    @Test
    void removingChargesRemovesTheChargeValues() {
        holdChargedItemWithProperties("charges");

        removeCharges();

        PersistentDataContainer container = heldMeta().getPersistentDataContainer();
        assertFalse(container.has(ItemKeys.CHARGES));
        assertFalse(container.has(ItemKeys.MAX_CHARGES));
    }

    @Test
    void removingChargesKeepsTheOtherPropertiesAndDropsItsLoreLine() {
        holdChargedItemWithProperties("charges;test");

        removeCharges();

        assertEquals(List.of("test"), ItemData.from(heldMeta()).getProperties());
        assertEquals(List.of("physical", "", "functional", "", "skill", ""), heldLore());
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
