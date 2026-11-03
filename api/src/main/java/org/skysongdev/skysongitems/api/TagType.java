package org.skysongdev.skysongitems.api;

/** The type of value a SkysongItems tag holds, as configured with {@code type:} in config.yml. */
public enum TagType {
    /** A whole number, {@code type: int}. */
    INT,
    /** A decimal number, {@code type: float}. */
    FLOAT,
    /** True or false, {@code type: bool}. */
    BOOL,
    /** Text, {@code type: string}. */
    STRING
}
