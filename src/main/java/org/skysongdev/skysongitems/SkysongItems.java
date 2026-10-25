package org.skysongdev.skysongitems;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.skysongdev.skysongitems.commands.SkysongItemsCommand;
import org.skysongdev.skysongitems.commands.lore.LoreSection;
import org.skysongdev.skysongitems.commands.lore.LoreSectionCommand;
import org.skysongdev.skysongitems.commands.lore.Updater;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.BookQueries;
import org.skysongdev.skysongitems.database.Database;
import org.skysongdev.skysongitems.database.DatabaseSettings;
import org.skysongdev.skysongitems.integration.BrewingProjectIntegration;
import org.skysongdev.skysongitems.integration.LuckPermsIntegration;
import org.skysongdev.skysongitems.integration.SoftDependency;
import org.skysongdev.skysongitems.listeners.*;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.MaterialPermissions;
import org.skysongdev.skysongitems.utils.PermissionItemSync;
import org.skysongdev.skysongitems.utils.PropertyRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

import java.io.File;
import java.util.Optional;

// Not final: MockBukkit loads a plugin through a generated subclass of its main class.
public class SkysongItems extends JavaPlugin {
    private File ItemOutputDirectory;
    private static SkysongItems plugin;
    private Database database;
    private BookQueries bookQueries;
    private Updater updater;
    private final PropertyRegistry propertyRegistry = new PropertyRegistry();
    private final TagRegistry tagRegistry = new TagRegistry();
    private final MaterialPermissions materialPermissions = new MaterialPermissions();
    private MiniMessage miniMessage = MiniMessage.miniMessage();
    // Each integration is null when its plugin is not installed.
    private LuckPermsIntegration luckPerms;
    private BrewingProjectIntegration brewingProject;
    // Null when LuckPerms is not installed, as permission items then grant nothing.
    private PermissionItemListener permissionItemListener;

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

    /** The item properties configured in config.yml. */
    public PropertyRegistry getPropertyRegistry() {
        return propertyRegistry;
    }

    /** The item tags, built-in and configured in config.yml. */
    public TagRegistry getTagRegistry() {
        return tagRegistry;
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
        for (LoreSection section : LoreSection.values()) {
            getCommand(section.getCommandName()).setExecutor(new LoreSectionCommand(section));
        }

        tagRegistry.load(getConfig());
        propertyRegistry.load(getConfig(), getLogger());
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

    public void fixFilePaths(){
        ItemOutputDirectory = new File(getDataFolder() + File.separator + "GeneratedItems");

        if (!ItemOutputDirectory.exists()) {
            ItemOutputDirectory.mkdir();
        }
        File folder = ItemTemplateRepository.generatedItemsFolder();
        File[] files = folder.listFiles();
        Constants.itemFileNames.clear();
        if(files != null){
            for(File file : files){
                Constants.itemFileNames.add(file.getName().replace(".yml", ""));
            }
        }
    }

    @Override
    public void onDisable() {
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
