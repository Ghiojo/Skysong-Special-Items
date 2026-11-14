package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.api.ItemTransformations;
import org.skysongdev.skysongitems.api.TransformResult;
import org.skysongdev.skysongitems.transform.ItemTransformer;
import org.skysongdev.skysongitems.transform.Transformation;

import java.util.Objects;
import java.util.Optional;

/** Hands the API the same transformations {@code /transform} uses. */
final class ItemTransformationsImpl implements ItemTransformations {
    private final ItemTransformer transformer;

    ItemTransformationsImpl(ItemTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public @NotNull Optional<String> targetOf(@Nullable ItemStack item) {
        StoredValues.requireMainThread();
        return transformer.ruleFor(item).map(Transformation::targetId);
    }

    @Override
    public @NotNull TransformResult transform(@NotNull Player player) {
        StoredValues.requireMainThread();
        return transformer.transform(Objects.requireNonNull(player, "player"));
    }
}
