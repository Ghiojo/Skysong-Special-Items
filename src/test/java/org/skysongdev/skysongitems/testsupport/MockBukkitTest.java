package org.skysongdev.skysongitems.testsupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

/**
 * Base class for tests that need a Bukkit server, for example to create
 * {@link org.bukkit.inventory.ItemStack}s or read item metadata.
 *
 * <p>A fresh mock server is started before each test and torn down after it,
 * so tests never share server state. Tests that only exercise plain Java logic
 * should not extend this class.
 */
public abstract class MockBukkitTest {
    protected ServerMock server;

    @BeforeEach
    void startMockServer() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void stopMockServer() {
        MockBukkit.unmock();
    }
}
