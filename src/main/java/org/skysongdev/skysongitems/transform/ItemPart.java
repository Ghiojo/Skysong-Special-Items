package org.skysongdev.skysongitems.transform;

import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.ItemKeys;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * One part of an item a transformation can take from the new form, as named in the
 * {@code parts} list of transformations.yml. Lore sections follow {@code /pd}, {@code /fd}
 * and {@code /sd}: 0 physical, 1 functional, 2 skill.
 *
 * @param kind  which part
 * @param tagId the tag's id for {@link Kind#TAG}, written {@code tag:<id>}; null otherwise
 */
public record ItemPart(Kind kind, @Nullable String tagId) {
    private static final String TAG_PREFIX = "tag:";

    public ItemPart {
        Objects.requireNonNull(kind, "kind");
        if ((kind == Kind.TAG) != (tagId != null)) {
            throw new IllegalArgumentException("Only the tag part names a tag");
        }
    }

    /** The part a name in transformations.yml stands for, ignoring case. Empty for an unknown name. */
    public static Optional<ItemPart> parse(String name) {
        String lower = name.toLowerCase(Locale.ROOT).trim();
        if (lower.startsWith(TAG_PREFIX) && lower.length() > TAG_PREFIX.length()) {
            return Optional.of(new ItemPart(Kind.TAG, lower.substring(TAG_PREFIX.length())));
        }
        return Arrays.stream(Kind.values())
                .filter(kind -> kind != Kind.TAG && kind.configName.equals(lower))
                .findFirst()
                .map(kind -> new ItemPart(kind, null));
    }

    /** Copies this part from the new form onto the item being transformed. */
    void copy(PartCopy copy) {
        kind.copy(copy, this);
    }

    /** The kinds of part, each copying its own data. */
    public enum Kind {
        /** The stored name, and so the display name. */
        NAME("name") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyKey(ItemKeys.NAME);
            }
        },
        PHYSICAL("physical") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyLoreSection(0);
            }
        },
        FUNCTIONAL("functional") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyLoreSection(1);
            }
        },
        SKILL("skill") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyLoreSection(2);
            }
        },
        /** Every lore section, and which of them are hidden. */
        LORE("lore") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyKey(ItemKeys.LORE);
                copy.copyKey(ItemKeys.DELETED_LINES);
            }
        },
        /** The new form's traits, plus the item's kept ones. */
        TRAITS("traits") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyTraits();
            }
        },
        /** Every tag, whichever plugin registered it, except the ones other parts cover and the kept ones. */
        TAGS("tags") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyTags();
            }
        },
        /** One tag, named by {@link ItemPart#tagId()}. */
        TAG("tag:<id>") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyTag(part.tagId());
            }
        },
        /** The maximum charges and whether charges can be added; the charges left are kept. */
        CHARGES("charges") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyCharges();
            }
        },
        /** The maximum HP; the HP left is kept, up to the new maximum. */
        ITEM_HP("hp") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyItemHp();
            }
        },
        /** The permission the item grants while held. */
        PERMISSION("permission") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyKey(ItemKeys.PERMISSION);
            }
        },
        MODEL("model") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                copy.copyItemModel();
            }
        },
        /** The custom model data. */
        CMD("cmd") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                // Done by PartCopy.copyCustomModelData once the meta is on the stack.
            }
        },
        /** The item type, such as IRON_SWORD. Changed before every other part, as it makes a new stack. */
        MATERIAL("material") {
            @Override
            void copy(PartCopy copy, ItemPart part) {
                // Done by PartCopy.withMaterial before the other parts are copied.
            }
        };

        private final String configName;

        Kind(String configName) {
            this.configName = configName;
        }

        abstract void copy(PartCopy copy, ItemPart part);
    }
}
