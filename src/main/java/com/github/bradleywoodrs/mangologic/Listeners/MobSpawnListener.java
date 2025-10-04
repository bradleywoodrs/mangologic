package com.github.bradleywoodrs.mangologic.Listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnListener implements Listener {
    private int maxPerChunk;

    public int getMaxPerChunk() {
        return maxPerChunk;
    }

    public void setMaxPerChunk(int maxPerChunk) {
        this.maxPerChunk = maxPerChunk;
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Chunk chunk = event.getLocation().getChunk();
        int count = 0;
        for (Entity e : chunk.getEntities()) {
            if (e instanceof LivingEntity && !(e instanceof Player) && !(e instanceof Animals)) {
                count++;
                if (count >= maxPerChunk) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
