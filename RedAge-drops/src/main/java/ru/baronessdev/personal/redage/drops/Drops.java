package ru.baronessdev.personal.redage.drops;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Drops extends JavaPlugin implements Listener {

    private Block chest;
    private long timer;
    private BossBar bossBar;
    private final List<Player> cooldown = new ArrayList<>();

    @Override
    public void onEnable() {
        saveResource("items.yml", false);
        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("drop").setExecutor(((sender, command, label, args) -> {
            if (chest != null) {
                Player p = (Player) sender;
                p.teleport(chest.getLocation());
                cooldown.add(p);
            }
            return true;
        }));

        RedAge.registerAdminCommand("drops", "- управление дропами", (sender, args) -> {
            if (args.length == 0) {
                adminHelp(sender);
                return true;
            }

            switch (args[0]) {

                case "start": {
                    if (chest != null) {
                        sender.sendMessage("Дроп уже создан");
                        return true;
                    }

                    start();
                    return true;
                }

                case "reset": {
                    if (chest == null) {
                        sender.sendMessage("Дроп не создан");
                        return true;
                    }

                    timer = 4;
                    return true;
                }

                default: {
                    adminHelp(sender);
                    return true;
                }
            }
        });
    }

    private void start() {
        World world = Bukkit.getWorld("world");
        Location loc = world.getHighestBlockAt(ThreadLocalRandom.current().nextInt(20000), ThreadLocalRandom.current().nextInt(20000)).getLocation();

        loc.setY(loc.getY() + 1);
        chest = world.getBlockAt(loc);
        chest.setType(Material.CHEST);
        Chest asStorage = (Chest) chest.getState();

        // чтение
        List<String> keys = new ArrayList<>();
        try {
            Files.lines(Path.of(getDataFolder() + File.separator + "items.yml")).forEach(keys::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // случайная сборка
        int max = ThreadLocalRandom.current().nextInt(3, 7);

        for (int i = 0; i < max; i++) {
            String key = keys.get(ThreadLocalRandom.current().nextInt(keys.size()));

            String[] split = key.split(" ");
            String s = split[0];
            Material material = Material.getMaterial(s);
            if (material == null) {
                RedAge.log("Не удалось получить материал " + s);
                i--;
                continue;
            }

            ItemStack item = new ItemStack(material, Integer.parseInt(split[1]));
            for (int j = 2; j < split.length; j++) {
                String enchantment = split[j].split(":")[0];
                int level = Integer.parseInt(split[j].split(":")[1]);

                System.out.println("создаю енчант " + enchantment + ":" + level);
                item.addUnsafeEnchantment(Enchantment.getByName(enchantment), level);
            }

            asStorage.getInventory().addItem(item);
        }

        bossBar = Bukkit.getServer().createBossBar("Загрузка...", BarColor.RED, BarStyle.SOLID);
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        timer = 300;
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                timer--;
                if (timer <= 0) {

                    bossBar.setProgress(1.0);
                    bossBar.setColor(BarColor.GREEN);
                    bossBar.setTitle("Дроп разблокирован!");
                    Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
                    ThreadUtil.runLater(3, () -> {
                        bossBar.removeAll();
                        bossBar = null;
                    });

                    chest = null;
                    cooldown.clear();
                    cancel();
                    return;
                }

                bossBar.setProgress(timer / 300.0);
                bossBar.setTitle("До разблокировки дропа: " + timer + " сек.");
                Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
            }
        };
        RedAge.broadcast("Появился сундук с ценным лутом! Введи /drop, чтобы телепортироваться.");
        runnable.runTaskTimer(RedAge.getInstance(), 0, 20);
    }

    private void adminHelp(CommandSender s) {
        s.sendMessage("§a/redage drops start§f - досрочно создаёт дроп, если ещё не создан");
        s.sendMessage("§a/redage drops reset§f - досрочно снимает лок с дропа, если создан");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock().equals(chest)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Дроп заблокирован.");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (cooldown.contains(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Вы не можете телепортироваться до разблокировки дропа.");
            e.setCancelled(true);
        }
    }


}
