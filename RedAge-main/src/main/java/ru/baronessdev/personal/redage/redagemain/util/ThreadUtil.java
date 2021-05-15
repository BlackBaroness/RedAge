package ru.baronessdev.personal.redage.redagemain.util;

import org.bukkit.Bukkit;
import ru.baronessdev.personal.clans.ClansPlugin;
import ru.baronessdev.personal.redage.redagemain.RedAge;

public class ThreadUtil {

    public static void execute(Task task) {
        new Thread(task::execute).start();
    }

    public static void runLater(int t, Runnable r) {
        Bukkit.getScheduler().runTaskLater(ClansPlugin.plugin, r, 20L * t);
        RedAge.log("Creating later task for " + t + "s");
    }
}
