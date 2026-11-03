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
