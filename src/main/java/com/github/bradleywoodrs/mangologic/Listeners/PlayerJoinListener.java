package com.github.bradleywoodrs.mangologic.Listeners;

import com.github.bradleywoodrs.mangologic.Discord.DiscordSync;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;


public class PlayerJoinListener implements Listener {
    private final DiscordSync discordSync;
    private Plugin plugin;
    public PlayerJoinListener(DiscordSync discordSync, Plugin plugin) {
        this.discordSync = discordSync;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            discordSync.SyncPlayer(e.getPlayer());
        });
    }
}
