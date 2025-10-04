package com.github.bradleywoodrs.mangologic;

import com.github.bradleywoodrs.mangologic.Listeners.MobSpawnListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Mangologic extends JavaPlugin{
    MobSpawnListener mobSpawnListener;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        mobSpawnListener.setMaxPerChunk(config.getInt("max-mobs-per-chunk", 15));
        getServer().getPluginManager().registerEvents(mobSpawnListener, this);
    }

}
