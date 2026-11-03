package org.skysongdev.skysongitems.api.event;

/** What changed an item, as carried by {@link ItemChargesChangeEvent} and {@link ItemTagChangeEvent}. */
public enum ItemChangeCause {
    /** A {@code /si} command, such as {@code /si charges set} or {@code /si tag set}. */
    COMMAND,
    /** The item was consumed and its {@code on_consumption} commands took charges off it. */
    CONSUMED,
    /** Another plugin wrote to the item through the SkysongItems API. */
    API
}
