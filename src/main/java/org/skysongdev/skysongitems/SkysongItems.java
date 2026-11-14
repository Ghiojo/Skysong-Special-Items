package org.skysongdev.skysongitems;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.skysongdev.skysongitems.apiimpl.SkysongItemsApiImpl;
import org.skysongdev.skysongitems.commands.SkysongItemsCommand;
import org.skysongdev.skysongitems.commands.transform.TransformCommand;
import org.skysongdev.skysongitems.commands.lore.LoreSection;
import org.skysongdev.skysongitems.commands.lore.LoreSectionCommand;
import org.skysongdev.skysongitems.commands.lore.Updater;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.BookQueries;
import org.skysongdev.skysongitems.database.Database;
import org.skysongdev.skysongitems.database.DatabaseSettings;
import org.skysongdev.skysongitems.integration.BrewingProjectIntegration;
import org.skysongdev.skysongitems.integration.CraftEngineIntegration;
import org.skysongdev.skysongitems.integration.CraftEngineItemIds;
import org.skysongdev.skysongitems.integration.LuckPermsIntegration;
import org.skysongdev.skysongitems.integration.SoftDependency;
import org.skysongdev.skysongitems.listeners.*;
import org.skysongdev.skysongitems.registration.RegistrationListener;
import org.skysongdev.skysongitems.registration.RegistryLoader;
import org.skysongdev.skysongitems.transform.ItemTransformer;
import org.skysongdev.skysongitems.transform.TransformationLoader;
import org.skysongdev.skysongitems.transform.TransformationRules;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.MaterialPermissions;
import org.skysongdev.skysongitems.utils.PermissionItemSync;
import org.skysongdev.skysongitems.utils.TraitRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

import java.io.File;
import java.util.Optional;

// Not final: MockBukkit loads a plugin through a generated subclass of its main class.
public class SkysongItems extends JavaPlugin {
    private static SkysongItems plugin;
    private Database database;
    private BookQueries bookQueries;
    private Updater updater;
    private final TraitRegistry traitRegistry = new TraitRegistry();
    private final TagRegistry tagRegistry = new TagRegistry();
    private final ItemEditor itemEditor = new ItemEditor(tagRegistry, traitRegistry);
    private RegistryLoader registryLoader;
    private final MaterialPermissions materialPermissions = new MaterialPermissions();
    private MiniMessage miniMessage = MiniMessage.miniMessage();
    // Each integration is null when its plugin is not installed.
    private LuckPermsIntegration luckPerms;
    private BrewingProjectIntegration brewingProject;
    private CraftEngineItemIds craftEngine;
    // Null when LuckPerms is not installed, as permission items then grant nothing.
    private PermissionItemListener permissionItemListener;
    // Null until the end of onEnable, and again after onDisable.
    private SkysongItemsApiImpl api;
    // Empty until the registries are first loaded with other plugins' tags.
    private TransformationRules transformations = TransformationRules.empty();

