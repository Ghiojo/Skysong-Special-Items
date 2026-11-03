package org.skysongdev.skysongitems.testsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.skysongdev.skysongitems.api.event.ItemChargesChangeEvent;
import org.skysongdev.skysongitems.api.event.ItemTagChangeEvent;

import java.util.ArrayList;
import java.util.List;

/** Records the item change events SkysongItems fires, the way another plugin would listen to them. */
public final class RecordedItemEvents implements Listener {
    private final List<ItemChargesChangeEvent> chargeChanges = new ArrayList<>();
    private final List<ItemTagChangeEvent> tagChanges = new ArrayList<>();

    private RecordedItemEvents() {
    }

    /** Starts recording the events fired on this server from now on. */
    public static RecordedItemEvents listenOn(ServerMock server) {
        RecordedItemEvents recorded = new RecordedItemEvents();
        server.getPluginManager().registerEvents(recorded, MockBukkit.createMockPlugin("EventListener"));
        return recorded;
    }

    @EventHandler
    public void onChargesChange(ItemChargesChangeEvent event) {
        chargeChanges.add(event);
    }

    @EventHandler
    public void onTagChange(ItemTagChangeEvent event) {
        tagChanges.add(event);
    }

    /** The charges change events fired so far, oldest first. */
    public List<ItemChargesChangeEvent> chargeChanges() {
        return chargeChanges;
    }

    /** The tag change events fired so far, oldest first. */
    public List<ItemTagChangeEvent> tagChanges() {
        return tagChanges;
    }

    /** The only charges change event fired so far; fails when there were none or several. */
    public ItemChargesChangeEvent onlyChargeChange() {
        if (chargeChanges.size() != 1) {
            throw new AssertionError("Expected one charges change event, got " + chargeChanges.size());
        }
        return chargeChanges.getFirst();
    }

    /** The only tag change event fired so far; fails when there were none or several. */
    public ItemTagChangeEvent onlyTagChange() {
        if (tagChanges.size() != 1) {
            throw new AssertionError("Expected one tag change event, got " + tagChanges.size());
        }
        return tagChanges.getFirst();
    }
}
