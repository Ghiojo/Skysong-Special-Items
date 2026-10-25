package org.skysongdev.skysongitems.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoftDependencyTest extends MockBukkitTest {
    private Logger logger;
    private final List<LogRecord> warnings = new ArrayList<>();

    @BeforeEach
    void captureWarnings() {
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                if (logRecord.getLevel() == Level.WARNING) {
                    warnings.add(logRecord);
                }
            }

            @Override
            public void flush() {
                // Nothing buffered.
            }

            @Override
            public void close() {
                // Nothing to release.
            }
        });
    }

    @Test
    void installedPluginIsReportedWithoutWarning() {
        MockBukkit.createMockPlugin(SoftDependency.LUCK_PERMS);

        assertTrue(SoftDependency.isInstalled(server.getPluginManager(), SoftDependency.LUCK_PERMS, "permission items", logger));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void missingPluginIsReportedWithOneWarningNamingTheDisabledFeatures() {
        assertFalse(SoftDependency.isInstalled(server.getPluginManager(), SoftDependency.LUCK_PERMS, "permission items", logger));

        assertEquals(1, warnings.size());
        assertEquals("LuckPerms not found, permission items will be disabled.", warnings.get(0).getMessage());
    }
}
