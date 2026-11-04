package org.skysongdev.skysongitems.registration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.PropertyDefinition;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.event.SkysongItemsReadyEvent;
import org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent;
import org.skysongdev.skysongitems.commands.misc.ReloadCommand;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.PrintableProperty;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.RenameProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for other plugins registering tags and properties through
 * {@link SkysongItemsRegisterEvent}. The bundled config.yml defines the int tags arcrot,
 * charged_arcrot and fictis, and the properties test, charges and spell.
 */
class PluginRegistrationTest extends MockBukkitTest {
    private SkysongItems plugin;
    private Plugin stats;
    private final List<Boolean> results = new ArrayList<>();

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        stats = MockBukkit.createMockPlugin("SkysongStats");
    }

    /** Runs {@code registration} for {@code owner} on every register event, recording what each call returns. */
    private void onRegister(Plugin owner, Consumer<SkysongItemsRegisterEvent> registration) {
        server.getPluginManager().registerEvents(new RegisterListener(registration), owner);
    }

    private void registerTag(Plugin owner, String tagId, TagType type) {
        onRegister(owner, event -> results.add(event.registerTag(owner, tagId, type)));
    }

    private void registerProperty(Plugin owner, PropertyDefinition definition) {
        onRegister(owner, event -> results.add(event.registerProperty(owner, definition)));
    }

    private void fireServerLoad() {
        server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
    }

    private NamespacedKey key(String tagId) {
        return ItemKeys.named(tagId);
    }

    @Test
    void serverLoadAddsAPluginsTagAndProperty() {
        registerTag(stats, "mana", TagType.INT);
        registerProperty(stats, PropertyDefinition.base("mana_bearer").name("Mana Bearer")
                .category("Magic").icon(Material.AMETHYST_SHARD).description("Holds mana."));

        fireServerLoad();

        assertEquals(List.of(true, true), results);
        assertEquals(PersistentDataType.INTEGER, plugin.getTagRegistry().getType(key("mana")));
        assertEquals("SkysongStats", plugin.getTagRegistry().ownerOf(key("mana")));
        Property property = plugin.getPropertyRegistry().get("mana_bearer");
        assertEquals("Mana Bearer", property.getName());
        assertEquals("Magic", property.getPropType());
        assertEquals(Material.AMETHYST_SHARD, property.getItem().getType());
        assertEquals("Holds mana.", property.getDescription());
        assertEquals("SkysongStats", plugin.getPropertyRegistry().ownerOf("mana_bearer"));
        assertTrue(plugin.getPropertyRegistry().getPropertyTypes().contains("Magic"));
    }

    @Test
    void theApiSeesRegisteredTagsAndProperties() {
        registerTag(stats, "mana", TagType.INT);
        registerProperty(stats, PropertyDefinition.base("bearer"));

        fireServerLoad();

        assertTrue(SkysongItemsApi.get().isTag("mana"));
        assertEquals(TagType.INT, SkysongItemsApi.get().getTagType("mana").orElseThrow());
        assertTrue(SkysongItemsApi.get().properties().exists("bearer"));
    }

    @Test
    void pluginEntriesSitNextToTheConfiguredOnes() {
        registerTag(stats, "mana", TagType.FLOAT);

        fireServerLoad();

        assertTrue(plugin.getTagRegistry().contains("arcrot"));
        assertTrue(plugin.getTagRegistry().contains(ItemKeys.CHARGES));
        assertTrue(plugin.getPropertyRegistry().contains("charges"));
        assertEquals(PersistentDataType.FLOAT, plugin.getTagRegistry().getType(key("mana")));
    }

    @Test
    void readyEventFiresOnceTheNewRegistriesAreInUse() {
        registerTag(stats, "mana", TagType.INT);
        List<Boolean> seenWhenReady = new ArrayList<>();
        server.getPluginManager().registerEvents(new ReadyListener(
                () -> seenWhenReady.add(plugin.getTagRegistry().contains("mana"))), stats);

        fireServerLoad();

        assertEquals(List.of(true), seenWhenReady);
    }

    @Test
    void builtInTagsAreRefused() {
        registerTag(stats, "charges", TagType.INT);
        registerTag(stats, "model", TagType.STRING);

        fireServerLoad();

        assertEquals(List.of(false, false), results);
        assertNull(plugin.getTagRegistry().ownerOf(ItemKeys.CHARGES));
    }

    @Test
    void keysThePluginStoresDataUnderAreRefused() {
        registerTag(stats, "properties", TagType.STRING);

        fireServerLoad();

        assertEquals(List.of(false), results);
        assertFalse(plugin.getTagRegistry().contains(ItemKeys.PROPERTIES));
    }

    @Test
    void aConfiguredTagWithTheSameTypeIsAcceptedAndStaysConfigured() {
        registerTag(stats, "arcrot", TagType.INT);

        fireServerLoad();

        assertEquals(List.of(true), results);
        assertNull(plugin.getTagRegistry().ownerOf(key("arcrot")));
    }

    @Test
    void aConfiguredTagWithAnotherTypeIsRefused() {
        registerTag(stats, "arcrot", TagType.STRING);

        fireServerLoad();

        assertEquals(List.of(false), results);
        assertEquals(PersistentDataType.INTEGER, plugin.getTagRegistry().getType(key("arcrot")));
    }

    @Test
    void aTagAnotherPluginRegisteredIsRefused() {
        Plugin other = MockBukkit.createMockPlugin("OtherPlugin");
        registerTag(stats, "mana", TagType.INT);
        registerTag(other, "mana", TagType.INT);

        fireServerLoad();

        assertEquals(List.of(true, false), results);
        assertEquals("SkysongStats", plugin.getTagRegistry().ownerOf(key("mana")));
    }

    @Test
    void idsBreakingTheKeyRulesAreRefused() {
        registerTag(stats, "Mana", TagType.INT);
        registerTag(stats, "mana points", TagType.INT);
        registerProperty(stats, PropertyDefinition.base("Bearer"));

        fireServerLoad();

        assertEquals(List.of(false, false, false), results);
    }

    @Test
    void aRefusalDoesNotStopTheOtherRegistrations() {
        registerTag(stats, "charges", TagType.INT);
        registerTag(stats, "mana", TagType.INT);

        fireServerLoad();

        assertEquals(List.of(false, true), results);
        assertTrue(plugin.getTagRegistry().contains("mana"));
    }

    @Test
    void printablePropertiesPrintTheirValueTags() {
        onRegister(stats, event -> {
            results.add(event.registerTag(stats, "mana", TagType.INT));
            results.add(event.registerProperty(stats,
                    PropertyDefinition.printable("mana_bar", "<blue>Mana: $1", List.of("mana")).weight(2)));
        });

        fireServerLoad();

        assertEquals(List.of(true, true), results);
        assertInstanceOf(PrintableProperty.class, plugin.getPropertyRegistry().get("mana_bar"));
    }

    @Test
    void printablePropertiesWithAnUnknownValueTagAreRefused() {
        registerProperty(stats, PropertyDefinition.printable("mana_bar", "Mana: $1", List.of("mana")));

        fireServerLoad();

        assertEquals(List.of(false), results);
        assertFalse(plugin.getPropertyRegistry().contains("mana_bar"));
    }

    @Test
    void renamePropertiesKeepTheirPrefix() {
        registerProperty(stats, PropertyDefinition.rename("cantrip", "<gray>[Cantrip]"));

        fireServerLoad();

        RenameProperty property = assertInstanceOf(RenameProperty.class,
                plugin.getPropertyRegistry().get("cantrip"));
        assertEquals("<gray>[Cantrip]", property.getTag());
    }

    @Test
    void aConfiguredPropertyIsKept() {
        registerProperty(stats, PropertyDefinition.base("charges"));

        fireServerLoad();

        assertEquals(List.of(true), results);
        assertInstanceOf(PrintableProperty.class, plugin.getPropertyRegistry().get("charges"));
        assertNull(plugin.getPropertyRegistry().ownerOf("charges"));
    }

    @Test
    void aPropertyAnotherPluginRegisteredIsRefused() {
        Plugin other = MockBukkit.createMockPlugin("OtherPlugin");
        registerProperty(stats, PropertyDefinition.base("bearer"));
        registerProperty(other, PropertyDefinition.base("bearer"));

        fireServerLoad();

        assertEquals(List.of(true, false), results);
        assertEquals("SkysongStats", plugin.getPropertyRegistry().ownerOf("bearer"));
    }

    @Test
    void disablingAPluginDropsOnlyItsEntries() {
        registerTag(stats, "mana", TagType.INT);
        registerProperty(stats, PropertyDefinition.base("bearer").category("Stats"));
        fireServerLoad();

        server.getPluginManager().callEvent(new PluginDisableEvent(stats));

        assertFalse(plugin.getTagRegistry().contains("mana"));
        assertFalse(plugin.getPropertyRegistry().contains("bearer"));
        assertFalse(plugin.getPropertyRegistry().getPropertyTypes().contains("Stats"));
        assertTrue(plugin.getTagRegistry().contains("arcrot"));
        assertTrue(plugin.getPropertyRegistry().contains("charges"));
    }

    @Test
    void everyLoadAsksThePluginsAgain() {
        RegisterListener listener = new RegisterListener(event -> event.registerTag(stats, "mana", TagType.INT));
        server.getPluginManager().registerEvents(listener, stats);
        fireServerLoad();

        HandlerList.unregisterAll(listener);
        plugin.reloadRegistries();

        assertFalse(plugin.getTagRegistry().contains("mana"));
    }

    @Test
    void siReloadKeepsPluginEntries() {
        registerTag(stats, "mana", TagType.INT);
        fireServerLoad();
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "skysongitems.reload", true);
        Command command = plugin.getCommand("skysongitems");

        new ReloadCommand().onCommand(player, command, "si", new String[0]);

        assertTrue(plugin.getTagRegistry().contains("mana"));
        assertEquals(List.of(true, true), results);
    }

    /** Hands each register event to a test's registration code. */
    private static final class RegisterListener implements Listener {
        private final Consumer<SkysongItemsRegisterEvent> registration;

        RegisterListener(Consumer<SkysongItemsRegisterEvent> registration) {
            this.registration = registration;
        }

        @EventHandler
        public void onRegister(SkysongItemsRegisterEvent event) {
            registration.accept(event);
        }
    }

    /** Runs a check on each ready event. */
    private static final class ReadyListener implements Listener {
        private final Runnable check;

        ReadyListener(Runnable check) {
            this.check = check;
        }

        @EventHandler
        public void onReady(SkysongItemsReadyEvent event) {
            check.run();
        }
    }
}
