package ru.baronessdev.personal.clans;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Data {

    private static JavaPlugin core;

    public static void setup(JavaPlugin plugin) {
        core = plugin;
        core.reloadConfig();
    }

    public static Clan getClan(String s) {
        for (String id : core.getConfig().getKeys(false)) {
            if (id.equals("settings")) continue;

            if (core.getConfig().getString(id + ".name").equals(s)) {
                return new Clan(UUID.fromString(id),
                        core.getConfig().getItemStack(id + ".icon"),
                        core.getConfig().getString(id + ".name"),
                        core.getConfig().getString(id + ".owner"),
                        core.getConfig().getInt(id + ".rating"),
                        core.getConfig().getBoolean(id + ".hasBattlePass"),
                        core.getConfig().getInt(id + ".battlePassPoints"),
                        core.getConfig().getStringList(id + ".members"),
                        (Long) core.getConfig().get(id + ".creationTime")
                );
            }
        }
        return null;
    }

    public static boolean nameExists(String s) {
        s = s.toLowerCase();
        for (String id : core.getConfig().getKeys(false)) {
            if (id.equals("settings")) continue;

            if (core.getConfig().getString(id + ".name").toLowerCase().equals(s)) return true;
        }
        return false;
    }

    public static Clan getClan(UUID uuid) {
        for (String id : core.getConfig().getKeys(false)) {
            if (id.equals("settings")) continue;

            if (id.equals(uuid.toString())) {
                return new Clan(UUID.fromString(id),
                        core.getConfig().getItemStack(id + ".icon"),
                        core.getConfig().getString(id + ".name"),
                        core.getConfig().getString(id + ".owner"),
                        core.getConfig().getInt(id + ".rating"),
                        core.getConfig().getBoolean(id + ".hasBattlePass"),
                        core.getConfig().getInt(id + ".battlePassPoints"),
                        core.getConfig().getStringList(id + ".members"),
                        (Long) core.getConfig().get(id + ".creationTime"));
            }
        }
        return null;
    }


    public static Clan getClan(Player p) {
        for (String id : core.getConfig().getKeys(false)) {
            if (id.equals("settings")) continue;

            if (core.getConfig().getStringList(id + ".members").contains(p.getName().toLowerCase())) {
                return new Clan(UUID.fromString(id),
                        core.getConfig().getItemStack(id + ".icon"),
                        core.getConfig().getString(id + ".name"),
                        core.getConfig().getString(id + ".owner"),
                        core.getConfig().getInt(id + ".rating"),
                        core.getConfig().getBoolean(id + ".hasBattlePass"),
                        core.getConfig().getInt(id + ".battlePassPoints"),
                        core.getConfig().getStringList(id + ".members"),
                        (Long) core.getConfig().get(id + ".creationTime"));
            }
        }
        return null;
    }

    public static boolean hasClan(Player p) {
        return getClan(p) != null;
    }

    public static void saveClan(final Clan clan) {
        synchronized (Data.class) {
            ThreadUtil.execute(() -> {
                String path = clan.getUuid().toString();

                core.getConfig().set(path + ".icon", clan.getIcon());
                core.getConfig().set(path + ".name", clan.getName());
                core.getConfig().set(path + ".owner", clan.getOwner());
                core.getConfig().set(path + ".rating", clan.getRating());
                core.getConfig().set(path + ".hasBattlePass", clan.isHasBattlePass());
                core.getConfig().set(path + ".battlePassPoints", clan.getBattlePassPoints());
                core.getConfig().set(path + ".members", clan.getMembers());
                core.getConfig().set(path + ".creationTime", clan.getCreationTime());
                core.saveConfig();
            });
        }
    }

    public static void saveClan(Clan... clans) {
        for (Clan c : clans) saveClan(c);
    }

    public static void deleteClan(Clan clan) {
        core.getConfig().set(clan.getUuid().toString(), null);
        core.saveConfig();
    }

    public static List<Clan> clanListByRating() {
        List<Clan> l = new ArrayList<>();
        core.getConfig().getKeys(false).forEach(k -> {
            if (!k.equals("settings")) {
                l.add(getClan(UUID.fromString(k)));
            }
        });

        l.sort(Clan.COMPARE_BY_RATING);
        return l;
    }
}
