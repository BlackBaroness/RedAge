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
import ru.baronessdev.personal.redage.redagemain.AdminACF;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class RTP extends JavaPlugin implements CommandExecutor {

    private final HashMap<Player, Long> coolDownMap = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("rtp").setExecutor(this);
        AdminACF.registerSimpleAdminCommand("rtp", "- сбрасывает кулдауны rtp", ((sender, args) -> {
            coolDownMap.clear();
            sender.sendMessage("Кд сброшены");
            return true;
        }));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player p = (Player) sender;
        long need = needTime(p);
        if (needTime(p) != 0) {
            long last = TimeUnit.MILLISECONDS.toSeconds(need);
            RedAge.say(p, String.format("§fВы сможете телепортироваться через: §c%d секунд", last));
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

        RedAge.say(p, "Телепортируем вас...");
        p.teleport(loc);

        coolDownMap.put(p, System.currentTimeMillis());
        return true;
    }

    private long needTime(Player p) {
        long x = coolDownMap.getOrDefault(p, 0L);
        if (x == 0) return 0;

        long y = 20000;
        long z = System.currentTimeMillis();

        return (z - y > x) ? 0 : ~(z - (x + y));
    }
}
