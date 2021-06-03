package ru.baronessdev.personal.redage.redagemain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    List<Player> chatCooldown = new ArrayList<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (chatCooldown.contains(p)) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "Подождите, прежде чем писать новое сообщение!");
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(RedAge.getInstance(), () -> chatCooldown.remove(p), 15);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        try {
            String chest;
            switch (RedAge.formatLocation(e.getClickedBlock().getLocation())) {
                case "spawn # -24 # 54 # -7": {
                    chest = "donate";
                    break;
                }
                case "spawn # -24 # 54 # -13": {
                    chest = "money";
                    break;
                }
                default:
                    return;
            }

            String action;
            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK: {
                    action = "open";
                    break;
                }
                case LEFT_CLICK_BLOCK: {
                    action = "preview";
                    break;
                }
                default:
                    return;
            }

            e.setCancelled(true);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "crazycrates " + action + " " + chest + " " + e.getPlayer().getName()
            );
        } catch (NullPointerException ignored) {
        }
    }
}
