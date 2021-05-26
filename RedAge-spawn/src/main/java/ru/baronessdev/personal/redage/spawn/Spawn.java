package ru.baronessdev.personal.redage.spawn;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Spawn extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.createWorld(new WorldCreator("spawn"));
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;


        Player p = (Player) e.getEntity();
        if (p.getLocation().getWorld().getName().equals("spawn")) {
            e.setCancelled(true);

            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                new BukkitRunnable() {
                    public void run() {
                        p.teleport(p.getWorld().getSpawnLocation());
                        p.setFallDistance(0F);

                        cancel();
                    }
                }.runTaskLater(this, 1L);
            }

            if (e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                p.teleport(p.getWorld().getSpawnLocation());
            }
        }
    }
}
