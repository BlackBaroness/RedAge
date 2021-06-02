package ru.baronessdev.personal.redage.redagemain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class RedAgeListener implements Listener {

    List<Player> warpCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        warpCooldown.remove(p);
        warpCooldown.add(p);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RedAge.getInstance(),
                () -> warpCooldown.remove(p), 10 * 20);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (warpCooldown.contains(p) && e.getMessage().equals("/warp pvp")) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "Варп доступен только через 10 секунд после смерти.");
        }
    }
}
