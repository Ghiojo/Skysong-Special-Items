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
 * to be one the item can take.
 */
public final class LaterEdits {
    /** The message for a bare time on a command that only takes {@code after} or {@code every}. */
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
}
