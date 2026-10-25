package org.skysongdev.skysongitems.integration;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.SkysongItem;
import org.skysongdev.skysongitems.utils.TextParser;
import org.skysongdev.skysongitems.utils.Utils;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.skysongdev.skysongitems.utils.ItemKeys;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class TBPSkysongIntegration implements ItemIntegration {
    CompletableFuture<Void> initialized = new CompletableFuture<>();
    @Override
    public Optional<ItemStack> createItem(String s) {
        //Grabs an item file with that name from the "brewing" folder in the plugin's
        //data folder, and then returns the item stack by generating the item from that file.
        //If the file doesn't exist, return Optional.empty()
        String fileName = s.replace("skysong:", "");
        File itemFile = getPlugin().getDataFolder().toPath().resolve("brewing").resolve(fileName + ".yml").toFile();
        if (itemFile.exists()) {
            SkysongItem skysongitem = Utils.LoadSkysongItem(itemFile);
            Bukkit.getLogger().warning("Item file found for id: " + s + ". Generating item...");
            return Optional.of(getPlugin().getUtils().GenerateSkysongItem(skysongitem));
        }
        Bukkit.getLogger().warning("Item file not found for id: " + s);
        return Optional.empty();
    }

    @Override
    public boolean isIngredient(String s) {
        //Checks if an item file with that name exists in the "brewing" folder in the plugin's
        getPlugin().getLogger().warning("Checking if " + s + " is an ingredient");
        File itemFile = getPlugin().getDataFolder().toPath().resolve("brewing").resolve(s + ".yml").toFile();
        getPlugin().getLogger().warning("Ingredient file " + itemFile.getAbsolutePath() + " exists: " + itemFile.exists());
        return itemFile.exists();
    }

    @Override
    public CompletableFuture<Optional<Ingredient>> createIngredient(String id) {
        return ItemIntegration.super.createIngredient(id);
    }

    @Override
    public @Nullable Component displayName(String s) {
        getPlugin().getLogger().warning("Getting display name for " + s);
        File itemFile = getPlugin().getDataFolder().toPath().resolve("brewing").resolve(s + ".yml").toFile();
        if (itemFile.exists()) {
            SkysongItem skysongitem = Utils.LoadSkysongItem(itemFile);
            String name = TextParser.translateColorCodes(skysongitem.getName());
            name = TextParser.translateHexColorCodes("&#", "", name);
            return getPlugin().getMiniMessage().deserialize(name);
        }
        return null;
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        //checks for the item's id in its corresponding material_id tag. If it doesn't have one, return null
        getPlugin().getLogger().warning("Getting item id for " + itemStack);
        if(itemStack.getItemMeta() != null && itemStack.getItemMeta().getPersistentDataContainer().has(ItemKeys.MATERIAL_ID)){
            getPlugin().getLogger().warning("Found material id key for item: " + itemStack.getItemMeta().getPersistentDataContainer().get(ItemKeys.MATERIAL_ID, Constants.tagList.get(ItemKeys.MATERIAL_ID)));
            return (String) itemStack.getItemMeta().getPersistentDataContainer().get(ItemKeys.MATERIAL_ID, Constants.tagList.get(ItemKeys.MATERIAL_ID));
        }
        getPlugin().getLogger().warning("No material id key found. meta" + itemStack.getItemMeta() + " pdc " + itemStack.getItemMeta().getPersistentDataContainer());
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> initialized() {
        return initialized;
    }

    public void registerComplete(){
        initialized.complete(null);
    }

    @Override
    public String getId() {
        return "skysong";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void loadIfPossible() {
        try {
            if (!Bukkit.getServicesManager().isProvidedFor(TheBrewingProjectApi.class)) {
                return;
            }
            RegisteredServiceProvider<TheBrewingProjectApi> provider = Bukkit.getServicesManager().getRegistration(TheBrewingProjectApi.class);
            if (provider != null) {
                provider.getProvider().getIntegrationManager().register(IntegrationTypes.ITEM, this);
            }
        } catch (NoClassDefFoundError ignored) {
        }
    }

}
