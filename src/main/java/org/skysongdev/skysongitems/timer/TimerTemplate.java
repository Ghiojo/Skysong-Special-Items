package org.skysongdev.skysongitems.timer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A timer as a saved item keeps it: what it does and how long after the item is made, never
 * a date, since each item made from the saved item starts its own clock.
 *
 * @param clock   the id of the clock it counts in, such as {@code real}
 * @param action  the id of the action it applies
 * @param target  the tag or trait the action is about, or null
 * @param value   the value or amount the action takes, or null
 * @param span    how long after the item is made it runs, such as {@code 24h}
 * @param repeats whether it then runs again every {@code span}
 */
public record TimerTemplate(String clock, String action, @Nullable String target, @Nullable String value,
                            String span, boolean repeats) {
    public TimerTemplate {
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(span, "span");
    }

    /**
     * The template of a timer on an item: its span, or how often it repeats. Empty for a timer
     * made without a span, which no command makes.
     */
    public static Optional<TimerTemplate> of(ItemTimer timer) {
        String span = timer.repeats() ? timer.every() : timer.span();
        if (span == null) {
            return Optional.empty();
        }
        return Optional.of(new TimerTemplate(timer.clock(), timer.action(), timer.target(), timer.value(), span,
                timer.repeats()));
    }

    /** The templates of the timers on an item, in order, leaving out any without a span. */
    public static List<TimerTemplate> allOf(List<ItemTimer> timers) {
        return timers.stream().map(TimerTemplate::of).flatMap(Optional::stream).toList();
    }

    /**
     * The timer this template starts on a newly made item: due {@code span} after now, or
     * after the origin when it is on this template's clock. Empty when the clock isn't
     * registered or doesn't read the span, such as a calendar plugin that was removed.
     */
    public Optional<ItemTimer> start(TimerClocks clocks, @Nullable TimerOrigin origin) {
        return clocks.find(clock).flatMap(found -> found.parseStep(span).map(step -> {
            long from = origin != null && found.id().equalsIgnoreCase(origin.clock()) ? origin.position() : found.now();
            return new ItemTimer(found.id(), step.after(from), action, target, value, span, repeats ? span : null);
        }));
    }

    /** The timers these templates start on a newly made item, leaving out those that can't start. */
    public static List<ItemTimer> startAll(List<TimerTemplate> templates, TimerClocks clocks, @Nullable TimerOrigin origin) {
        List<ItemTimer> timers = new ArrayList<>();
        for (TimerTemplate template : templates) {
            template.start(clocks, origin).ifPresent(timers::add);
        }
        return timers;
    }
}
