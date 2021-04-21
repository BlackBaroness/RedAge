package ru.baronessdev.personal.clans.util;

import org.bukkit.Bukkit;
import ru.baronessdev.personal.clans.Clans;

public class ThreadUtil {

    public static void execute(ThreadTask task) {
        new Thread(task::execute).start();
    }

    public static void runLater(int t, Runnable r) {
        Bukkit.getScheduler().runTaskLater(Clans.plugin, r, 20L * t);
    }
}
