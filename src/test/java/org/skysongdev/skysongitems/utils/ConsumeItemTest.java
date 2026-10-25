package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumeItemTest extends MockBukkitTest {
    /** Long enough for any real run, short enough that a regression fails instead of hanging the build. */
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private Logger logger;
    private final List<LogRecord> warnings = new ArrayList<>();

    @BeforeEach
    void captureWarnings() {
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                if (Level.WARNING.equals(logRecord.getLevel())) {
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

    private static ItemStack itemWithConsumptionCommands(String commands) {
        ItemStack item = new ItemStack(Material.APPLE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.ON_CONSUMPTION, PersistentDataType.STRING, commands);
        item.setItemMeta(meta);
        return item;
    }

    private List<ItemStack> consumeWithinTimeout(ItemStack item) {
        return assertTimeoutPreemptively(TIMEOUT, () -> Utils.consumeItem(item, logger));
    }

    @Test
    void chargeCommandWithoutChargesTagIsSkippedInsteadOfLoopingForever() {
        ItemStack item = itemWithConsumptionCommands("charge:1:stop");

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertTrue(returned.isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void skippedChargeCommandMovesOnToTheNextCommand() {
        ItemStack item = itemWithConsumptionCommands("charge:1:stop;charge:2:stop");

        consumeWithinTimeout(item);

        assertEquals(2, warnings.size());
    }

    @Test
    void itemWithoutConsumptionCommandsReturnsNothing() {
        ItemStack item = new ItemStack(Material.APPLE);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertTrue(returned.isEmpty());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void missingItemReturnsNull() {
        assertNull(Utils.consumeItem(null, logger));
    }
}
