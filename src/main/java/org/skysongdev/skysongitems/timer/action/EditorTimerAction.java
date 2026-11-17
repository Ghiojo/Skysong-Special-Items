package org.skysongdev.skysongitems.timer.action;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.TimerAction;
import org.skysongdev.skysongitems.timer.TimerContext;
import org.skysongdev.skysongitems.utils.ItemEditor;

/**
 * A timer action that makes one edit through the {@link ItemEditor}, the same edit a
 * {@code /si} command makes, so it is validated, drawn into the lore and reported the same way.
 *
 * @param id          the id timers store
 * @param commandName the {@code /si} command it matches, such as {@code trait remove}, for describing it
 * @param edit        the edit, given the timer and how many times it came due
 */
public record EditorTimerAction(String id, String commandName, Edit edit) implements TimerAction {
    /** One edit of an item in place. */
    @FunctionalInterface
    public interface Edit {
        void apply(ItemEditor editor, ItemStack item, ItemTimer timer, long times);
    }

    @Override
    public ItemStack apply(TimerContext context, ItemStack item, ItemTimer timer, long times) {
        edit.apply(context.editor(), item, timer, times);
        return item;
    }

    @Override
    public String describe(ItemTimer timer) {
        StringBuilder text = new StringBuilder(commandName);
        if (timer.target() != null) {
            text.append(' ').append(timer.target());
        }
        if (timer.value() != null) {
            text.append(' ').append(timer.value());
        }
        return text.toString();
    }
}
