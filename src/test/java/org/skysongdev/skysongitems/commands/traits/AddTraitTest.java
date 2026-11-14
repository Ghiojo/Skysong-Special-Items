package org.skysongdev.skysongitems.commands.traits;

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
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the messages of {@code /si trait add}, using the "test" base trait of the bundled config.yml. */
class AddTraitTest extends MockBukkitTest {
    private final AddTrait addTrait = new AddTrait();
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

    @Test
    void noTraitIdShowsTheAddUsage() {
        run();

        assertEquals("[SkysongItems] Usage: /si trait add <trait>", nextMessage());
    }

    @Test
    void addingATraitConfirms() {
        run("test");

        assertEquals("[SkysongItems] Trait added!", nextMessage());
    }

    @Test
    void addingATraitTwiceSaysTheItemHasIt() {
        run("test");
        nextMessage();

        run("test");

        assertEquals("[SkysongItems] This item already has this trait!", nextMessage());
    }

    @Test
    void unknownTraitIsRefusedWithTheValidTraits() {
        run("not_a_trait");

        assertEquals("[SkysongItems] Invalid trait!", nextMessage());
        assertTrue(nextMessage().startsWith("[SkysongItems] Valid traits: ["));
    }

    private void run(String... args) {
        addTrait.onCommand(player, new StubCommand(), "si", args);
    }

    private String nextMessage() {
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
