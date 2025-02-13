package org.skysongdev.skysongitems.database;

import org.bukkit.Bukkit;
import org.skysongdev.skysongitems.SkysongItems;

public class Pinger implements Runnable {

    @Override
    public void run() {
        try{
        SkysongItems.getPlugin().getDatabase().Ping();
        }catch(Exception e) {
            Bukkit.getLogger().info("Error while pinging database");
            e.printStackTrace();
        }
        Bukkit.getLogger().info("Pinger task is running");
    }
}
