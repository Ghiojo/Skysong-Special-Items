package org.skysongdev.skysongitems.timer;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads and writes the timers an item carries, and nothing else. They are stored under the
 * item's {@link ItemKeys#TIMERS} tag as a list, one entry per timer, holding:
 * <ul>
 *     <li>{@code clock}: the clock it counts in; left out for real time;</li>
 *     <li>{@code due}: when it runs, as a position of that clock (milliseconds since 1970, UTC, for real time);</li>
 *     <li>{@code action}: what it does, such as {@code add_trait};</li>
 *     <li>{@code target}, {@code value}: what the action works on, when it takes them;</li>
 *     <li>{@code span}: the time it was made with, such as {@code 24h}, when it has one;</li>
 *     <li>{@code every}: how often it repeats, such as {@code 1mo}, only for repeating timers.</li>
 * </ul>
 * Clock and action are stored by id and not checked here, so timers of a clock or action that
 * isn't registered survive being read and written back. Entries without a due time or action
 * aren't timers and are skipped.
 */
public final class ItemTimerStore {
    private static final NamespacedKey CLOCK = ItemKeys.named("clock");
    private static final NamespacedKey DUE = ItemKeys.named("due");
    private static final NamespacedKey ACTION = ItemKeys.named("action");
    private static final NamespacedKey TARGET = ItemKeys.named("target");
    private static final NamespacedKey VALUE = ItemKeys.named("value");
    private static final NamespacedKey SPAN = ItemKeys.named("span");
    private static final NamespacedKey EVERY = ItemKeys.named("every");

    private ItemTimerStore() {
    }

    /** Whether the item carries timers at all, without reading them. */
    public static boolean hasTimers(PersistentDataContainerView container) {
        return container.has(ItemKeys.TIMERS);
    }

    /** The timers stored on the item, in the order they were stored. */
    public static List<ItemTimer> read(PersistentDataContainerView container) {
        List<PersistentDataContainer> entries = container.get(ItemKeys.TIMERS, PersistentDataType.LIST.dataContainers());
        if (entries == null) {
            return List.of();
        }
        List<ItemTimer> timers = new ArrayList<>();
        for (PersistentDataContainer entry : entries) {
            readEntry(entry).ifPresent(timers::add);
        }
        return timers;
    }

    private static Optional<ItemTimer> readEntry(PersistentDataContainer entry) {
        Long due = entry.get(DUE, PersistentDataType.LONG);
        String action = entry.get(ACTION, PersistentDataType.STRING);
        if (due == null || action == null) {
            return Optional.empty();
        }
        String clock = entry.getOrDefault(CLOCK, PersistentDataType.STRING, RealTimeClock.ID);
        return Optional.of(new ItemTimer(clock, due, action, entry.get(TARGET, PersistentDataType.STRING),
                entry.get(VALUE, PersistentDataType.STRING), entry.get(SPAN, PersistentDataType.STRING),
                entry.get(EVERY, PersistentDataType.STRING)));
    }

    /** Stores these timers on the container in place of the ones it had; none removes the tag. */
    public static void write(PersistentDataContainer container, List<ItemTimer> timers) {
        if (timers.isEmpty()) {
            container.remove(ItemKeys.TIMERS);
            return;
        }
        PersistentDataAdapterContext context = container.getAdapterContext();
        List<PersistentDataContainer> entries = new ArrayList<>();
        for (ItemTimer timer : timers) {
            entries.add(writeEntry(context.newPersistentDataContainer(), timer));
        }
        container.set(ItemKeys.TIMERS, PersistentDataType.LIST.dataContainers(), entries);
    }

    /** Stores these timers on the item in place of the ones it had; none removes the tag. */
    public static void write(ItemStack item, List<ItemTimer> timers) {
        ItemMeta meta = item.getItemMeta();
        write(meta.getPersistentDataContainer(), timers);
        item.setItemMeta(meta);
    }

    private static PersistentDataContainer writeEntry(PersistentDataContainer entry, ItemTimer timer) {
        if (!RealTimeClock.ID.equals(timer.clock())) {
            entry.set(CLOCK, PersistentDataType.STRING, timer.clock());
        }
        entry.set(DUE, PersistentDataType.LONG, timer.due());
        entry.set(ACTION, PersistentDataType.STRING, timer.action());
        setIfPresent(entry, TARGET, timer.target());
        setIfPresent(entry, VALUE, timer.value());
        setIfPresent(entry, SPAN, timer.span());
        setIfPresent(entry, EVERY, timer.every());
        return entry;
    }

    private static void setIfPresent(PersistentDataContainer entry, NamespacedKey key, String value) {
        if (value != null) {
            entry.set(key, PersistentDataType.STRING, value);
        }
    }
}
