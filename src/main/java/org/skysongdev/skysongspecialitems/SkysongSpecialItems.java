package org.skysongdev.skysongspecialitems;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.skysongdev.skysongspecialitems.commands.SaveBook;
import org.skysongdev.skysongspecialitems.commands.SkysongItems;
import org.skysongdev.skysongspecialitems.database.Database;
import org.skysongdev.skysongspecialitems.events.DisableItemUse;
import org.skysongdev.skysongspecialitems.events.OpenDescRightClick;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SkysongSpecialItems extends JavaPlugin {

    private static SkysongSpecialItems plugin;
    private Database database;
    public Database getDatabase() {
        return database;
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        InitializeDatabase();

        super.onEnable();
        plugin = this;
        getCommand("skysongitems").setExecutor(new SkysongItems(this));
        getServer().getPluginManager().registerEvents(new DisableItemUse(), this);
        getServer().getPluginManager().registerEvents(new OpenDescRightClick(this), this);
    }

    public static SkysongSpecialItems getPlugin(){
        return plugin;
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
