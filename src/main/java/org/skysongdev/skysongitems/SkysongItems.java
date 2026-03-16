package org.skysongdev.skysongitems;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.skysongdev.skysongitems.commands.SkysongItemsCommand;
import org.skysongdev.skysongitems.commands.lore.Updater;
import org.skysongdev.skysongitems.commands.lore.funcionaldesc.FunctionalDesc;
import org.skysongdev.skysongitems.commands.lore.physicaldesc.PhysicalDesc;
import org.skysongdev.skysongitems.commands.lore.skilldesc.SkillDesc;
import org.skysongdev.skysongitems.database.Database;
import org.skysongdev.skysongitems.database.Pinger;
import org.skysongdev.skysongitems.integration.TBPSkysongIntegration;
import org.skysongdev.skysongitems.integration.listeners.SkysongItemReloader;
import org.skysongdev.skysongitems.listeners.*;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.io.File;
import java.sql.SQLException;

public final class SkysongItems extends JavaPlugin {
    private File ItemOutputDirectory;
    private static SkysongItems plugin;
    private Database database;
    private Updater updater;
    private Utils utils;
    private MiniMessage miniMessage = MiniMessage.miniMessage();
    private LuckPerms luckPerms;
    private TheBrewingProjectApi tbp;
    private TBPSkysongIntegration tbpIntegration = new TBPSkysongIntegration();

    public static SkysongItems getPlugin(){
        if(plugin == null){
            plugin = new SkysongItems();
        }
        return plugin;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public TheBrewingProjectApi getTbp() {
        return tbp;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public Utils getUtils() {
        return utils;
    }

    public Updater getUpdater() {
        return updater;
    }

    private SkysongItemsCommand mainCommand;

    public SkysongItemsCommand getMainCommand() {
        return mainCommand;
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public void onLoad(){
        try {
            tbpIntegration.loadIfPossible();
        } catch (NoClassDefFoundError ignored){
            Bukkit.getLogger().warning("The Brewing Project not found, skipping integration.");
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        utils = new Utils();
        tbp = TheBrewingProject.getInstance();
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
        getServer().getPluginManager().registerEvents(new SkysongItemReloader(), this);
        getServer().getPluginManager().registerEvents(new ConsumptionListener(), this);
        getCommand("physdesc").setExecutor(new PhysicalDesc());
        getCommand("funcdesc").setExecutor(new FunctionalDesc());
        getCommand("skilldesc").setExecutor(new SkillDesc());

        getServer().getScheduler().runTaskTimer(this, new Pinger(), 0L, 6000L);

        utils.setupTags();
        utils.setupProperties();
        utils.InitializeMaterialPermissions();
        fixFilePaths();


        luckPerms = LuckPermsProvider.get();
        tbpIntegration.registerComplete();
    }

    public void fixFilePaths(){
        ItemOutputDirectory = new File(getDataFolder() + File.separator + "GeneratedItems");

        if (!ItemOutputDirectory.exists()) {
            ItemOutputDirectory.mkdir();
        }
        File folder = Utils.getOrGenerateOutputFile();
        File[] files = folder.listFiles();
        Constants.itemFileNames.clear();
        if(files != null){
            for(File file : files){
                Constants.itemFileNames.add(file.getName().replace(".yml", ""));
            }
        }
    }

    public void InitializeDatabase(){
        try{
            this.database = new Database(
                    getConfig().getString("database.host"),
                    getConfig().getString("database.port"),
                    getConfig().getString("database.user"),
                    getConfig().getString("database.password"),
                    getConfig().getString("database.database_name"));
            database.initializeDatabase();
        }catch(SQLException e){
            Bukkit.getLogger().warning("[SkysongItems] Unable to connect to database and create tables!");
            e.printStackTrace();
        }
    }
}
