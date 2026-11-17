package org.skysongdev.skysongitems.timer.action;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.api.event.ItemTransformEvent;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.TimerAction;
import org.skysongdev.skysongitems.timer.TimerClocks;
import org.skysongdev.skysongitems.timer.TimerContext;
import org.skysongdev.skysongitems.timer.TimerOrigin;
import org.skysongdev.skysongitems.transform.ItemTransformer;
import org.skysongdev.skysongitems.transform.TransformChain;
import org.skysongdev.skysongitems.transform.Transformation;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.Optional;

/**
 * Transforms the item into its next form, as {@code /transform} does, once for every time
 * the timer came due. A cycle of forms is walked by name first, so an item many cycles
 * behind is only rebuilt for the forms it really has to pass. Staff set the timer, so the
 * rule's permission isn't checked. With a player holding the item, {@link ItemTransformEvent}
 * fires for each step and a cancelled step ends the walk. The new form's timers count from when
 * the last of its transformations was due, not from when the item was finally checked.
 */
public record TransformTimerAction(ItemTransformer transformer, TimerClocks clocks) implements TimerAction {
    /** The id timers store for transforming. */
    public static final String ID = ItemTransformer.TIMER_ACTION;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ItemStack apply(TimerContext context, ItemStack item, ItemTimer timer, long times) {
        long steps = TransformChain.stepsNeeded(this::nextForm, ItemTransformer.materialIdOf(item), times);
        TimerOrigin origin = new TimerOrigin(timer.clock(), lastDue(timer, times));
        ItemStack current = item;
        for (long step = 0; step < steps; step++) {
            Optional<Transformation> rule = transformer.ruleFor(current);
            if (rule.isEmpty() || isCancelled(context.holder(), current, rule.get())) {
                break;
            }
            current = transformer.transformed(current, rule.get(), origin);
        }
        return current;
    }

    /**
     * When the last of the times the timer came due was, so the new form's timers count from
     * then and keep to the schedule the item would have had if it had been checked on time.
     */
    private long lastDue(ItemTimer timer, long times) {
        if (!timer.repeats()) {
            return timer.due();
        }
        return clocks.find(timer.clock())
                .flatMap(clock -> clock.parseStep(timer.every()))
                .map(step -> step.advance(timer.due(), times - 1))
                .orElse(timer.due());
    }

    private Optional<String> nextForm(String materialId) {
        return transformer.rule(materialId).map(Transformation::targetMaterialId);
    }

    private static boolean isCancelled(Player holder, ItemStack item, Transformation rule) {
        return holder != null
                && !new ItemTransformEvent(holder, item, rule.fromId(), rule.targetMaterialId(), rule.targetId()).callEvent();
    }

    /** Refuses an item whose form has no transformation. */
    @Override
    public ItemEditor.Result check(ItemEditor editor, ItemStack item, ItemTimer timer) {
        return transformer.ruleFor(item).isPresent() ? ItemEditor.Result.DONE : ItemEditor.Result.NO_TRANSFORMATION;
    }

    @Override
    public String describe(ItemTimer timer) {
        return "transform";
    }
}
