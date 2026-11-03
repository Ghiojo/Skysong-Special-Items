package org.skysongdev.skysongitems.commands.misc;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests {@code /si saveitem [folder]} and loading the saved item back by its folder path. */
class SaveItemFolderTest extends MockBukkitTest {
    private static final String ITEM_NAME = "Iron Sword";
    private static final String FILE_NAME = "iron_sword.yml";

    private final SaveItem saveItem = new SaveItem();
    private SkysongItems plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() throws IOException {
        plugin = MockBukkit.load(SkysongItems.class);
        deleteSavedItems();
        player = server.addPlayer();
        player.addAttachment(plugin, "skysongitems.save", true);
        player.addAttachment(plugin, "skysongitems.load", true);
        player.getInventory().setItemInMainHand(skysongItem());
    }

    /** Starts each test without templates another test may have left behind. */
    private static void deleteSavedItems() throws IOException {
        try (Stream<Path> paths = Files.walk(ItemTemplateRepository.generatedItemsFolder().toPath())) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    private static ItemStack skysongItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.NAME, PersistentDataType.STRING, ITEM_NAME);
        meta.getPersistentDataContainer().set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);
        item.setItemMeta(meta);
        return item;
    }

    private static File inGeneratedItems(String path) {
        return new File(ItemTemplateRepository.generatedItemsFolder(), path);
    }

    @Test
    void withoutAFolderTheItemIsSavedAtTheTopLevel() {
        save();

        assertTrue(inGeneratedItems(FILE_NAME).isFile());
        assertEquals("[SkysongItems] Item saved successfully as iron_sword!", nextPlainMessage());
        assertTrue(Constants.itemFileNames.contains("iron_sword"));
    }

    @Test
    void aFolderArgumentSavesIntoThatSubfolder() {
        save("weapons/swords");

        assertTrue(inGeneratedItems("weapons/swords/" + FILE_NAME).isFile());
        assertFalse(inGeneratedItems(FILE_NAME).exists());
        assertEquals("[SkysongItems] Item saved successfully as weapons/swords/iron_sword!", nextPlainMessage());
        assertTrue(Constants.itemFileNames.contains("weapons/swords/iron_sword"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "../../plugins", "/tmp", "weapons\\swords", "Weapons"})
    void invalidFoldersAreRefusedWithoutSaving(String folder) {
        save(folder);

        assertEquals("[SkysongItems] Folder names may only use a-z, 0-9, _ and -, with / between folders.", nextPlainMessage());
        assertFalse(new File(plugin.getDataFolder(), FILE_NAME).exists());
        assertFalse(new File(plugin.getDataFolder().getParentFile(), FILE_NAME).exists());
        assertFalse(inGeneratedItems(FILE_NAME).exists());
    }

    @Test
    void moreThanOneArgumentShowsTheUsage() {
        save("weapons", "swords");

        assertEquals("[SkysongItems] Usage: /si saveitem [folder]", nextPlainMessage());
        assertFalse(inGeneratedItems("weapons").exists());
    }

    @Test
    void loadItemFindsAnItemSavedInAFolder() {
        save("weapons");
        player.nextComponentMessage();
        player.getInventory().clear();

        new LoadItem().onCommand(player, new StubCommand(), "si", new String[]{"weapons/iron_sword"});

        assertEquals("[SkysongItems] Item loaded successfully!", nextPlainMessage());
        assertTrue(player.getInventory().contains(Material.IRON_SWORD));
    }

    @Test
    void loadItemTabCompletionOffersFolderPaths() {
        save("weapons");

        List<String> suggestions = new LoadItem().onTabComplete(player, new StubCommand(), "si", new String[]{"weap"});

        assertEquals(List.of("weapons/iron_sword"), suggestions);
    }

    @Test
    void tabCompletionOffersExistingFolders() {
        save("weapons/swords");
        save("armor");

        assertEquals(List.of("weapons", "weapons/swords"), tabComplete("w"));
        assertEquals(List.of("armor", "weapons", "weapons/swords"), tabComplete(""));
        assertEquals(List.of(), tabComplete("weapons", ""));
    }

    private void save(String... args) {
        saveItem.onCommand(player, new StubCommand(), "si", args);
    }

    private List<String> tabComplete(String... args) {
        return saveItem.onTabComplete(player, new StubCommand(), "si", args);
    }

    private String nextPlainMessage() {
        return PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage());
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
