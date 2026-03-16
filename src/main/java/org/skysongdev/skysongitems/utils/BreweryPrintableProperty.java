package org.skysongdev.skysongitems.utils;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class BreweryPrintableProperty extends PrintableProperty{
    public BreweryPrintableProperty(String id, String type, String item, String name, String description, String printable, ArrayList<String> valuetags, int weight) {
        super(id, type, item, name, description, printable, valuetags, weight);
    }

    public Component getBrewedPrinting(ItemStack item){
        Component base = getPlugin().getMiniMessage().deserialize(printable.split(";")[0]);
        Component end = getPlugin().getMiniMessage().deserialize(printable.split(";")[1]);

        Optional<Brew> itemBrew = BrewAdapter.fromItem(item);
        Brew brew = itemBrew.get();
        RecipeRegistry<ItemStack> registry = getPlugin().getTbp().getRecipeRegistry();
        Optional<Recipe<ItemStack>> recipe = brew.closestRecipe(registry);
        if(recipe.isPresent()){
            BrewScore score = brew.score(recipe.get());
            return base.append(GlobalTranslator.render(score.displayName(), getPlugin().getTbp().getConfiguration().language()).color(NamedTextColor.GOLD)).append(end).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false));
        }
        return null;
    }
}
