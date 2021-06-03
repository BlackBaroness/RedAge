package ru.baronessdev.personal.redage.lobby;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.paid.auth.api.events.AuthPlayerLoginEvent;
import ru.baronessdev.paid.auth.api.events.AuthPlayerSessionSavedEvent;

import java.util.ArrayList;
import java.util.List;

public final class Lobby extends JavaPlugin implements Listener {

    private final List<Player> commandMuted = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.hidePlayer(this, p);
            p.hidePlayer(this, player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthPlayerLogin(AuthPlayerLoginEvent e) {
        process(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthPlayerLogin(AuthPlayerSessionSavedEvent e) {
        process(e.getPlayer());
    }

    private void process(Player p) {
        commandMuted.add(p);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (p.isOnline()) p.kickPlayer("Пожалуйста, перезайдите через минуту.");
            commandMuted.remove(p);
        }, 60);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        commandMuted.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(PlayerCommandPreprocessEvent e) {
        if (commandMuted.contains(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryMoveItem(PlayerMoveEvent e) {
        if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent e) {
        e.setCancelled(true);
    }
}
