package ru.baronessdev.personal.redage.combat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public final class Combat extends JavaPlugin implements Listener {

    HashMap<Player, Integer> timers = new HashMap<>();
    HashMap<Player, BossBar> bars = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        addCombatMode((Player) e.getDamager());
        addCombatMode((Player) e.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (timers.containsKey(p)) {
            Arrays.stream(p.getInventory().getStorageContents())
                    .filter(Objects::nonNull)
                    .forEach(it -> p.getLocation().getWorld().dropItem(p.getLocation(), it));
            Arrays.stream(p.getInventory().getArmorContents())
                    .filter(Objects::nonNull)
                    .forEach(it -> p.getLocation().getWorld().dropItem(p.getLocation(), it));

            p.getInventory().clear();
            p.setHealth(0.0);

            timers.remove(p);
            bars.remove(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        timers.remove(e.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (timers.containsKey(p)) {
            RedAge.say(p, "Вы не можете использовать команды во время боя.");
            e.setCancelled(true);
        }
    }

    private void addCombatMode(Player p) {
        if (timers.containsKey(p)) {
            timers.put(p, 20);
            bars.get(p).setProgress(1.0);
            return;
        }

        BossBar bossBar = Bukkit.createBossBar("Вы в бою!", BarColor.YELLOW, BarStyle.SEGMENTED_20);
        bossBar.setProgress(1.0);

        timers.put(p, 20);
        bars.put(p, bossBar);

        bossBar.setVisible(true);
        bossBar.addPlayer(p);

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    return;
                }

                BossBar b = bars.get(p);

                if (!timers.containsKey(p)) {
                    b.removeAll();
                    timers.remove(p);
                    bars.remove(p);
                    cancel();
                    return;
                }

                if (timers.get(p) == 1) {
                    b.removeAll();
                    timers.remove(p);
                    bars.remove(p);
                    cancel();

                    RedAge.say(p, ChatColor.GREEN + "Вы вышли из режима боя.");
                    return;
                }

                int timer = timers.get(p) - 1;
                timers.put(p, timer);

                b.setProgress(timer / 20.0);
            }
        };

        r.runTaskTimer(RedAge.getInstance(), 0, 20);
        RedAge.say(p, ChatColor.RED + "Не выходите из игры до окончания таймера!");
    }
}
