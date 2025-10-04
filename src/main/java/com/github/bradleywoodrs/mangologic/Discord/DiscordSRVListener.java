package com.github.bradleywoodrs.mangologic.Discord;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DiscordSRVListener {
    private final DiscordSync discordSync;
    private final Plugin plugin;

    public DiscordSRVListener(Plugin plugin, DiscordSync discordSync) {
        this.plugin = plugin;
        this.discordSync = discordSync;
    }

    @Subscribe
    public void discordReadyEvent(DiscordReadyEvent event) {
        DiscordUtil.getJda().addEventListener(new JDAListener(plugin));
    }

    @Subscribe
    public void accountsLinked(AccountLinkedEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            discordSync.SyncPlayer((Player) e.getPlayer());
        });
    }

    @Subscribe
    public void accountUnlinked(AccountUnlinkedEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            discordSync.SyncPlayer((Player) e.getPlayer());
        });
    }

}