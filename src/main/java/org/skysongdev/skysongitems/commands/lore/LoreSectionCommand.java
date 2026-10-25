package org.skysongdev.skysongitems.commands.lore;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.TextParser;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * Edits one lore section of the item in the player's main hand. Registered
 * once per {@link LoreSection}, as /physdesc, /funcdesc and /skilldesc.
 * <p>
 * Subcommands:
 * <ul>
 *     <li>{@code set <text>} replaces the section.</li>
 *     <li>{@code add <text>} continues the section's last line;
 *     {@code add -n <text>} starts a new line.</li>
 *     <li>{@code del} / {@code delete} hides the section, or shows it again.</li>
 *     <li>{@code dl} / {@code delline <line>} deletes one line, counting from 1.</li>
 * </ul>
 */
public class LoreSectionCommand implements CommandExecutor {
    private static final String HOLD_ITEM = "<red>Please hold the item you wish to lore!";
    private static final String TAGGED_HOLD_ITEM = Constants.pluginTag + HOLD_ITEM;
    private static final String NOT_A_NUMBER = ChatColor.of("RED") + "[ERROR] Please type an actual number of line!";
    private static final String NEW_LINE_FLAG = "-n";

    // Modes of TextParser.manipulateSection.
    private static final int REPLACE = 0;
    private static final int CONCATENATE = 1;
    private static final int APPEND_BELOW = 2;

    private final LoreSection section;

    public LoreSectionCommand(LoreSection section) {
        this.section = section;
    }

    public LoreSection getSection() {
        return section;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        String subcommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        boolean missingText = subArgs.length == 0 && takesText(subcommand);
        if (missingText && section.showsUsageWhenTextIsMissing()) {
            return false;
        }
        if (!missingText && !runSubcommand(player, subcommand, subArgs)) {
            return false;
        }
        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));
        return true;
    }

    private static boolean takesText(String subcommand) {
        return "set".equals(subcommand) || "add".equals(subcommand);
    }

    /** Runs the subcommand, or returns false if there is no such subcommand. */
    private boolean runSubcommand(Player player, String subcommand, String[] subArgs) {
        switch (subcommand) {
            case "set" -> writeText(player, subArgs, REPLACE, setEmptyHandMessage());
            case "add" -> addText(player, subArgs);
            case "del", "delete" -> toggleHidden(player);
            case "delline", "dl" -> deleteLine(player, subArgs);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void addText(Player player, String[] words) {
        if (NEW_LINE_FLAG.equals(words[0])) {
            writeText(player, Arrays.copyOfRange(words, 1, words.length), APPEND_BELOW, TAGGED_HOLD_ITEM);
        } else {
            writeText(player, words, CONCATENATE, TAGGED_HOLD_ITEM);
        }
    }

    private String setEmptyHandMessage() {
        return section.tagsEmptyHandMessageOnSet() ? TAGGED_HOLD_ITEM : HOLD_ITEM;
    }

    private void writeText(Player player, String[] words, int mode, String emptyHandMessage) {
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = heldItem(player, emptyHandMessage);
        if (item == null) {
            return;
        }
        editSections(item, sections -> TextParser.manipulateSection(sections, words, section.getIndex(), mode));
        player.updateInventory();
    }

    private void toggleHidden(Player player) {
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = heldItem(player, HOLD_ITEM);
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        data.toggleSectionDeleted(section.getIndex());
        meta.lore(TextParser.TranslateToLore(data.getLoreSections(), data.getDeletedLines()));
        item.setItemMeta(meta);
    }

    /** Does nothing when no line is given, like the original commands. */
    private void deleteLine(Player player, String[] subArgs) {
        if (subArgs.length == 0) {
            return;
        }
        getPlugin().getUpdater().UpdateItem(player);
        final int line;
        try {
            line = Integer.parseInt(subArgs[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(NOT_A_NUMBER);
            return;
        }
        ItemStack item = heldItem(player, HOLD_ITEM);
        if (item == null) {
            return;
        }
        editSections(item, sections -> TextParser.deleteLine(section.getIndex(), line, sections));
    }

    /** Returns the item in the player's main hand, or null after sending the message if it is empty. */
    private static ItemStack heldItem(Player player, String emptyHandMessage) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(emptyHandMessage));
            return null;
        }
        return item;
    }

    /** Applies the edit to the item's stored lore sections and rebuilds its visible lore. */
    private static void editSections(ItemStack item, UnaryOperator<List<String>> edit) {
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        List<String> sections = edit.apply(data.getLoreSections());
        data.setLoreSections(sections);
        meta.lore(TextParser.TranslateToLore(sections, data.getDeletedLines()));
        item.setItemMeta(meta);
    }
}
