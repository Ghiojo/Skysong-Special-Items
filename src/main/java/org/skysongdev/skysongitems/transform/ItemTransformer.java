package org.skysongdev.skysongitems.transform;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.api.TransformResult;
import org.skysongdev.skysongitems.api.event.ItemTransformEvent;
import org.skysongdev.skysongitems.timer.ItemTimer;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.timer.TimerOrigin;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemFactory;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;
import org.skysongdev.skysongitems.utils.TagRegistry;
import org.skysongdev.skysongitems.utils.TraitRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Transforms the item a player holds into its next form, as transformations.yml says.
 *
 * <p>A rule that lists parts copies just those parts of the new form onto the item, see
 * {@link ItemPart}; the rest of the item stays as it is. A rule without parts swaps the
 * whole item: it is rebuilt from the saved item of its next form, then given back what
 * belongs to this copy of the item rather than to its form:
 * <ul>
 *     <li>the stack size;</li>
 *     <li>its charges, capped at the new form's maximum, so cycling forms never refills an
 *     item (when the new form has no charges, none are kept);</li>
 *     <li>its HP, capped at the new form's maximum in the same way;</li>
 *     <li>its durability damage, when both forms can be damaged;</li>
 *     <li>the book bound to it, with its right-click and DM-only settings;</li>
 *     <li>the tags and traits marked {@code keep_on_transform}, or named in the rule's
 *     {@code keep} list, with the held item's values, see {@link KeptOnTransform}.</li>
 * </ul>
 * Everything else comes from the new form, so every other tag and trait the new form
 * lacks is dropped, whichever plugin registered it.
 */
public final class ItemTransformer {
    /** The id of the timer action that transforms an item, the timers that carry over to its new form. */
    public static final String TIMER_ACTION = "transform";

    private final Supplier<TransformationRules> rules;
    private final TagRegistry tags;
    private final TraitRegistry traits;
    private final Consumer<Player> permissionItemSync;
    private final UnaryOperator<ItemStack> upgrader;

    /**
     * @param rules              looked up on each use, since /si reload replaces them
     * @param tags               the tag types, and which tags are kept on transform
     * @param traits             which traits are kept on transform
     * @param permissionItemSync grants and revokes the player's permission-item permissions after the change
     * @param upgrader           brings an item up to the current item version, as {@code /si update} does
     */
    public ItemTransformer(Supplier<TransformationRules> rules, TagRegistry tags, TraitRegistry traits,
                           Consumer<Player> permissionItemSync, UnaryOperator<ItemStack> upgrader) {
        this.rules = rules;
        this.tags = tags;
        this.traits = traits;
        this.permissionItemSync = permissionItemSync;
        this.upgrader = upgrader;
    }

