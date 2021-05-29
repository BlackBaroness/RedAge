package ru.baronessdev.personal.redage.redagemain.util;

import org.bukkit.Bukkit;
import ru.baronessdev.personal.redage.redagemain.RedAge;

public class ThreadUtil {

    public static void execute(Task task) {
        new Thread(task::execute).start();
    }

    public static void runLater(int t, Runnable r) {
        Bukkit.getScheduler().runTaskLater(RedAge.getInstance(), r, 20L * t);
        RedAge.log("Creating later task for " + t + "s");
    }

    public static void runBukkitTask(Task task) {
        Bukkit.getScheduler().runTask(RedAge.getInstance(), task::execute);
    }
}
