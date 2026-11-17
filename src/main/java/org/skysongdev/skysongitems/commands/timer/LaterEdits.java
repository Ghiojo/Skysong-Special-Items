package org.skysongdev.skysongitems.commands.timer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerScheduler;
import org.skysongdev.skysongitems.timer.TimerAction;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * What a command does with {@code after <time>} or {@code every <time>}: instead of making
 * its change, it stores the change on the held item as a timer, once the change is checked
 * to be one the item can take. With a time on its own, the command makes its change now and
 * {@link #undoLater undoes it later}.
 */
public final class LaterEdits {
    /** The message for a bare time on a command whose change can't be undone, so it only takes {@code after} or {@code every}. */
    public static final String LATER_ONLY = "Use after <time> or every <time> with this command, such as after 1d.";

    private LaterEdits() {
    }

    /**
     * Stores the change as a timer on the item, in place of an earlier timer making the same
     * change to the same tag or trait, and confirms it to the player.
     *
     * @return {@link ItemEditor.Result#DONE} when stored, otherwise why the change was refused,
     *         for the command to explain; nothing is stored then
     */
    public static ItemEditor.Result schedule(Player player, ItemStack item, TimingArgs timing, TimerAction action,
                                             @Nullable String target, @Nullable String value) {
        ItemTimer timer = ItemTimerScheduler.timer(timing.timing(), timing.repeats(), action.id(), target, value);
        ItemEditor.Result result = action.check(getPlugin().getItemEditor(), item, timer);
        if (ItemEditor.Result.DONE.equals(result)) {
            ItemTimerScheduler.replace(item, timer);
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag
                    + "<green>Timer set: " + action.describe(timer) + " " + timing.laterText() + "."));
        }
        return result;
    }

    /**
     * After a command made its change for a while, such as {@code /si hp add 5 24h}, stores
     * the change that undoes it once the time has passed, next to any earlier such timer, and
     * confirms it to the player.
     */
    public static void undoLater(Player player, ItemStack item, TimingArgs timing, TimerAction undo,
                                 @Nullable String target, @Nullable String value) {
        ItemTimer timer = ItemTimerScheduler.add(item,
                ItemTimerScheduler.timer(timing.timing(), false, undo.id(), target, value));
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag
                + "<green>Undone after " + timing.timing().text() + ": " + undo.describe(timer) + "."));
    }
}
