package org.skysongdev.skysongitems.timer;

import java.util.ArrayList;
import java.util.List;

/** Describes an item's timers as players and staff read them, one numbered line each. */
public final class TimerDescriptions {
    private final TimerClocks clocks;
    private final TimerActions actions;

    public TimerDescriptions(TimerClocks clocks, TimerActions actions) {
        this.clocks = clocks;
        this.actions = actions;
    }

    /**
     * One line per timer, numbered from 1 in the order stored, such as
     * {@code 1. trait remove honed in 23h59m}. A timer whose clock or action isn't
     * registered says it is waiting for it.
     */
    public List<String> describe(List<ItemTimer> timers) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < timers.size(); i++) {
            lines.add((i + 1) + ". " + describe(timers.get(i)));
        }
        return lines;
    }

    private String describe(ItemTimer timer) {
        String what = actions.find(timer.action()).map(action -> action.describe(timer))
                .orElse(timer.action() + " (waiting: unknown action)");
        return clocks.find(timer.clock())
                .map(clock -> what + wait(clock.describeWait(clock.now(), timer.due())) + repeat(timer))
                .orElse(what + " (waiting: unknown clock " + timer.clock() + ")");
    }

    private static String wait(String wait) {
        return wait.isEmpty() ? " now" : " in " + wait;
    }

    private static String repeat(ItemTimer timer) {
        return timer.repeats() ? ", then every " + timer.every() : "";
    }
}
