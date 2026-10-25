package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.skysongdev.skysongitems.SkysongItems;
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

class ConsumptionHandlerTest extends MockBukkitTest {
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

    private static ItemStack chargedItem(String commands, int charges, int amount) {
        ItemStack item = itemWithConsumptionCommands(commands);
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges);
        ItemData.from(meta).setLoreSections(List.of("physical", "functional", "skill"));
        item.setItemMeta(meta);
        return item;
    }

    private static int chargesOf(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(ItemKeys.CHARGES, PersistentDataType.INTEGER);
    }

    private List<ItemStack> consumeWithinTimeout(ItemStack item) {
        return assertTimeoutPreemptively(TIMEOUT, () -> ConsumptionHandler.consume(item, logger));
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
        assertNull(ConsumptionHandler.consume(null, logger));
    }

    @Test
    void chargeCommandGivesBackTheItemWithFewerCharges() {
        ItemStack item = chargedItem("charge:1:stop", 3, 1);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertEquals(1, returned.size());
        assertEquals(2, chargesOf(returned.getFirst()));
        assertEquals(1, returned.getFirst().getAmount());
    }

    @Test
    void chargeCommandOnAStackKeepsTheRestOfTheStackUntouched() {
        ItemStack item = chargedItem("charge:1:stop", 3, 4);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertEquals(2, returned.size());
        assertEquals(3, returned.get(0).getAmount());
        assertEquals(3, chargesOf(returned.get(0)));
        assertEquals(1, returned.get(1).getAmount());
        assertEquals(2, chargesOf(returned.get(1)));
    }

    @Test
    void lastChargeUsesTheItemUpAndRunsTheNextCommand() {
        ItemStack item = chargedItem("charge:1:stop;charge:5:stop", 1, 1);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertTrue(returned.isEmpty());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void invalidChargeAmountConsumesOneCharge() {
        ItemStack item = chargedItem("charge:lots:stop", 3, 1);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertEquals(2, chargesOf(returned.getFirst()));
        assertEquals(1, warnings.size());
    }

    @Test
    void continueRunsTheCommandsAfterACharge() {
        ItemStack item = chargedItem("charge:1:continue;charge:1:stop", 3, 1);

        List<ItemStack> returned = consumeWithinTimeout(item);

        // Each charge command works from the consumed item, so both report 2 charges left.
        assertEquals(2, returned.size());
        assertEquals(2, chargesOf(returned.get(0)));
        assertEquals(2, chargesOf(returned.get(1)));
    }

    @Test
    void chargeCommandWithoutSuffixStopsInsteadOfCrashing() {
        ItemStack item = chargedItem("charge:1;charge:1:stop", 3, 1);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertEquals(1, returned.size());
        assertEquals(2, chargesOf(returned.getFirst()));
        assertEquals(1, warnings.size());
    }

    @Test
    void chargeCommandWithoutAmountConsumesOneChargeInsteadOfCrashing() {
        ItemStack item = chargedItem("charge", 3, 1);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertEquals(1, returned.size());
        assertEquals(2, chargesOf(returned.getFirst()));
        assertEquals(2, warnings.size());
    }

    @Test
    void lastChargeWithoutSuffixRunsTheNextCommand() {
        ItemStack item = chargedItem("charge:1;charge:5:stop", 1, 1);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertTrue(returned.isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void itemCommandWithoutAMatchingFileOnlyUsesUpOneItem() {
        MockBukkit.load(SkysongItems.class);
        ItemStack item = itemWithConsumptionCommands("no_such_item");
        item.setAmount(2);

        List<ItemStack> returned = consumeWithinTimeout(item);

        assertEquals(1, returned.size());
        assertEquals(1, returned.getFirst().getAmount());
    }
}
