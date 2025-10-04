package com.github.bradleywoodrs.mangologic;

import com.github.bradleywoodrs.mangologic.Discord.DiscordSRVListener;
import com.github.bradleywoodrs.mangologic.Discord.DiscordSync;
import com.github.bradleywoodrs.mangologic.Listeners.MobSpawnListener;
import com.github.bradleywoodrs.mangologic.Listeners.PlayerJoinListener;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Mangologic extends JavaPlugin{
    DiscordSync discordSync = new DiscordSync(this);
    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        MobSpawnListener mobSpawnListener = new MobSpawnListener();
        mobSpawnListener.setMaxPerChunk(config.getInt("max-mobs-per-chunk"));
        getServer().getPluginManager().registerEvents(mobSpawnListener, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(discordSync, this), this);
        DiscordSRV.api.subscribe(discordSync.discordsrvListener);
        discordSync.setGuildid((String) config.get("guildid"));
        discordSync.setBoosterroleid((String) config.get("boosterroleid"));
    }

    @Override
    public void onDisable() {
        DiscordSRV.api.unsubscribe(discordSync.discordsrvListener);
    }
}
