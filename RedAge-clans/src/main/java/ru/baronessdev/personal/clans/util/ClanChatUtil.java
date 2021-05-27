package ru.baronessdev.personal.clans.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.baronessdev.personal.clans.Data;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.Optional;

public class ClanChatUtil {

    public static void process(Player p, String msg) {
        Clan c = Data.getInstance().getClan(p);
        if (c == null) {
            RedAge.say(p, "Чтобы использовать клановый чат, нужно находиться в клане.");
            return;
        }

        c.getMembers().forEach(s -> Optional.of(Bukkit.getPlayer(s)).ifPresent(player -> player.sendMessage(ChatColor.DARK_AQUA + p.getName() + ": " + ChatColor.AQUA + msg)));
    }
}
