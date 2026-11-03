package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.TagType;

/** Maps the data types tags are stored with to the API's {@link TagType}. */
final class TagTypes {
    private TagTypes() {
        // Static methods only.
    }

    /** The API type for a tag's data type, or null for a data type tags are never configured with. */
    static TagType of(PersistentDataType<?, ?> type) {
        if (PersistentDataType.INTEGER.equals(type)) {
            return TagType.INT;
        }
        if (PersistentDataType.FLOAT.equals(type)) {
            return TagType.FLOAT;
        }
        if (PersistentDataType.BOOLEAN.equals(type)) {
            return TagType.BOOL;
        }
        if (PersistentDataType.STRING.equals(type)) {
            return TagType.STRING;
        }
        return null;
    }
}
