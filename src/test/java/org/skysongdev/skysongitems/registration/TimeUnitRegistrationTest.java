package org.skysongdev.skysongitems.registration;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent;
import org.skysongdev.skysongitems.commands.timer.TimingArgs;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerRunner;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests other plugins registering units of time, such as a calendar's seasons, through
 * {@link SkysongItemsRegisterEvent}. The bundled config.yml defines the int tag arcrot.
 */
class TimeUnitRegistrationTest extends MockBukkitTest {
    private final AtomicLong seasons = new AtomicLong(5);
    private final List<Boolean> results = new ArrayList<>();
    private SkysongItems plugin;
    private Plugin calendar;

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        calendar = MockBukkit.createMockPlugin("SkysongCalendar");
    }

    private void onRegister(Plugin owner, Consumer<SkysongItemsRegisterEvent> registration) {
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void register(SkysongItemsRegisterEvent event) {
                registration.accept(event);
            }
        }, owner);
    }

    private void registerSeasons() {
        onRegister(calendar, event -> results.add(event.registerTimeUnit(calendar, "season", seasons::get)));
        server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
    }

    @Test
    void timersCountInARegisteredUnitAndCatchUpOnEveryUnitTheyMissed() {
        registerSeasons();
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemTimerStore.write(item, List.of(new ItemTimer("season", 3, "add_tag", "arcrot", "1", "1season", "1season")));

        new ItemTimerRunner(plugin.getTimerClocks(), plugin.getTimerActions(), plugin::getItemEditor).run(item, null);

        // Due at seasons 3, 4 and 5, and next at 6.
        assertEquals(3, item.getPersistentDataContainer().get(ItemKeys.named("arcrot"), PersistentDataType.INTEGER));
        assertEquals(6, ItemTimerStore.read(item.getPersistentDataContainer()).get(0).due());
        assertEquals(List.of(true), results);
    }

    @Test
    void commandsReadTheUnitOnlyWhileItsPluginHasItRegistered() {
        registerSeasons();
        assertEquals("season", TimingArgs.read(new String[]{"every", "2season"}, 0, plugin.getTimerClocks()).timing().clock().id());

        server.getPluginManager().callEvent(new PluginDisableEvent(calendar));

        assertTrue(TimingArgs.read(new String[]{"every", "2season"}, 0, plugin.getTimerClocks()).isBad());
        assertFalse(plugin.getTimerClocks().find("season").isPresent());
    }

    @Test
    void unitsThatReadAsRealTimeOrAreTakenOrMalformedAreRefused() {
        Plugin weather = MockBukkit.createMockPlugin("SkysongWeather");
        onRegister(calendar, event -> {
            results.add(event.registerTimeUnit(calendar, "season", seasons::get));
            results.add(event.registerTimeUnit(calendar, "mo", seasons::get));
            results.add(event.registerTimeUnit(calendar, "real", seasons::get));
            results.add(event.registerTimeUnit(calendar, "moon2", seasons::get));
        });
        onRegister(weather, event -> results.add(event.registerTimeUnit(weather, "season", seasons::get)));

        server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));

        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
        assertEquals("SkysongCalendar", plugin.getTimerClocks().ownerOf("season"));
    }
}
