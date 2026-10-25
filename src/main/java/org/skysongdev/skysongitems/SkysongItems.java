package org.skysongdev.skysongitems;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.skysongdev.skysongitems.commands.SkysongItemsCommand;
import org.skysongdev.skysongitems.commands.lore.LoreSection;
import org.skysongdev.skysongitems.commands.lore.LoreSectionCommand;
import org.skysongdev.skysongitems.commands.lore.Updater;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.Database;
import org.skysongdev.skysongitems.database.DatabaseSettings;
import org.skysongdev.skysongitems.integration.BrewingProjectIntegration;
import org.skysongdev.skysongitems.integration.LuckPermsIntegration;
import org.skysongdev.skysongitems.integration.SoftDependency;
import org.skysongdev.skysongitems.listeners.*;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemTemplateRepository;
import org.skysongdev.skysongitems.utils.MaterialPermissions;
import org.skysongdev.skysongitems.utils.PropertyRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

import java.io.File;
import java.sql.SQLException;
import java.util.Optional;

// Not final: MockBukkit loads a plugin through a generated subclass of its main class.
public class SkysongItems extends JavaPlugin {
    private File ItemOutputDirectory;
    private static SkysongItems plugin;
    private Database database;
    private Updater updater;
    private final PropertyRegistry propertyRegistry = new PropertyRegistry();
    private final TagRegistry tagRegistry = new TagRegistry();
    private final MaterialPermissions materialPermissions = new MaterialPermissions();
    private MiniMessage miniMessage = MiniMessage.miniMessage();
    // Each integration is null when its plugin is not installed.
    private LuckPermsIntegration luckPerms;
    private BrewingProjectIntegration brewingProject;

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

    /** The books stored in the database, for the book commands and bound-book right-clicks. */
    public BookDescriptionRepository getBookRepository() {
        return database.getBooks();
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

        InitializeDatabase();

        super.onEnable();
        mainCommand = new SkysongItemsCommand();
        updater = new Updater();
        getCommand("skysongitems").setExecutor(mainCommand);
        getServer().getPluginManager().registerEvents(new DisableItemUse(), this);
        getServer().getPluginManager().registerEvents(new OpenDescRightClick(this), this);
        getServer().getPluginManager().registerEvents(new UpdateItemListener(this), this);
        getServer().getPluginManager().registerEvents(new PermissionItemListener(), this);
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
        }
        if (brewingProject != null) {
            brewingProject.markItemsReady();
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
        if (database != null) {
            database.close();
        }
    }

    /** Replaces the database connection pool with one built from config.yml and creates the books table. */
    public void InitializeDatabase(){
        if (database != null) {
            database.close();
        }
        database = new Database(DatabaseSettings.fromConfig(getConfig()));
        try{
            database.getBooks().createTable();
            getLogger().info("Created the book_descriptions table");
        }catch(SQLException e){
            Bukkit.getLogger().warning("[SkysongItems] Unable to connect to database and create tables!");
            e.printStackTrace();
        }
    }
}
