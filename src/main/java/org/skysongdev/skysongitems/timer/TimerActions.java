package org.skysongdev.skysongitems.timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** The actions timers can apply, by the id they store. */
public final class TimerActions {
    private final Map<String, TimerAction> byId = new HashMap<>();

    /**
     * @throws IllegalArgumentException when another action has the same id
     */
    public void register(TimerAction action) {
        if (byId.putIfAbsent(action.id(), action) != null) {
            throw new IllegalArgumentException("A timer action with the id " + action.id() + " is already registered");
        }
    }

    /** The action with this id, or empty when none is registered. */
    public Optional<TimerAction> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }
}
