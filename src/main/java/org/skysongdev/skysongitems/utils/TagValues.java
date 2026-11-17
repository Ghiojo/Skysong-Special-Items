package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Writes tag values of the four types a tag can have: int, float, bool and
 * string. Used by {@link ItemEditor}.
 */
final class TagValues {
    private TagValues() {
    }

    /**
     * Parses the value as the tag's type and writes it. Nothing is written when
     * the value doesn't parse, or when the type isn't one of the four.
     */
    static ItemEditor.Result write(PersistentDataContainer container, NamespacedKey key, PersistentDataType<?, ?> type, String value) {
        if (PersistentDataType.INTEGER.equals(type)) {
            return writeInt(container, key, value);
        }
        if (PersistentDataType.BOOLEAN.equals(type)) {
            return writeBoolean(container, key, value);
        }
        if (PersistentDataType.FLOAT.equals(type)) {
            return writeFloat(container, key, value);
        }
        if (PersistentDataType.STRING.equals(type)) {
            container.set(key, PersistentDataType.STRING, value);
        }
        return ItemEditor.Result.DONE;
    }

    /**
     * Adds the amount, times the given count, to a number tag; a negative amount takes it
     * off. A tag the item doesn't have counts as 0, and the result never goes below 0.
     * Nothing is written when the amount doesn't parse as the tag's type or the tag isn't
     * a number.
     */
    static ItemEditor.Result add(PersistentDataContainer container, NamespacedKey key, PersistentDataType<?, ?> type,
                                 String amount, long times) {
        if (PersistentDataType.INTEGER.equals(type)) {
            return addInt(container, key, amount, times);
        }
        if (PersistentDataType.FLOAT.equals(type)) {
            return addFloat(container, key, amount, times);
        }
        return ItemEditor.Result.NOT_NUMERIC;
    }

    private static ItemEditor.Result addInt(PersistentDataContainer container, NamespacedKey key, String amount, long times) {
        try {
            long current = container.getOrDefault(key, PersistentDataType.INTEGER, 0);
            long total = current + timesOver(Integer.parseInt(amount), times);
            container.set(key, PersistentDataType.INTEGER, (int) Math.clamp(total, 0, Integer.MAX_VALUE));
            return ItemEditor.Result.DONE;
        } catch (NumberFormatException e) {
            return ItemEditor.Result.NOT_A_NUMBER;
        }
    }

    /** The amount times the count, held at the ends of a long instead of overflowing. */
    private static long timesOver(long amount, long times) {
        try {
            return Math.multiplyExact(amount, times);
        } catch (ArithmeticException e) {
            return amount < 0 ? Long.MIN_VALUE / 2 : Long.MAX_VALUE / 2;
        }
    }

    private static ItemEditor.Result addFloat(PersistentDataContainer container, NamespacedKey key, String amount, long times) {
        try {
            double total = container.getOrDefault(key, PersistentDataType.FLOAT, 0f) + (double) Float.parseFloat(amount) * times;
            container.set(key, PersistentDataType.FLOAT, (float) Math.clamp(total, 0, Float.MAX_VALUE));
            return ItemEditor.Result.DONE;
        } catch (NumberFormatException e) {
            return ItemEditor.Result.NOT_A_NUMBER;
        }
    }

    /** Writes the type's empty value: 0, 0.0, false or an empty text. Does nothing for any other type. */
    static void writeEmpty(PersistentDataContainer container, NamespacedKey key, PersistentDataType<?, ?> type) {
        if (PersistentDataType.STRING.equals(type)) {
            container.set(key, PersistentDataType.STRING, "");
        } else if (PersistentDataType.INTEGER.equals(type)) {
            container.set(key, PersistentDataType.INTEGER, 0);
        } else if (PersistentDataType.FLOAT.equals(type)) {
            container.set(key, PersistentDataType.FLOAT, 0.0f);
        } else if (PersistentDataType.BOOLEAN.equals(type)) {
            container.set(key, PersistentDataType.BOOLEAN, false);
        }
    }

    private static ItemEditor.Result writeInt(PersistentDataContainer container, NamespacedKey key, String value) {
        try {
            container.set(key, PersistentDataType.INTEGER, Integer.parseInt(value));
            return ItemEditor.Result.DONE;
        } catch (NumberFormatException e) {
            return ItemEditor.Result.NOT_A_NUMBER;
        }
    }

    private static ItemEditor.Result writeFloat(PersistentDataContainer container, NamespacedKey key, String value) {
        try {
            container.set(key, PersistentDataType.FLOAT, Float.parseFloat(value));
            return ItemEditor.Result.DONE;
        } catch (NumberFormatException e) {
            return ItemEditor.Result.NOT_A_NUMBER;
        }
    }

    private static ItemEditor.Result writeBoolean(PersistentDataContainer container, NamespacedKey key, String value) {
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            return ItemEditor.Result.NOT_A_BOOLEAN;
        }
        container.set(key, PersistentDataType.BOOLEAN, "true".equalsIgnoreCase(value));
        return ItemEditor.Result.DONE;
    }
}
