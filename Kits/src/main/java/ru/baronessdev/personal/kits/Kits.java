package ru.baronessdev.personal.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.RedAge;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Kits extends JavaPlugin implements Listener {

    private final List<Kit> kits = new ArrayList<>();

    @Override
    public void onEnable() {
        load();
        saveDefaultConfig();
        
        RedAge.registerAdminCommand("kit", "перезагружает киты", (sender, args) -> {
            load();
            RedAge.say(sender, "Киты перезагружены");
            return true;
        });
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (e.getMessage().equals("/kit")) {
            e.setCancelled(true);
            StringBuilder s = new StringBuilder();
            kits.forEach(kit -> {
                if (hasPermission(p, kit)) {
                    s.append(" ")
                            .append((hasTime(p, kit) ? ChatColor.GREEN : ChatColor.GRAY))
                            .append(kit.getName());
                }
            });

            if (s.toString().equals("")) {
                RedAge.say(p, "Доступных китов нет.");
                return;
            }


            RedAge.say(p, "Доступные киты:" + s.toString());
            return;
        }
        if (e.getMessage().startsWith("/kit ")) {
            e.setCancelled(true);

            Kit k = getKit(e.getMessage().split(" ")[1]);
            if (k == null) {
                RedAge.say(p, "Указанный кит не найден. Введите " + ChatColor.RED + "/kit" + ChatColor.WHITE + ", чтобы посмотреть доступные.");
                return;
            }

            if (!hasPermission(p, k)) {
                RedAge.say(p, "У вас нет доступа к этому киту.");
                return;
            }

            if (!hasTime(p, k)) {
                RedAge.say(p, "Похоже, время сбора этого кита не пришло. Попробуйте завтра.");
                return;
            }

            k.getContain().forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("{P}", p.getName())));
            getConfig().set(p.getName().toLowerCase() + "." + k.getName(), new Date().getDay());
            saveConfig();

            RedAge.say(p, "Вам выдан кит " + ChatColor.RED + k.getName() + ChatColor.WHITE + ".");
        }
    }

    private boolean hasPermission(Player p, Kit kit) {
        return p.hasPermission(kit.getName());
    }

    private Kit getKit(String s) {
        return kits.stream().filter(kit -> kit.getName().equals(s)).findAny().orElse(null);
    }

    @SuppressWarnings("deprecation")
    private boolean hasTime(Player p, Kit k) {
        int d = getConfig().getInt(p.getName().toLowerCase() + "." + k.getName(), -1);
        if (d == -1) return true;
        return d != new Date().getDay();
    }

    private void load() {
        kits.clear();
        reloadConfig();

        ConfigurationSection kitsSection = getConfig().getConfigurationSection("kits");
        kitsSection.getKeys(false).forEach(s -> kits.add(new Kit(s, kitsSection.getStringList(s))));
    }
}
