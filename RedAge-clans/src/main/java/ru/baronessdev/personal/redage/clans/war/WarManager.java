package ru.baronessdev.personal.redage.clans.war;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.baronessdev.personal.redage.clans.ClansPlugin;
import ru.baronessdev.personal.redage.clans.Data;
import ru.baronessdev.personal.redage.clans.obj.Clan;
import ru.baronessdev.personal.redage.clans.util.SpawnTeleportUtil;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WarManager {

    protected static War w;
    public static HashMap<UUID, Integer> warCount = new HashMap<>();

    public static void scheduleWar(Clan first, Clan second, WarType type) {
        RedAge.broadcast("Кланы " + ChatColor.RED + first.getName() + ChatColor.WHITE + " и " + ChatColor.RED + second.getName() + ChatColor.WHITE + " готовятся к войне! До начала: " + ChatColor.RED + "2" + ChatColor.WHITE + " минуты.");

        warCount.put(first.getUuid(), warCount.getOrDefault(first.getUuid(), 0) + 1);
        warCount.put(second.getUuid(), warCount.getOrDefault(first.getUuid(), 0) + 1);

        w = new War(UUID.randomUUID(), type, first.getUuid(), second.getUuid(), new ArrayList<>(), new ArrayList<>());
        ThreadUtil.runLater(60 * 2, () -> {
            // начало войны
            RedAge.broadcast("Война между кланами " + ChatColor.RED + first.getName() + ChatColor.WHITE + " и " + ChatColor.RED + second.getName() + ChatColor.WHITE + " вот-вот начнётся!");

            // проверка на пустые отряды
            int firstCount = w.getFirstClanPlayers().size();
            int secondCount = w.getSecondClanPlayers().size();

            Clan firstClan = Data.getInstance().getClan(w.getFirstClan());
            Clan secondClan = Data.getInstance().getClan(w.getSecondClan());
            assert firstClan != null;
            assert secondClan != null;

            if (firstCount == 0 && secondCount == 0) {
                // ничья
                RedAge.broadcast(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Итоги войны" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
                RedAge.broadcast("Война между кланами " + ChatColor.RED + firstClan.getName() + ChatColor.WHITE + " и " + ChatColor.RED + secondClan.getName() + ChatColor.WHITE + " завершилась ничьёй!");
                RedAge.broadcast("Оба клана не явились на бой!");
                RedAge.broadcast(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━─━" + ChatColor.RED + "─━─━─━─━─━─━╯");
                w = null;
                return;
            }

            if (firstCount == 0 || secondCount == 0) {
                // проигрыш
                Clan winner = (firstCount != 0) ? firstClan : secondClan;
                Clan looser = (firstCount == 0) ? firstClan : secondClan;

                RedAge.broadcast(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Итоги войны" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
                RedAge.broadcast("Война между кланами " + ChatColor.RED + firstClan.getName() + ChatColor.WHITE + " и " + ChatColor.RED + secondClan.getName() + ChatColor.WHITE + " завершилась победой " + winner.getName());
                RedAge.broadcast("Клан " + looser.getName() + " не явился на бой!");
                RedAge.broadcast(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━─━" + ChatColor.RED + "━─━─━─━─━╯");

                winner.setRating(winner.getRating() + 1);
                w = null;
                return;
            }

            // телепортация игроков 1 клана
            for (int i = 0; i < w.getFirstClanPlayers().size(); i++) {
                Location l = RedAge.formatLocation(ClansPlugin.plugin.getConfig().getString("settings.1." + (i + 1)));
                Player p = w.getFirstClanPlayers().get(i);

                if (!p.isOnline()) continue;
                p.teleport(l);
            }

            // телепортация игроков 2 клана
            for (int i = 0; i < w.getSecondClanPlayers().size(); i++) {
                Location l = RedAge.formatLocation(ClansPlugin.plugin.getConfig().getString("settings.2." + (i + 1)));
                Player p = w.getSecondClanPlayers().get(i);

                if (!p.isOnline()) continue;
                p.teleport(l);
            }

            RedAge.broadcast("Война между кланами " + ChatColor.RED + first.getName() + ChatColor.WHITE + " и " + ChatColor.RED + second.getName() + ChatColor.WHITE + " началась!");

            w.setTimer(new BukkitRunnable() {
                @Override
                public void run() {
                    finishWar(true);
                }
            });
            w.getTimer().runTaskLater(ClansPlugin.plugin, 20 * 60 * 10);
        });
    }

    public static void finishWar(boolean strong) {
        RedAge.log("попытка завершить войну, strong=" + strong);
        // подсчёт оставшихся игроков
        int firstCount = (int) w.getFirstClanPlayers().stream()
                .filter(OfflinePlayer::isOnline)
                .filter(p -> !p.isDead())
                .filter(p -> p.getLocation().getWorld().getName().equals("warWorld"))
                .count();

        int secondCount = (int) w.getSecondClanPlayers().stream()
                .filter(OfflinePlayer::isOnline)
                .filter(p -> !p.isDead())
                .filter(p -> p.getLocation().getWorld().getName().equals("warWorld"))
                .count();
        RedAge.log(firstCount + "/" + secondCount);

        if (firstCount != 0 && secondCount != 0 && !strong) return;
        if (!strong) w.getTimer().cancel();

        boolean draw = firstCount == secondCount;

        Clan firstClan = Data.getInstance().getClan(w.getFirstClan());
        Clan secondClan = Data.getInstance().getClan(w.getSecondClan());
        assert firstClan != null;
        assert secondClan != null;

        if (draw) {
            // если ничья
            RedAge.broadcast(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Итоги войны" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
            RedAge.broadcast("Война между кланами " + ChatColor.RED + firstClan.getName() + ChatColor.WHITE + " и " + ChatColor.RED + secondClan.getName() + ChatColor.WHITE + " завершилась ничьёй!");

            // log first
            StringBuilder firstLog = new StringBuilder(firstClan.getName() + ": ");
            w.getFirstClanPlayers()
                    .forEach(p -> {
                        firstLog.append((!p.isDead() && p.isOnline()) ? ChatColor.GREEN : ChatColor.RED);
                        firstLog.append(p.getName()).append(ChatColor.RESET).append(" ");
                    });
            RedAge.broadcast(firstLog.toString());

            // log second
            StringBuilder secondLog = new StringBuilder(secondClan.getName() + ": ");
            w.getSecondClanPlayers()
                    .forEach(p -> {
                        secondLog.append((!p.isDead() && p.isOnline()) ? ChatColor.GREEN : ChatColor.RED);
                        secondLog.append(p.getName()).append(ChatColor.RESET).append(" ");
                    });
            RedAge.broadcast(secondLog.toString());

            firstClan.setRating(firstClan.getRating() + 1);
            secondClan.setRating(secondClan.getRating() + 1);
            w = null;

            RedAge.broadcast(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━─━" + ChatColor.RED + "─━─━─━─━─━╯");
            Bukkit.getWorld("warWorld").getEntities().stream()
                    .filter(e -> e instanceof Player)
                    .forEach(e -> {
                        e.sendMessage("Вы будете телепортированы на спавн через 15 секунд.");
                        Bukkit.getScheduler().runTaskLater(RedAge.getInstance(), () -> SpawnTeleportUtil.tp((Player) e), 15 * 20);
                    });
            return;
        }

        Clan winner = (firstCount > secondCount) ? firstClan : secondClan;

        RedAge.broadcast(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Итоги войны" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
        RedAge.broadcast("Война между кланами " + ChatColor.RED + firstClan.getName() + ChatColor.WHITE + " и " + ChatColor.RED + secondClan.getName() + ChatColor.WHITE + " завершилась " + ChatColor.GOLD + "победой " + winner.getName() + "!");

        // log first
        StringBuilder firstLog = new StringBuilder(firstClan.getName() + ": ");
        w.getFirstClanPlayers()
                .forEach(p -> {
                    firstLog.append((!p.isDead() && p.isOnline()) ? ChatColor.GREEN : ChatColor.RED);
                    firstLog.append(p.getName()).append(ChatColor.RESET).append(" ");
                });
        RedAge.broadcast(firstLog.toString());

        // log second
        StringBuilder secondLog = new StringBuilder(secondClan.getName() + ": ");
        w.getSecondClanPlayers()
                .forEach(p -> {
                    secondLog.append((!p.isDead() && p.isOnline()) ? ChatColor.GREEN : ChatColor.RED);
                    secondLog.append(p.getName()).append(ChatColor.RESET).append(" ");
                });
        RedAge.broadcast(secondLog.toString());

        winner.setRating(winner.getRating() + 1);
        w = null;

        RedAge.broadcast(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━" + ChatColor.RED + "─━─━─━─━─━─╯");
        Bukkit.getWorld("warWorld").getEntities().stream()
                .filter(e -> e instanceof Player)
                .forEach(e -> {
                    e.sendMessage("Вы будете телепортированы на спавн через 15 секунд.");
                    Bukkit.getScheduler().runTaskLater(RedAge.getInstance(), () -> SpawnTeleportUtil.tp((Player) e), 15 * 20);
                });
    }

    public static War getWar(Clan c) {
        if (w != null && (
                w.getFirstClan().equals(c.getUuid()) || w.getSecondClan().equals(c.getUuid()))
        ) return w;
        return null;
    }

    public static void giveUp(Clan c) {
        Clan enemy = (w.getFirstClan().equals(c.getUuid())) ? Data.getInstance().getClan(w.getSecondClan()) : Data.getInstance().getClan(w.getFirstClan());
        assert enemy != null;
        RedAge.broadcast("Клан " + ChatColor.RED + c.getName() + ChatColor.RESET + " сдаётся клану " + ChatColor.RED + enemy.getName() + "! Война отменена.");

        enemy.setRating(enemy.getRating() + 1);
        w = null;
    }

    public static boolean warExists() {
        return w != null;
    }
}
