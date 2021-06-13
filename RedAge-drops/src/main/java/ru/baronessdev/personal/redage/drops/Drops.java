package ru.baronessdev.personal.redage.drops;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.baronessdev.personal.redage.redagemain.AdminACF;
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
    private List<ItemStack> items = new ArrayList<>();
    private long timer;
    private BossBar bossBar;
    private final List<Player> cooldown = new ArrayList<>();

    @Override
    public void onEnable() {
        saveResource("items.yml", true);
        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("drop").setExecutor(((sender, command, label, args) -> {
            if (chest != null) {
                Player p = (Player) sender;

                Location loc = null;
                World world = chest.getWorld();

                boolean normal = false;
                while (!normal) {
                    loc = world.getHighestBlockAt(ThreadLocalRandom.current().nextInt(chest.getX() - 10, chest.getX() + 10), ThreadLocalRandom.current().nextInt(chest.getZ() - 10, chest.getZ() + 10)).getLocation();
                    Block block;

                    boolean checked = false;
                    int i = 1;
                    while (!checked) {
                        block = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - i, loc.getBlockZ());
                        if (block.getType() != Material.AIR) {
                            checked = true;
                            if (block.getType() != Material.STATIONARY_WATER && block.getType() != Material.WATER && block.getType() != Material.LAVA)
                                normal = true;
                        } else i++;
                    }
                }

                p.teleport(loc);
                cooldown.add(p);
            }
            return true;
        }));

        AdminACF.addCommand("drops", "- управление дропами", new DropsCommand());
    }

    private void spawn() {
        World world = Bukkit.getWorld("world");
        Location loc = null;
        items = new ArrayList<>();

        boolean normal = false;
        while (!normal) {
            loc = world.getHighestBlockAt(ThreadLocalRandom.current().nextInt(-3000, 3000), ThreadLocalRandom.current().nextInt(-3000, 3000)).getLocation();
            Block block;

            boolean checked = false;
            int i = 1;
            while (!checked) {
                block = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - i, loc.getBlockZ());
                if (block.getType() != Material.AIR) {
                    checked = true;
                    if (block.getType() != Material.STATIONARY_WATER && block.getType() != Material.WATER && block.getType() != Material.LAVA)
                        normal = true;
                } else i++;
            }
        }

        chest = world.getBlockAt(loc);
        chest.setType(Material.CHEST);

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

                RedAge.log("создаю енчант " + enchantment + ":" + level);
                item.addUnsafeEnchantment(Enchantment.getByName(enchantment), level);
            }

            items.add(item);
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

                    items.forEach(i -> ((Chest) chest.getState()).getInventory().addItem(i));

                    bossBar.setProgress(1.0);
                    bossBar.setColor(BarColor.GREEN);
                    bossBar.setTitle("Дроп разблокирован!");
                    Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
                    ThreadUtil.runLater(3, () -> {
                        bossBar.removeAll();
                        bossBar = null;
                    });

                    chest = null;
                    Bukkit.getScheduler().runTaskLater(RedAge.getInstance(), cooldown::clear, 20 * 30);

                    cancel();
                    return;
                }

                bossBar.setProgress(timer / 300.0);
                bossBar.setTitle("ОПАСНО! | До разблокировки дропа: " + timer + " сек. | /drop");
                Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
            }
        };

        RedAge.broadcast("Появился сундук с ценным лутом! Введи /drop, чтобы телепортироваться.");
        RedAge.broadcast("§c§l[!] Осторожно! Вас могут убить.");

        runnable.runTaskTimer(RedAge.getInstance(), 0, 20);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock().equals(chest)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Дроп заблокирован.");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (cooldown.contains(e.getPlayer()) && !e.getPlayer().hasPermission("admin")) {
            e.getPlayer().sendMessage(ChatColor.RED + "Вы не можете использовать команды до разблокировки дропа");
            e.getPlayer().sendMessage(ChatColor.RED + "                     и в течении 30с после разблокировки.");

            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (chest != null && e.blockList().contains(chest)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        cooldown.remove(e.getEntity());
    }

    @SuppressWarnings("unused")
    @CommandAlias("redage")
    @Subcommand("drops")
    @CommandPermission("admin")
    class DropsCommand extends BaseCommand {

        @CatchUnknown
        @Default
        public void unknown(CommandSender sender) {
            help(sender);
        }

        @Subcommand("start")
        public void start(CommandSender sender) {
            if (chest != null) {
                sender.sendMessage("Дроп уже создан");
                return;
            }

            spawn();
        }

        @Subcommand("reset")
        public void reset(CommandSender sender) {
            if (chest == null) {
                sender.sendMessage("Дроп не создан");
                return;
            }

            timer = 4;
        }

        @Subcommand("cd")
        public void cd(CommandSender sender) {
            if (chest == null) {
                sender.sendMessage("Дроп не создан");
                return;
            }

            cooldown.clear();
        }


        private void help(CommandSender s) {
            s.sendMessage("§a/redage drops start§f - досрочно создаёт дроп, если ещё не создан");
            s.sendMessage("§a/redage drops reset§f - досрочно снимает лок с дропа, если создан");
            s.sendMessage("§a/redage drops cd§f - обнуляет кулдауны");
        }
    }
}
