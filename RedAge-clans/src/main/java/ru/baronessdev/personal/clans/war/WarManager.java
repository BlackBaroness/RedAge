package ru.baronessdev.personal.clans.war;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ru.baronessdev.personal.clans.objects.Clan;
import ru.baronessdev.personal.clans.util.ThreadUtil;
import ru.baronessdev.personal.clans.Clans;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WarManager {

    private static War w;
    HashMap<UUID, Integer> warCount = new HashMap<>();
    private static final List<Block> glass = new ArrayList<>();

    public static void scheduleWar(Clan first, Clan second, WarType type) {
        RedAge.broadcast("Кланы " + ChatColor.RED + first.getName() + ChatColor.WHITE + " и " + ChatColor.RED + second.getName() + ChatColor.WHITE + " готовятся к войне! Арена будет открыта через " + ChatColor.RED + "10" + ChatColor.WHITE + " минут.");

        w = new War(UUID.randomUUID(), type, first.getUuid(), second.getUuid(), new ArrayList<>(), new ArrayList<>());
        final UUID warUUID = w.getUuid();
        ThreadUtil.runLater(600, () -> {
            // начало войны
            RedAge.broadcast("Война между кланами " + ChatColor.RED + first.getName() + ChatColor.WHITE + " и " + ChatColor.RED + second.getName() + ChatColor.WHITE + " начинается!");

            // создание стекла
            glass.forEach(block -> block.setType(Material.GLASS));

            // телепортация игроков 1 клана
            for (int i = 1; i < w.getFirstClanPlayers().size(); i++) {
                Location l = RedAge.formatLocation(Clans.plugin.getConfig().getString("settings.1." + i));
                Player p = w.getFirstClanPlayers().get(i - 1);

                if (!p.isOnline()) continue;
                p.teleport(l);
                RedAge.say(p, "Схватка начнётся через " + ChatColor.RED + "15" + ChatColor.WHITE + " секунд.");
            }

            // телепортация игроков 2 клана
            for (int i = 1; i < w.getSecondClanPlayers().size(); i++) {
                Location l = RedAge.formatLocation(Clans.plugin.getConfig().getString("settings.2." + i));
                Player p = w.getSecondClanPlayers().get(i - 1);

                if (!p.isOnline()) continue;
                p.teleport(l);
                RedAge.say(p, "Схватка начнётся через " + ChatColor.RED + "15" + ChatColor.WHITE + " секунд.");
            }

            List<Player> allPlayers = new ArrayList<>();
            allPlayers.addAll(w.getFirstClanPlayers());
            allPlayers.addAll(w.getFirstClanPlayers());

            for (int i = 15; i > 0; i--) {

            }

            ThreadUtil.runLater(15, () -> {

            });

            // защита от срабатывания к уже закончившейся войне
            if (w != null && w.getUuid().equals(warUUID)) {

            }
        });
    }

    public static void finishWar(Clan winner) {

    }

    public static War getWar(Clan c) {
        if (w != null && (
                w.getFirstClan().equals(c.getUuid()) || w.getSecondClan().equals(c.getUuid()))
        ) return w;
        return null;
    }

    public static void setup() {
        for (int i = 1; i < 10; i++) {
            Location l = RedAge.formatLocation(Clans.plugin.getConfig().getString("settings.1." + i));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() + 1, l.getBlockY(), l.getBlockZ() + 1));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() - 1, l.getBlockY(), l.getBlockZ() - 1));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() + 1, l.getBlockY() + 1, l.getBlockZ() + 1));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() - 1, l.getBlockY() + 1, l.getBlockZ() - 1));
        }

        for (int i = 1; i < 10; i++) {
            Location l = RedAge.formatLocation(Clans.plugin.getConfig().getString("settings.2." + i));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() + 1, l.getBlockY(), l.getBlockZ() + 1));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() - 1, l.getBlockY(), l.getBlockZ() - 1));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() + 1, l.getBlockY() + 1, l.getBlockZ() + 1));
            glass.add(l.getWorld().getBlockAt(l.getBlockX() - 1, l.getBlockY() + 1, l.getBlockZ() - 1));
        }
    }

    public static boolean warExists() {
        return w != null;
    }
}