    public static SkysongItems getPlugin(){
        if(plugin == null){
            plugin = new SkysongItems();
        }
        return plugin;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    /** TheBrewingProject integration, empty when TheBrewingProject is not installed. */
    public Optional<BrewingProjectIntegration> getBrewingProject() {
        return Optional.ofNullable(brewingProject);
    }

    /** LuckPerms integration, empty when LuckPerms is not installed. */
    public Optional<LuckPermsIntegration> getLuckPerms() {
        return Optional.ofNullable(luckPerms);
    }

    /** Edits items' CraftEngine ids, empty when CraftEngine is not installed. */
    public Optional<CraftEngineItemIds> getCraftEngine() {
        return Optional.ofNullable(craftEngine);
    }

    /** The item traits configured in config.yml. */
    public TraitRegistry getTraitRegistry() {
        return traitRegistry;
    }

    /** The item tags, built-in and configured in config.yml. */
    public TagRegistry getTagRegistry() {
        return tagRegistry;
    }

    /**
     * Rebuilds the tags and traits from config.yml and asks other plugins for theirs
     * through {@link org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent}.
     * Main thread only. Reload config.yml first to pick up changes on disk.
     */
    public void reloadRegistries() {
        registryLoader.loadWithPlugins(getConfig(), this::useRegistries);
        // The saved items the rules point to are read with the tags just registered.
        reloadTransformations();
    }

    /** The transformations loaded from transformations.yml. */
    public TransformationRules getTransformations() {
        return transformations;
    }

    /**
     * Re-reads transformations.yml and the saved items its rules point to, creating the
     * file first if it does not exist. Called with every registry reload, and after
     * {@code /si saveitem} so a re-saved form takes effect straight away.
     */
    public void reloadTransformations() {
        File file = new File(getDataFolder(), TransformationLoader.FILE_NAME);
        if (!file.exists()) {
            saveResource(TransformationLoader.FILE_NAME, false);
        }
        transformations = new TransformationLoader(TransformationLoader.savedItemsOnDisk(), tagRegistry::contains, getLogger())
                .load(YamlConfiguration.loadConfiguration(file));
    }

    // The registries are shared with the item editor and the API, so a load is copied into them.
    private void useRegistries(TagRegistry tags, TraitRegistry traits) {
        tagRegistry.replaceWith(tags);
        traitRegistry.replaceWith(traits);
    }

    /** Changes items' charges, tags and traits. */
    public ItemEditor getItemEditor() {
        return itemEditor;
    }

    /** The permissions needed to use items, by material id, from materials.yml. */
    public MaterialPermissions getMaterialPermissions() {
        return materialPermissions;
    }

    public Updater getUpdater() {
        return updater;
    }

    private SkysongItemsCommand mainCommand;

    public SkysongItemsCommand getMainCommand() {
        return mainCommand;
    }

    /** The books stored in the database. Query them through {@link #getBookQueries()}, off the main thread. */
    public BookDescriptionRepository getBookRepository() {
        return database.getBooks();
    }

    /** Runs the book commands' and bound-book right-clicks' queries off the main thread. */
    public BookQueries getBookQueries() {
        return bookQueries;
    }

    @Override
    public void onLoad(){
        if (SoftDependency.isInstalled(getServer().getPluginManager(), SoftDependency.BREWING_PROJECT,
                "brewing ingredients and brew score printing", getLogger())) {
            brewingProject = new BrewingProjectIntegration();
            brewingProject.registerItemIntegration();
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        if (brewingProject != null) {
            brewingProject.enable(this);
        }
        saveDefaultConfig();

        if (SoftDependency.isInstalled(getServer().getPluginManager(), SoftDependency.CRAFT_ENGINE,
                "/si furniture", getLogger())) {
            craftEngine = new CraftEngineIntegration();
        }

        bookQueries = BookQueries.onWorkerThread(this, this::getBookRepository);
        InitializeDatabase(() -> { });

        super.onEnable();
        mainCommand = new SkysongItemsCommand();
        updater = new Updater();
        getCommand("skysongitems").setExecutor(mainCommand);
        getServer().getPluginManager().registerEvents(new DisableItemUse(), this);
        getServer().getPluginManager().registerEvents(new OpenDescRightClick(bookQueries), this);
        getServer().getPluginManager().registerEvents(new UpdateItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ConsumptionListener(), this);
        ItemTransformer transformer = new ItemTransformer(this::getTransformations, tagRegistry, this::syncPermissionItems,
                updater::upgrade);
        getCommand("transform").setExecutor(new TransformCommand(transformer, this::getTransformations));
        for (LoreSection section : LoreSection.values()) {
            getCommand(section.getCommandName()).setExecutor(new LoreSectionCommand(section));
        }

        // Other plugins add their tags and traits once all plugins have enabled.
        registryLoader = new RegistryLoader(getServer().getPluginManager(), getLogger());
        registryLoader.loadConfig(getConfig(), this::useRegistries);
        getServer().getPluginManager().registerEvents(new RegistrationListener(this), this);
        materialPermissions.load(getDataFolder(), getLogger());
        fixFilePaths();


        if (SoftDependency.isInstalled(getServer().getPluginManager(), SoftDependency.LUCK_PERMS,
                "permission items", getLogger())) {
            luckPerms = new LuckPermsIntegration();
            permissionItemListener = new PermissionItemListener(this, new PermissionItemSync());
            permissionItemListener.register();
        }
        if (brewingProject != null) {
            brewingProject.markItemsReady();
        }

        // Last, so other plugins only see the API once everything it reads is loaded.
        api = new SkysongItemsApiImpl(this);
        api.register(getServer().getServicesManager(), this);
    }

    /**
     * Grants or revokes the player's item permissions right away, after the plugin
     * itself changes a permission item they hold. Does nothing without LuckPerms.
     */
    public void syncPermissionItems(Player player) {
        if (permissionItemListener != null) {
            permissionItemListener.syncNow(player);
        }
    }

    /** Re-reads the ids of the saved items, subfolders included, for /si loaditem. */
    public void fixFilePaths(){
        Constants.itemFileNames.clear();
        Constants.itemFileNames.addAll(ItemTemplateRepository.savedItemIds());
    }

    @Override
    public void onDisable() {
        // First, so other plugins stop using the API before anything it reads shuts down.
        if (api != null) {
            api.unregister(getServer().getServicesManager());
            api = null;
        }
        if (permissionItemListener != null) {
            permissionItemListener.revokeOnlinePlayers();
        }
        // Let the queries already submitted finish before their connections are closed.
        if (bookQueries != null) {
            bookQueries.shutdown();
        }
        if (database != null) {
            database.close();
        }
    }

    /**
     * Replaces the database connection pool with one built from config.yml and creates the
     * books table, then runs {@code whenDone} on the main thread. Queries submitted before
     * this call still run on the old pool; it is closed once they have finished.
     */
    public void InitializeDatabase(Runnable whenDone){
        Database previous = database;
        database = new Database(DatabaseSettings.fromConfig(getConfig()));
        bookQueries.submit(books -> {
                    if (previous != null) {
                        previous.close();
                    }
                    books.createTable();
                    return null;
                },
                ignored -> {
                    getLogger().info("Created the book_descriptions table");
                    whenDone.run();
                },
                e -> {
                    Bukkit.getLogger().warning("[SkysongItems] Unable to connect to database and create tables!");
                    e.printStackTrace();
                    whenDone.run();
                });
    }
}