    /**
     * Transforms the item in the player's main hand, if its form has a transformation they
     * may use and no listener cancels the {@link ItemTransformEvent}.
     */
    public TransformResult transform(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            return TransformResult.NOT_HOLDING;
        }
        Transformation rule = rules.get().find(materialIdOf(held)).orElse(null);
        if (rule == null) {
            return TransformResult.NO_TRANSFORMATION;
        }
        if (rule.permission() != null && !player.hasPermission(rule.permission())) {
            return TransformResult.NO_PERMISSION;
        }
        if (!new ItemTransformEvent(player, held, rule.fromId(), rule.targetMaterialId(), rule.targetId()).callEvent()) {
            return TransformResult.CANCELLED;
        }
        player.getInventory().setItemInMainHand(transformed(held, rule));
        // Setting the hand slot fires none of the inventory events permission items follow.
        permissionItemSync.accept(player);
        return TransformResult.TRANSFORMED;
    }

    /** The rule the item's form starts, empty when it has none. */
    public Optional<Transformation> ruleFor(@Nullable ItemStack item) {
        return item == null ? Optional.empty() : rules.get().find(materialIdOf(item));
    }

    /** The rule the form with this material_id starts, empty when it has none. */
    public Optional<Transformation> rule(@Nullable String materialId) {
        return rules.get().find(materialId);
    }

    /** The form the item is in: its material_id, or null for air and items without one. */
    public static @Nullable String materialIdOf(ItemStack item) {
        if (item.getType().isAir()) {
            return null;
        }
        return item.getPersistentDataContainer().get(ItemKeys.MATERIAL_ID, PersistentDataType.STRING);
    }

    /** The item the held one becomes under this rule, its new timers counted from now. */
    public ItemStack transformed(ItemStack held, Transformation rule) {
        return transformed(held, rule, null);
    }

    /**
     * The item the held one becomes under this rule. The held item itself is not changed.
     *
     * <p>The new form starts the timers of its saved item, counted from the origin when given.
     * Of the held item's own timers, only those that transform it carry over, so a repeating
     * transformation keeps going, unless the new form's saved item brings a transformation
     * timer of its own. Its other timers belonged to the old form and are dropped.
     *
     * @param origin where the new form's timers count from, or null for now
     */
    public ItemStack transformed(ItemStack held, Transformation rule, @Nullable TimerOrigin origin) {
        ItemStack result = rule.isWholeItem() ? wholeItem(held, rule) : withParts(held, rule);
        List<ItemTimer> timers = new ArrayList<>(ItemFactory.startedTimers(rule.target(), origin));
        if (timers.stream().noneMatch(ItemTransformer::transforms)) {
            ItemTimerStore.read(held.getPersistentDataContainer()).stream()
                    .filter(ItemTransformer::transforms)
                    .forEach(timers::add);
        }
        if (!timers.isEmpty() || ItemTimerStore.hasTimers(result.getPersistentDataContainer())) {
            ItemTimerStore.write(result, timers);
        }
        return result;
    }

    private static boolean transforms(ItemTimer timer) {
        return TIMER_ACTION.equals(timer.action());
    }

    /**
     * The held item with the rule's parts taken from the new form, and its new form's
     * material_id. Items of an older item version are updated first, so their data is in
     * the shape the parts are copied into.
     */
    private ItemStack withParts(ItemStack held, Transformation rule) {
        ItemStack target = ItemFactory.create(rule.target());
        ItemStack result = upgrader.apply(held.clone());
        if (lists(rule, ItemPart.Kind.MATERIAL)) {
            result = PartCopy.withMaterial(result, target.getType());
        }
        ItemMeta meta = result.getItemMeta();
        PartCopy copy = new PartCopy(meta, target, tags, keptFrom(result.getPersistentDataContainer(), rule));
        rule.parts().forEach(part -> part.copy(copy));
        meta.getPersistentDataContainer().set(ItemKeys.MATERIAL_ID, PersistentDataType.STRING, rule.targetMaterialId());
        if (!ItemData.from(meta).hasTraits()) {
            ItemLoreRenderer.renderWithoutTraits(meta);
        }
        result.setItemMeta(meta);
        if (lists(rule, ItemPart.Kind.CMD)) {
            PartCopy.copyCustomModelData(target, result);
        }
        // Redraws the name and lore from what the item now stores.
        ItemLoreRenderer.refresh(result);
        return result;
    }

    private static boolean lists(Transformation rule, ItemPart.Kind kind) {
        return rule.parts().stream().anyMatch(part -> part.kind() == kind);
    }

    private ItemStack wholeItem(ItemStack held, Transformation rule) {
        ItemStack result = ItemFactory.create(rule.target());
        result.setAmount(held.getAmount());
        ItemMeta meta = result.getItemMeta();
        PersistentDataContainerView from = held.getPersistentDataContainer();
        PersistentDataContainer to = meta.getPersistentDataContainer();
        keepUpToTheNewMax(from, to, ItemKeys.CHARGES, ItemKeys.MAX_CHARGES);
        keepUpToTheNewMax(from, to, ItemKeys.ITEM_HP, ItemKeys.MAX_ITEM_HP);
        keepBoundBook(from, to);
        keepMarkedTagsAndTraits(from, to, keptFrom(from, rule));
        keepDamage(held, result, meta);
        result.setItemMeta(meta);
        // Redraws the trait lines, such as the charges line, with the kept values.
        ItemLoreRenderer.refresh(result);
        return result;
    }

    /** Keeps the held item's charges or HP, capped at the new form's maximum; a form without one gets none. */
    private static void keepUpToTheNewMax(PersistentDataContainerView from, PersistentDataContainer to,
                                          NamespacedKey current, NamespacedKey max) {
        Integer value = from.get(current, PersistentDataType.INTEGER);
        Integer newMax = to.get(max, PersistentDataType.INTEGER);
        if (value != null && newMax != null) {
            to.set(current, PersistentDataType.INTEGER, Math.min(value, newMax));
        }
    }

    private KeptOnTransform keptFrom(PersistentDataContainerView held, Transformation rule) {
        return new KeptOnTransform(tags, traits, ItemData.parseTraits(ItemData.readTraitsText(held)), rule.keep());
    }

    /** Puts the kept tags back with the held item's values, and adds the kept traits the new form lacks. */
    private void keepMarkedTagsAndTraits(PersistentDataContainerView from, PersistentDataContainer to,
                                         KeptOnTransform kept) {
        for (NamespacedKey key : from.getKeys()) {
            if (kept.keepsTag(key)) {
                copy(from, to, key, tags.getType(key));
            }
        }
        ItemData data = new ItemData(to);
        kept.traits().stream().filter(traitId -> !data.hasTrait(traitId)).forEach(data::addTrait);
    }

    private static <P, C> void copy(PersistentDataContainerView from, PersistentDataContainer to,
                                    NamespacedKey key, PersistentDataType<P, C> type) {
        C value = from.get(key, type);
        if (value != null) {
            to.set(key, type, value);
        }
    }

    private static void keepBoundBook(PersistentDataContainerView from, PersistentDataContainer to) {
        Integer book = from.get(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER);
        if (book == null) {
            return;
        }
        to.set(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER, book);
        copyFlag(from, to, ItemKeys.RIGHT_CLICKABLE);
        copyFlag(from, to, ItemKeys.DM_ONLY);
    }

    private static void copyFlag(PersistentDataContainerView from, PersistentDataContainer to, NamespacedKey key) {
        Boolean flag = from.get(key, PersistentDataType.BOOLEAN);
        if (flag == null) {
            to.remove(key);
        } else {
            to.set(key, PersistentDataType.BOOLEAN, flag);
        }
    }

    private static void keepDamage(ItemStack held, ItemStack result, ItemMeta meta) {
        if (!(held.getItemMeta() instanceof Damageable heldDamage) || !heldDamage.hasDamage()
                || !(meta instanceof Damageable newDamage)) {
            return;
        }
        int maxDamage = newDamage.hasMaxDamage() ? newDamage.getMaxDamage() : result.getType().getMaxDurability();
        if (maxDamage > 0) {
            newDamage.setDamage(Math.min(heldDamage.getDamage(), maxDamage - 1));
        }
    }
}
