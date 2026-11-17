package org.skysongdev.skysongitems.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One timer stored on an item, as {@link ItemTimers#list} reads it.
 *
 * @param clock  the clock it counts in: {@code real} for real time, or the id of a clock
 *               another plugin registered
 * @param due    when it runs: for real time in milliseconds since 1970 (UTC), for other
 *               clocks a position of that clock
 * @param action what it does, such as {@code remove_trait} or {@code set_tag}
 * @param target the tag or trait the action is about, or null for actions without one
 * @param value  the value or amount the action takes, or null for actions without one
 * @param span   the time it was made with, such as {@code 24h}, or null
 * @param every  how often it repeats, such as {@code 1mo}, or null for a timer that runs once
 * @since 1.7.0
 */
public record ItemTimer(@NotNull String clock, long due, @NotNull String action, @Nullable String target,
                        @Nullable String value, @Nullable String span, @Nullable String every) {
}
