package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.skysongdev.skysongitems.api.ExtraLorePosition;
import org.skysongdev.skysongitems.api.LoreSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Lays an item's lore out in paragraphs: its shown lore sections, and other plugins'
 * lines above or below them or after the traits, with a blank line between paragraphs.
 */
final class LoreLayout {
    private static final char SHOWN = '0';
    private static final String NO_HIDDEN_SECTIONS = "000";
    private static final LoreSection[] SECTIONS = LoreSection.values();

    private LoreLayout() {
    }

    /**
     * The lines of these lore sections, the hidden ones left out, with the extra lines
     * placed above or below a section where their plugin put them. A section's extra
     * lines show even when the item hides the section.
     */
    static List<Component> sections(List<String> sections, String deletedLines, ExtraLore extras) {
        if (extras.isEmpty()) {
            return TextParser.TranslateToLore(sections, deletedLines);
        }
        String hidden = deletedLines == null || deletedLines.isEmpty() ? NO_HIDDEN_SECTIONS : deletedLines;
        List<List<Component>> paragraphs = new ArrayList<>();
        for (int index = 0; index < Math.max(sections.size(), SECTIONS.length); index++) {
            addExtras(paragraphs, extras, index, true);
            if (index < sections.size() && isShown(hidden, index)) {
                paragraphs.add(TextParser.TranslateToLore(List.of(sections.get(index)), String.valueOf(SHOWN)));
            }
            addExtras(paragraphs, extras, index, false);
        }
        return joined(paragraphs);
    }

    private static void addExtras(List<List<Component>> paragraphs, ExtraLore extras, int index, boolean above) {
        if (index < SECTIONS.length) {
            LoreSection section = SECTIONS[index];
            paragraphs.addAll(extras.at(above ? ExtraLorePosition.above(section) : ExtraLorePosition.below(section)));
        }
    }

    private static boolean isShown(String hidden, int index) {
        return index >= hidden.length() || hidden.charAt(index) == SHOWN;
    }

    private static List<Component> joined(List<List<Component>> paragraphs) {
        List<Component> lines = new ArrayList<>();
        for (List<Component> paragraph : paragraphs) {
            if (!lines.isEmpty()) {
                lines.add(Component.empty());
            }
            lines.addAll(paragraph);
        }
        return lines;
    }

    /** Adds the lines plugins put after the traits at the end, each a paragraph of its own. */
    static void appendAfterTraits(List<Component> lore, ExtraLore extras) {
        for (List<Component> paragraph : extras.at(ExtraLorePosition.afterTraits())) {
            if (!lore.isEmpty() && !isBlank(lore.getLast())) {
                lore.add(Component.empty());
            }
            lore.addAll(paragraph);
        }
    }

    private static boolean isBlank(Component line) {
        return PlainTextComponentSerializer.plainText().serialize(line).isEmpty();
    }
}
