package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The one place that changes an item's charges, tags and traits. The
 * commands and consumption all go through it, so a change is validated,
 * written and drawn into the lore the same way wherever it comes from.
 * <p>
 * Every method changes the given item in place: it copies the item's meta
 * once, makes the change and writes the meta back. Charges and trait
 * changes then redraw the lore with {@link ItemLoreRenderer#refresh}; tag
 * changes leave the lore alone. An item without stored lore is never redrawn,
 * as there is nothing to draw it from.
 * <p>
 * {@link #silently()} returns an editor that makes the same changes and nothing
 * more, for items nobody holds: no events, no lore redraw, and no value tags set
 * for a printable trait. {@link Effects} is the one place that says which of
 * the three an editor does.
 * <p>
 * Methods report what they did with a {@link Result}, and the caller decides
 * how to tell the player. When the result is anything other than
 * {@link Result#DONE}, the item is left unchanged.
 * <p>
 * After each edit, a changed charge count fires an
 * {@link org.skysongdev.skysongitems.api.event.ItemChargesChangeEvent} and every
 * other changed tag an {@link org.skysongdev.skysongitems.api.event.ItemTagChangeEvent},
 * so commands, consumption and the API all fire them. {@link #editedBy} names the
 * holder and cause the events carry.
 */
public final class ItemEditor {
    /** The trait every charged item carries, so its lore shows the charges. */
    public static final String CHARGES_TRAIT = "charges";

    /** What an edit did. */
    public enum Result {
        /** The change was made. */
        DONE,
        /** The item doesn't have both charges and max charges. */
        NOT_CHARGED,
        /** The item doesn't allow adding charges. */
        ADDING_LOCKED,
        /** The amount is out of range: negative charges, or max charges under 1. */
        INVALID_AMOUNT,
        /** No tag is registered with this id. */
        UNKNOWN_TAG,
        /** The tag holds numbers and the value isn't one. */
        NOT_A_NUMBER,
        /** The tag holds true or false and the value is neither. */
        NOT_A_BOOLEAN,
        /** No trait is registered with this id. */
        UNKNOWN_TRAIT,
        /** The item already has the trait. */
        ALREADY_PRESENT,
        /** The item doesn't have the tag or trait. */
        MISSING,
        /** The item has no traits at all. */
        NO_TRAITS
    }

    /**
     * What an edit does beyond storing the value: tell the server about it with the
     * change events, show it in the item's lore, and give a printable trait's value
     * tags their empty value. An edit to an item nobody holds needs none of the three.
     */
    public record Effects(boolean fireEvents, boolean redrawLore, boolean initialiseValueTags) {
        /** Everything an edit to an item a player holds does. */
        public static final Effects ALL = new Effects(true, true, true);
        /** Nothing: the value is stored and that is all. */
        public static final Effects NONE = new Effects(false, false, false);
    }

    private final TagRegistry tags;
    private final TraitRegistry traits;
    private final ItemChangeEvents events;
    private final Player holder;
    private final ItemChangeCause cause;
    private final Effects effects;

    /** An editor whose changes fire their events as {@link ItemChangeCause#COMMAND} with no holder. */
    public ItemEditor(TagRegistry tags, TraitRegistry traits) {
        this(tags, traits, new ItemChangeEvents(tags), null, ItemChangeCause.COMMAND, Effects.ALL);
    }

    private ItemEditor(TagRegistry tags, TraitRegistry traits, ItemChangeEvents events,
                       Player holder, ItemChangeCause cause, Effects effects) {
        this.tags = Objects.requireNonNull(tags, "tags");
        this.traits = Objects.requireNonNull(traits, "properties");
        this.events = events;
        this.holder = holder;
        this.cause = Objects.requireNonNull(cause, "cause");
        this.effects = Objects.requireNonNull(effects, "effects");
    }

    /**
     * An editor making the same changes, whose events name this holder and cause.
     *
     * @param holder the player holding the edited items, or null when unknown
     */
    public ItemEditor editedBy(@Nullable Player holder, ItemChangeCause cause) {
        return new ItemEditor(tags, traits, events, holder, cause, effects);
    }

    /**
     * An editor for items nobody holds, which makes the same changes and nothing more:
     * no change events, no lore redraw, and no value tags set for a printable trait.
     */
    public ItemEditor silently() {
        return new ItemEditor(tags, traits, events, holder, cause, Effects.NONE);
    }

    /** An editor for a {@code /si} command run by this player, holding the item they edit. */
    public ItemEditor forCommand(Player player) {
        return editedBy(player, ItemChangeCause.COMMAND);
    }

    /**
     * Sets the item's charges, capped at its max charges. An item without max
     * charges gets this amount as its max and the charges trait.
     *
     * @return {@link Result#INVALID_AMOUNT} for a negative amount, otherwise {@link Result#DONE}
     */
    public Result setCharges(ItemStack item, int charges) {
        return tracked(item, () -> applySetCharges(item, charges));
    }

    private Result applySetCharges(ItemStack item, int charges) {
        if (charges < 0) {
            return Result.INVALID_AMOUNT;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(ItemKeys.MAX_CHARGES)) {
            container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, charges);
            addChargesTrait(container);
        }
        int maxCharges = container.get(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, Math.min(charges, maxCharges));
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Sets the item's max charges. Charges the item already has are kept, even
     * above the new max. An item without charges gets this amount as its charges
     * and the charges trait.
     *
     * @return {@link Result#INVALID_AMOUNT} for an amount under 1, otherwise {@link Result#DONE}
     */
    public Result setMaxCharges(ItemStack item, int maxCharges) {
        return tracked(item, () -> applySetMaxCharges(item, maxCharges));
    }

    private Result applySetMaxCharges(ItemStack item, int maxCharges) {
        if (maxCharges < 1) {
            return Result.INVALID_AMOUNT;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(ItemKeys.CHARGES)) {
            container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, maxCharges);
            addChargesTrait(container);
        }
        container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, maxCharges);
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Adds charges to the item, or takes them off for a negative amount. The
     * result is kept between 0 and the item's max charges.
     *
     * @param ignoreLock whether to add charges even to an item that doesn't allow it
     * @return {@link Result#NOT_CHARGED} for an item without charges,
     *         {@link Result#ADDING_LOCKED} when adding to an item that doesn't allow it,
     *         otherwise {@link Result#DONE}
     */
    public Result addCharges(ItemStack item, int amount, boolean ignoreLock) {
        return tracked(item, () -> applyAddCharges(item, amount, ignoreLock));
    }

    private Result applyAddCharges(ItemStack item, int amount, boolean ignoreLock) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!isCharged(container)) {
            return Result.NOT_CHARGED;
        }
        if (amount > 0 && !ignoreLock && isAddingLocked(container)) {
            return Result.ADDING_LOCKED;
        }
        int charges = container.get(ItemKeys.CHARGES, PersistentDataType.INTEGER) + amount;
        int maxCharges = container.get(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges > maxCharges ? maxCharges : Math.max(charges, 0));
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Stores the charges an item has left after being used, exactly as given.
     * Unlike {@link #setCharges}, the amount isn't checked against the max.
     */
    public void setChargesLeft(ItemStack item, int charges) {
        tracked(item, () -> {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(ItemKeys.CHARGES, PersistentDataType.INTEGER, charges);
            saveAndRedraw(item, meta);
            return Result.DONE;
        });
    }

    /** Takes the item's charges, max charges and charges trait off it. */
    public void removeCharges(ItemStack item) {
        tracked(item, () -> applyRemoveCharges(item));
    }

    private Result applyRemoveCharges(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(ItemKeys.CHARGES);
        container.remove(ItemKeys.MAX_CHARGES);
        // The lore redraw skips items without traits, so clear the charges
        // line here when charges was the last trait.
        ItemData data = new ItemData(container);
        if (data.removeTrait(CHARGES_TRAIT) && !data.hasTraits()) {
            clearTraitLore(meta);
        }
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Turns adding charges to the item off if it is on, or on if it is off.
     * Read the new setting with {@link #canAddCharges}.
     *
     * @return {@link Result#NOT_CHARGED} for an item without charges, otherwise {@link Result#DONE}
     */
    public Result toggleChargeAdding(ItemStack item) {
        return tracked(item, () -> applyToggleChargeAdding(item));
    }

    private Result applyToggleChargeAdding(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!isCharged(container)) {
            return Result.NOT_CHARGED;
        }
        boolean canAdd = Boolean.TRUE.equals(container.get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN));
        container.set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, !canAdd);
        item.setItemMeta(meta);
        return Result.DONE;
    }

    /** Whether adding charges to the item was explicitly turned on. */
    public boolean canAddCharges(ItemStack item) {
        return Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer()
                .get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN));
    }

    /**
     * Sets a registered tag on the item, parsing the value as the tag's type.
     * The id matches a tag's key name, ignoring case.
     *
     * @return {@link Result#UNKNOWN_TAG}, {@link Result#NOT_A_NUMBER} or
     *         {@link Result#NOT_A_BOOLEAN} when the tag or value is invalid,
     *         otherwise {@link Result#DONE}
     */
    public Result setTag(ItemStack item, String tagId, String value) {
        return tracked(item, () -> applySetTag(item, tagId, value));
    }

    private Result applySetTag(ItemStack item, String tagId, String value) {
        TagRegistry.RegisteredTag tag = tags.find(tagId);
        if (tag == null) {
            return Result.UNKNOWN_TAG;
        }
        ItemMeta meta = item.getItemMeta();
        Result result = TagValues.write(meta.getPersistentDataContainer(), tag.key(), tag.type(), value);
        if (Result.DONE.equals(result)) {
            item.setItemMeta(meta);
        }
        return result;
    }

    /**
     * Removes a registered tag from the item. The id matches a tag's key name,
     * ignoring case.
     *
     * @return {@link Result#UNKNOWN_TAG} for an unregistered tag, {@link Result#MISSING}
     *         when the item doesn't have it, otherwise {@link Result#DONE}
     */
    public Result removeTag(ItemStack item, String tagId) {
        return tracked(item, () -> applyRemoveTag(item, tagId));
    }

    private Result applyRemoveTag(ItemStack item, String tagId) {
        TagRegistry.RegisteredTag tag = tags.find(tagId);
        if (tag == null) {
            return Result.UNKNOWN_TAG;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(tag.key(), tag.type())) {
            return Result.MISSING;
        }
        container.remove(tag.key());
        item.setItemMeta(meta);
        return Result.DONE;
    }

    /**
     * Adds a registered trait to the item. For a printable trait, each of
     * its value tags is set to its type's empty value (0, 0.0, false or an empty
     * text), replacing any value already there.
     *
     * @return {@link Result#UNKNOWN_TRAIT} for an unregistered trait,
     *         {@link Result#ALREADY_PRESENT} when the item has it, otherwise {@link Result#DONE}
     */
    public Result addTrait(ItemStack item, String traitId) {
        return tracked(item, () -> applyAddTrait(item, traitId));
    }

    private Result applyAddTrait(ItemStack item, String traitId) {
        if (!traits.contains(traitId)) {
            return Result.UNKNOWN_TRAIT;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        ItemData data = new ItemData(container);
        if (data.hasTrait(traitId)) {
            return Result.ALREADY_PRESENT;
        }
        data.addTrait(traitId);
        if (effects.initialiseValueTags() && traits.get(traitId) instanceof PrintableTrait printable) {
            for (String valueTag : printable.getValuetags()) {
                NamespacedKey key = ItemKeys.named(valueTag);
                TagValues.writeEmpty(container, key, tags.getType(key));
            }
        }
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Removes a trait from the item, whether or not it is still registered.
     * Removing the last one also takes the trait lines and tags out of the
     * lore and name.
     *
     * @return {@link Result#NO_TRAITS} for an item without traits,
     *         {@link Result#MISSING} when the item doesn't have this one,
     *         otherwise {@link Result#DONE}
     */
    public Result removeTrait(ItemStack item, String traitId) {
        return tracked(item, () -> applyRemoveTrait(item, traitId));
    }

    private Result applyRemoveTrait(ItemStack item, String traitId) {
        ItemMeta meta = item.getItemMeta();
        ItemData data = ItemData.from(meta);
        if (!data.hasTraits()) {
            return Result.NO_TRAITS;
        }
        if (!data.removeTrait(traitId)) {
            return Result.MISSING;
        }
        if (!data.hasTraits()) {
            clearTraitLore(meta);
        }
        saveAndRedraw(item, meta);
        return Result.DONE;
    }

    /**
     * Runs an edit and fires the change events for whatever it changed. An edit that
     * changes nothing, including every one that doesn't return {@link Result#DONE}, fires none.
     */
    private Result tracked(ItemStack item, Supplier<Result> edit) {
        if (!effects.fireEvents()) {
            return edit.get();
        }
        Map<NamespacedKey, Object> before = events.snapshot(item);
        Result result = edit.get();
        events.fireChanges(holder, item, before, cause);
        return result;
    }

    private static boolean isCharged(PersistentDataContainer container) {
        return container.has(ItemKeys.MAX_CHARGES) && container.has(ItemKeys.CHARGES);
    }

    /** Whether adding charges was explicitly turned off; items that never set it allow it. */
    private static boolean isAddingLocked(PersistentDataContainer container) {
        return Boolean.FALSE.equals(container.get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN));
    }

    private static void addChargesTrait(PersistentDataContainer container) {
        ItemData data = new ItemData(container);
        if (!data.hasTrait(CHARGES_TRAIT)) {
            data.addTrait(CHARGES_TRAIT);
        }
    }

    private void saveAndRedraw(ItemStack item, ItemMeta meta) {
        boolean hasStoredLore = meta.getPersistentDataContainer().has(ItemKeys.LORE, PersistentDataType.STRING);
        item.setItemMeta(meta);
        if (hasStoredLore && effects.redrawLore()) {
            ItemLoreRenderer.refresh(item);
        }
    }

    /** Clears the lore lines a trait drew, for an item that just lost its last trait. */
    private void clearTraitLore(ItemMeta meta) {
        if (effects.redrawLore()) {
            ItemLoreRenderer.renderWithoutTraits(meta);
        }
    }
}
