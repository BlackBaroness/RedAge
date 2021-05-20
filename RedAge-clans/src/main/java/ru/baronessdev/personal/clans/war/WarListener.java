package ru.baronessdev.personal.clans.war;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.baronessdev.personal.clans.Data;
import ru.baronessdev.personal.clans.obj.Clan;

import java.util.Arrays;

public class WarListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!p.getLocation().getWorld().getName().equals("warWorld")) return;
        Clan c = Data.getInstance().getClan(p);

        Player killer = p.getKiller();
        if (killer != null) Data.getInstance().addKillCount(killer);

        e.setDeathMessage(p.getName() + " погиб в бою. " + ChatColor.RED + "[" + ((c != null) ? c.getName() : "неизвестный клан") + "]");
        WarManager.finishWar(false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.getLocation().getWorld().getName().equals("warWorld")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + p.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (WarManager.w == null || WarManager.w.getTimer() != null) return;
        Clan c = WarManager.w.remove(e.getPlayer());
        if (c == null) return;

        WarManager.w.add(c.getUuid(), e.getPlayer());
        int last = WarManager.w.remove(c.getUuid(), e.getPlayer());
        c.broadcast(ChatColor.RED + e.getPlayer().getName() + ChatColor.RESET + " отказывается от войны, выйдя из игры [" + last + "/" + WarManager.w.getType().getPlayers() + "]");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuitInBattle(PlayerQuitEvent e) {
        if (WarManager.w != null && WarManager.w.getTimer() != null) {
            Player p = e.getPlayer();
            if (!p.getLocation().getWorld().getName().equals("warWorld")) return;
            Clan c = Data.getInstance().getClan(p);
            Arrays.stream(p.getInventory().getContents())
                    .forEach(i -> p.getLocation().getWorld().dropItem(p.getLocation(), i));
            p.getInventory().clear();
            e.setQuitMessage(p.getName() + " вышел во время боя. " + ChatColor.RED + "[" + ((c != null) ? c.getName() : "неизвестный клан") + "]");
            WarManager.finishWar(false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        try {
            if (e.getClickedInventory().getTitle().equals("Рейтинг кланов")) e.setCancelled(true);
        } catch (Exception ignored) {
        }
    }
}
