package ru.baronessdev.personal.redage.rtp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class RTP extends JavaPlugin implements CommandExecutor {

    private final HashMap<Player, Integer> coolDownMap = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("rtp").setExecutor(this);
        RedAge.registerAdminCommand("rtp", "- сбрасывает кулдауны rtp", ((sender, args) -> {
            coolDownMap.clear();
            sender.sendMessage("Кд сброшены");
            return true;
        }));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player p = (Player) sender;
        if (coolDownMap.containsKey(p)) {
            p.sendMessage("§f[§cRed§fAge] §7Вы сможете телепортироваться через §с" + coolDownMap.get(p) + " §7секунд.");
            return true;
        }

        World world = Bukkit.getWorld("world");
        Location loc = null;

        boolean normal = false;
        while (!normal) {
            loc = world.getHighestBlockAt(ThreadLocalRandom.current().nextInt(20000), ThreadLocalRandom.current().nextInt(20000)).getLocation();
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

        p.sendMessage("§f[§cRed§fAge] §7Телепортируем вас...");
        p.teleport(loc);

        coolDownMap.put(p, 600);
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (!coolDownMap.containsKey(p)) {
                    cancel();
                    return;
                }

                if (coolDownMap.get(p) <= 0) {
                    coolDownMap.remove(p);
                    cancel();
                    return;
                }

                coolDownMap.put(p, coolDownMap.get(p) - 1);
            }
        };
        r.runTaskTimer(this, 0, 20);
        return true;
    }
}
