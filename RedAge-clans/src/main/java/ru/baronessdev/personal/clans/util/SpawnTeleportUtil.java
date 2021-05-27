package ru.baronessdev.personal.clans.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpawnTeleportUtil {

    public static void tp(Player p) {
        if (!p.isOnline()) return;
        p.teleport(Bukkit.getWorld("spawn").getSpawnLocation());
    }
}
