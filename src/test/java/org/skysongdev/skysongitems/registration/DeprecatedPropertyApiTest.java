package org.skysongdev.skysongitems.registration;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.PropertyDefinition;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.TraitDefinition;
import org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.PrintableTrait;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The API names from before properties were renamed to traits, kept deprecated so plugins
 * built against them keep working until the next major version: they must reach the same
 * objects and registrations as the trait names.
 */
@SuppressWarnings("removal")
class DeprecatedPropertyApiTest extends MockBukkitTest {
    private SkysongItems plugin;
    private Plugin crafting;

    @BeforeEach
    void setUp() {
        plugin = MockBukkit.load(SkysongItems.class);
        crafting = MockBukkit.createMockPlugin("SkysongCrafting");
    }

    @Test
    void propertiesIsTheTraitsView() {
        SkysongItemsApi api = SkysongItemsApi.get();

        assertSame(api.traits(), api.properties());
        assertSame(api.detached().traits(), api.detached().properties());
    }

    @Test
    void registerPropertyRegistersTheTrait() {
        List<Boolean> results = new ArrayList<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onRegister(SkysongItemsRegisterEvent event) {
                results.add(event.registerTag(crafting, "mana", TagType.INT));
                results.add(event.registerProperty(crafting,
                        PropertyDefinition.printable("mana_bar", "Mana: $1", List.of("mana"))
                                .name("Mana Bar").category("Magic").icon(Material.AMETHYST_SHARD).weight(2)));
            }
        }, crafting);

        server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));

        assertEquals(List.of(true, true), results);
        PrintableTrait trait = assertInstanceOf(PrintableTrait.class, plugin.getTraitRegistry().get("mana_bar"));
        assertEquals("Mana Bar", trait.getName());
        assertEquals("Magic", trait.getPropType());
        assertEquals(Material.AMETHYST_SHARD, trait.getItem().getType());
        assertEquals(2, trait.getWeight());
        assertEquals("SkysongCrafting", plugin.getTraitRegistry().ownerOf("mana_bar"));
    }

    @Test
    void aPropertyDefinitionReadsAsItsTraitDefinition() {
        PropertyDefinition property = PropertyDefinition.rename("cantrip", "[Cantrip]").description("A small spell.");
        TraitDefinition trait = property.toTrait();

        assertEquals(PropertyDefinition.Kind.RENAME, property.kind());
        assertEquals(TraitDefinition.Kind.RENAME, trait.kind());
        assertEquals("cantrip", trait.id());
        assertEquals("[Cantrip]", trait.template());
        assertEquals("A small spell.", trait.description());
        assertEquals(trait.description(), property.description());
    }
}
