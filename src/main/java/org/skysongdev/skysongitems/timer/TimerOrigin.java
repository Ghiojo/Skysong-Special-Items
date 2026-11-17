package org.skysongdev.skysongitems.timer;

/**
 * Where a new item's timers count from when it isn't now: an item made by a timed
 * transformation that ran late starts its new form's timers from when the transformation was
 * due, so its schedule doesn't slip by however long nobody touched it.
 *
 * @param clock    the clock the position is on; timers on other clocks count from now
 * @param position the position on that clock to count from
 */
public record TimerOrigin(String clock, long position) {
}
