package com.github.bradleywoodrs.mangologic.Listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class MobSpawnListener implements Listener {
    private int maxPerChunk;
    private final Map<Long, Integer> mobCountByChunk = new HashMap<>();

    public int getMaxPerChunk() {
        return maxPerChunk;
    }

    public void setMaxPerChunk(int maxPerChunk) {
        this.maxPerChunk = maxPerChunk;
    }

    private long chunkKey(Chunk chunk) {
        return (((long) chunk.getX()) << 32) | (chunk.getZ() & 0xffffffffL);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player) return;

        Chunk chunk = event.getLocation().getChunk();
        long key = chunkKey(chunk);

        int count = mobCountByChunk.getOrDefault(key, 0);
        if (count >= maxPerChunk) {
            event.setCancelled(true);
            return;
        }

        mobCountByChunk.put(key, count + 1);
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;

        Chunk chunk = entity.getLocation().getChunk();
        long key = chunkKey(chunk);

        mobCountByChunk.computeIfPresent(key, (k, v) -> Math.max(0, v - 1));
    }
}
