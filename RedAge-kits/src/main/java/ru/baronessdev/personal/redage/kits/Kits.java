package ru.baronessdev.personal.redage.kits;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.redagemain.ACF;
import ru.baronessdev.personal.redage.redagemain.AdminACF;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Kits extends JavaPlugin {

    private final List<Kit> kits = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load();

        AdminACF.registerSimpleAdminCommand("kit-reload", "- перезагружает киты", (sender, args) -> {
            load();
            RedAge.say(sender, "Киты перезагружены");
            return true;
        });

        ACF.addCompletion("kits", c -> {
            List<String> l = new ArrayList<>();
            Player p = c.getPlayer();
            kits.forEach(kit -> {
                if (hasPermission(p, kit) && needTime(p, kit) == 0) {
                    l.add(kit.getName());
                }
            });

            return l;
        });
        ACF.addCommand(new KitCommand());

        AdminACF.registerSimpleAdminCommand("kit", "[кит] [игрок] - выдаёт кит", ((sender, args) -> {
            if (args.length != 2) return false;

            Kit k = getKit(args[0]);
            if (k == null) {
                sender.sendMessage("Кит не найден");
                return true;
            }

            Player p = Bukkit.getPlayer(args[1]);
            k.getContain().forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("{p}", p.getName())));

            sender.sendMessage("Кит выдан");
            return true;
        }));
    }

    public static boolean hasPermission(Player p, Kit kit) {
        return p.hasPermission(kit.getName());
    }

    private Kit getKit(String s) {
        return kits.stream().filter(kit -> kit.getName().equals(s)).findAny().orElse(null);
    }

    private long needTime(Player p, Kit k) {
        long x = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "data.yml")).getLong(k.getName() + "." + p.getName());
        if (x == 0) return 0;

        long y = k.getDelay();
        long z = System.currentTimeMillis();

        return (z - y > x) ? 0 : ~(z - (x + y));
    }

    private void load() {
        kits.clear();
        reloadConfig();

        getConfig().getKeys(false).forEach(s -> {
            kits.add(new Kit(s, getConfig().getLong(s + ".timer"), getConfig().getStringList(s + ".list")));
            RedAge.log("создаю кит: " + s);
        });
    }

    @SuppressWarnings("unused")
    @CommandAlias("kit|kits")
    public class KitCommand extends BaseCommand {

        @Default
        public void unknown(Player p) {
            StringBuilder s = new StringBuilder();
            kits.forEach(kit -> {
                if (Kits.hasPermission(p, kit)) {
                    s.append(" ")
                            .append((needTime(p, kit) == 0) ? ChatColor.GREEN : ChatColor.GRAY)
                            .append(kit.getName());
                }
            });

            if (s.toString().equals("")) {
                RedAge.say(p, "Доступных китов нет.");
                return;
            }


            RedAge.say(p, "Доступные киты:" + s);
        }

        @CommandCompletion("@kits")
        @CatchUnknown
        public void kit(Player p, String[] args) {
            Kit k = getKit(args[0]);
            if (k == null) {
                RedAge.say(p, "Указанный кит не найден. Введите " + ChatColor.RED + "/kit" + ChatColor.WHITE + ", чтобы посмотреть доступные.");
                return;
            }

            if (!Kits.hasPermission(p, k)) {
                RedAge.say(p, "У вас нет доступа к этому киту.");
                return;
            }

            long l = needTime(p, k);
            if (l == 0) {
                k.getContain().forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("{p}", p.getName())));

                File f = new File(getDataFolder() + File.separator + "data.yml");
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                cfg.set(k.getName() + "." + p.getName(), System.currentTimeMillis());
                ThreadUtil.execute(() -> {
                    try {
                        cfg.save(f);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });

                RedAge.say(p, "Вам выдан кит " + ChatColor.RED + k.getName() + ChatColor.WHITE + ".");
                return;
            }

            long last = TimeUnit.MILLISECONDS.toSeconds(l);
            long hours = last / 3600;
            long minutes = (last % 3600) / 60;
            long seconds = last % 60;

            RedAge.say(p, String.format("§fДо следующего использования: §c%d:%d:%d", hours, minutes, seconds));
        }
    }
}
