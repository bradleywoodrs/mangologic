package com.github.bradleywoodrs.mangologic.Listeners;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.Expansion;
import combatlogx.expansion.newbie.helper.NewbieHelperExpansion;
import combatlogx.expansion.newbie.helper.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PVPToggleListener implements Listener {

    private final Plugin plugin;

    private final Map<Block, UUID> lavaPlaced = new ConcurrentHashMap<>();
    private final Map<Block, UUID> firePlaced = new ConcurrentHashMap<>();
    private final Map<Block, UUID> magmaPlaced = new ConcurrentHashMap<>();
    private final Map<Block, UUID> crystalOwners = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> tntOwners = new ConcurrentHashMap<>();
    private final Map<Block, Long> lavaTimestamps = new ConcurrentHashMap<>();
    private final Map<Block, Long> fireTimestamps = new ConcurrentHashMap<>();
    private final Map<Block, Long> magmaTimestamps = new ConcurrentHashMap<>();
    private final Map<Block, Long> crystalTimestamps = new ConcurrentHashMap<>();
    private final Map<Integer, Long> tntTimestamps = new ConcurrentHashMap<>();

    private static final long TRACK_TICKS = 20L * 30;
    private final ProtectionManager protectionManager;
    public PVPToggleListener(Plugin plugin) {
        this.plugin = plugin;

        ICombatLogX clx = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        ProtectionManager pm = null;
        if (clx != null) {
            Optional<Expansion> expansion = clx.getExpansionManager().getExpansion("NewbieHelperExpansion");
            if (expansion.isPresent() && expansion.get() instanceof NewbieHelperExpansion nhe) {
                pm = nhe.getProtectionManager();
            }
        }
        this.protectionManager = pm;

        startCleanupTask();
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                removeExpired(lavaPlaced, lavaTimestamps, now);
                removeExpired(firePlaced, fireTimestamps, now);
                removeExpired(magmaPlaced, magmaTimestamps, now);
                removeExpired(crystalOwners, crystalTimestamps, now);
                removeExpired(tntOwners, tntTimestamps, now);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private <K> void removeExpired(Map<K, UUID> map, Map<K, Long> timestamps, long now) {
        long expiry = TRACK_TICKS * 50;
        timestamps.entrySet().removeIf(entry -> {
            if (now - entry.getValue() >= expiry) {
                map.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onBucketUse(PlayerBucketEmptyEvent e) {
        if (e.getBucket() != Material.LAVA_BUCKET) return;

        Block placed = e.getBlockClicked().getRelative(e.getBlockFace());

        lavaPlaced.put(placed, e.getPlayer().getUniqueId());
        lavaTimestamps.put(placed, System.currentTimeMillis());

        firePlaced.put(placed, e.getPlayer().getUniqueId());
        fireTimestamps.put(placed, System.currentTimeMillis());
    }



    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();

        if (block.getType() == Material.TNT) {
            int key = block.hashCode();
            tntOwners.put(key, e.getPlayer().getUniqueId());
            tntTimestamps.put(key, System.currentTimeMillis());
        }

        Material item = e.getItemInHand() != null ? e.getItemInHand().getType() : null;
        if (item == Material.FLINT_AND_STEEL || item == Material.FIRE_CHARGE) {
            firePlaced.put(block, e.getPlayer().getUniqueId());
            fireTimestamps.put(block, System.currentTimeMillis());
        }

        if (block.getType() == Material.MAGMA_BLOCK) {
            magmaPlaced.put(block, e.getPlayer().getUniqueId());
            magmaTimestamps.put(block, System.currentTimeMillis());
        }

        if (block.getType() == Material.END_CRYSTAL) {
            crystalOwners.put(block, e.getPlayer().getUniqueId());
            crystalTimestamps.put(block, System.currentTimeMillis());
        }
    }


    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getPlayer() == null) return;

        Block block = e.getBlock();
        firePlaced.put(block, e.getPlayer().getUniqueId());
        fireTimestamps.put(block, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.FLINT_AND_STEEL && e.getItem().getType() != Material.FIRE_CHARGE) return;

        Block target = e.getClickedBlock() != null ? e.getClickedBlock().getRelative(e.getBlockFace()) : null;
        if (target != null) {
            firePlaced.put(target, e.getPlayer().getUniqueId());
            fireTimestamps.put(target, System.currentTimeMillis());
        }
    }


    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed tnt)) return;
        Block below = tnt.getLocation().getBlock();
        UUID owner = tntOwners.get(below.hashCode());
        if (owner != null) tntOwners.put(tnt.getEntityId(), owner);
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause != EntityDamageEvent.DamageCause.LAVA
                && cause != EntityDamageEvent.DamageCause.FIRE
                && cause != EntityDamageEvent.DamageCause.FIRE_TICK
                && cause != EntityDamageEvent.DamageCause.HOT_FLOOR) return;

        boolean cancelled = false;

        Block block = victim.getLocation().getBlock();
        List<Block> blocks = blocksToCheck(block);

        for (Block b : blocks) {
            UUID ownerId = null;

            if (cause == EntityDamageEvent.DamageCause.LAVA) ownerId = lavaPlaced.get(b);
            else if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK) ownerId = firePlaced.get(b);
            else if (cause == EntityDamageEvent.DamageCause.HOT_FLOOR) ownerId = magmaPlaced.get(b);

            if (ownerId != null) {
                Player owner = Bukkit.getPlayer(ownerId);
                if (owner != null && !isPvPAllowed(owner, victim)) {
                    cancelled = true;
                    break;
                }
            }
        }

        if (cancelled) e.setCancelled(true);
    }




    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Player victim = e.getEntity() instanceof Player p ? p : null;
        if (victim == null) return;

        if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter) {
            if (!isPvPAllowed(shooter, victim)) e.setCancelled(true);
            return;
        }

        if (e.getDamager() instanceof EnderCrystal crystal) {
            Block block = crystal.getLocation().getBlock();
            UUID ownerId = crystalOwners.get(block);
            Player owner = ownerId != null ? Bukkit.getPlayer(ownerId) : null;
            if (owner != null && !isPvPAllowed(owner, victim)) e.setCancelled(true);
            return;
        }

        if (e.getDamager() instanceof TNTPrimed tnt) {
            UUID ownerId = tntOwners.get(tnt.getEntityId());
            Player owner = ownerId != null ? Bukkit.getPlayer(ownerId) : null;
            if (owner != null && !isPvPAllowed(owner, victim)) e.setCancelled(true);
        }
    }


    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Entity ent = e.getEntity();
        if (!(ent instanceof TNTPrimed || ent instanceof EnderCrystal)) return;

        UUID ownerId = ent instanceof TNTPrimed tnt ? tntOwners.get(tnt.getEntityId())
                : crystalOwners.get(ent.getLocation().getBlock());

        Player owner = ownerId != null ? Bukkit.getPlayer(ownerId) : null;
        if (owner == null) return;

        for (Entity near : ent.getNearbyEntities(5,5,5)) {
            if (near instanceof Player victim && !isPvPAllowed(owner, victim)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private List<Block> blocksToCheck(Block center) {
        List<Block> blocks = new ArrayList<>(7);
        blocks.add(center);
        blocks.add(center.getRelative(1, 0, 0));
        blocks.add(center.getRelative(-1, 0, 0));
        blocks.add(center.getRelative(0, 0, 1));
        blocks.add(center.getRelative(0, 0, -1));
        blocks.add(center.getRelative(0, 1, 0));
        blocks.add(center.getRelative(0, -1, 0));
        return blocks;
    }

    private boolean isPvPAllowed(Player attacker, Player victim) {
        if (protectionManager == null) return false;
        return !protectionManager.isProtected(attacker) && !protectionManager.isProtected(victim);
    }
}
