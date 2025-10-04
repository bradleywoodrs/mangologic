package com.github.bradleywoodrs.mangologic.Listeners;

import com.github.bradleywoodrs.mangologic.Discord.DiscordSync;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerJoinListener implements Listener {
    private final DiscordSync discordSync;

    public PlayerJoinListener(DiscordSync discordSync) {
        this.discordSync = discordSync;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        discordSync.SyncPlayer(e.getPlayer());
    }
}
