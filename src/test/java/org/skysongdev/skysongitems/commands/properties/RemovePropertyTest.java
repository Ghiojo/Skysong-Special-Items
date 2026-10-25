package org.skysongdev.skysongitems.commands.properties;

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
 * Tests {@code /properties remove}. Uses the properties configured in the
 * bundled config.yml: "test" (base), "charges" (printable, "[Charges: $1/$2]")
 * and "spell" (rename, "[Spell]").
 */
class RemovePropertyTest extends MockBukkitTest {
    private static final List<String> SECTIONS = List.of("physical", "functional", "skill");
    private static final List<String> LORE_WITHOUT_PROPERTIES = List.of("physical", "", "functional", "", "skill");

    private final RemoveProperty removeProperty = new RemoveProperty();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.load(SkysongItems.class);
        player = server.addPlayer();
    }

    /**
     * Puts in the player's hand an item at the current version, so the lore
     * updater leaves it alone, with its lore and name already rendered.
     */
    private void holdItemWithProperties(String properties) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        container.set(ItemKeys.NAME, PersistentDataType.STRING, "Sword");
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
        return heldMeta().lore().stream().map(RemovePropertyTest::plain).toList();
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private void remove(String propertyId) {
        removeProperty.onCommand(player, new StubCommand(), "properties", new String[]{propertyId});
    }

    @Test
    void removingTheLastPropertyLeavesNoPropertiesEntry() {
        holdItemWithProperties("test");

        remove("test");

        assertFalse(heldMeta().getPersistentDataContainer().has(ItemKeys.PROPERTIES));
    }

    @Test
    void removingTheLastPropertyLeavesNoBlankLoreLine() {
        holdItemWithProperties("test");

        remove("test");

        assertEquals(LORE_WITHOUT_PROPERTIES, heldLore());
    }

    @Test
    void removingTheLastPrintablePropertyRemovesItsLoreLine() {
        holdItemWithProperties("charges");

        remove("charges");

        assertEquals(LORE_WITHOUT_PROPERTIES, heldLore());
    }

    @Test
    void removingTheLastRenamePropertyRemovesItsTag() {
        holdItemWithProperties("spell");

        remove("spell");

        assertEquals("Sword", plain(heldMeta().displayName()));
    }

    @Test
    void removingOnePropertyKeepsTheOthersAndTheirLore() {
        holdItemWithProperties("charges;test");

        remove("test");

        assertEquals(List.of("charges"), ItemData.from(heldMeta()).getProperties());
        assertEquals(List.of("physical", "", "functional", "", "skill", "", "[Charges: 3/5]"), heldLore());
    }

    /** The command never looks at the command object, so any instance will do. */
    private static final class StubCommand extends Command {
        private StubCommand() {
            super("properties");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            return true;
        }
    }
}
