package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.utils.PropertyRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkysongItemsApiImplTest extends MockBukkitTest {

    @Test
    void getFailsWithAClearMessageBeforeSkysongItemsIsEnabled() {
        IllegalStateException error = assertThrows(IllegalStateException.class, SkysongItemsApi::get);

        assertTrue(error.getMessage().contains("softdepend: [Skysong_Items]"), error.getMessage());
    }

    @Test
    void enablingThePluginRegistersTheApiAsAService() {
        SkysongItems plugin = MockBukkit.load(SkysongItems.class);

        RegisteredServiceProvider<SkysongItemsApi> registration =
                server.getServicesManager().getRegistration(SkysongItemsApi.class);

        assertInstanceOf(SkysongItemsApiImpl.class, registration.getProvider());
        assertSame(plugin, registration.getPlugin());
        assertSame(registration.getProvider(), SkysongItemsApi.get());
    }

    @Test
    void apiVersionIsTheImplementedVersion() {
        MockBukkit.load(SkysongItems.class);

        String version = SkysongItemsApi.get().apiVersion();

        assertEquals(SkysongItemsApiImpl.API_VERSION, version);
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+"), version);
    }

    @Test
    void disablingThePluginUnregistersTheApi() {
        SkysongItems plugin = MockBukkit.load(SkysongItems.class);

        server.getPluginManager().disablePlugin(plugin);

        assertFalse(server.getServicesManager().isProvidedFor(SkysongItemsApi.class));
        assertThrows(IllegalStateException.class, SkysongItemsApi::get);
    }

    @Test
    void unregisterOnlyRemovesThisInstance() {
        SkysongItemsApiImpl first = new SkysongItemsApiImpl(new TagRegistry(), new PropertyRegistry());
        SkysongItemsApiImpl second = new SkysongItemsApiImpl(new TagRegistry(), new PropertyRegistry());
        first.register(server.getServicesManager(), MockBukkit.createMockPlugin("First"));
        second.register(server.getServicesManager(), MockBukkit.createMockPlugin("Second"));

        first.unregister(server.getServicesManager());

        assertSame(second, SkysongItemsApi.get());
    }
}
