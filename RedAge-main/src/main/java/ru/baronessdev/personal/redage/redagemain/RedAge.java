package ru.baronessdev.personal.redage.redagemain;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class RedAge extends JavaPlugin implements Listener {

    @Getter
    protected static JavaPlugin instance;

    @Override
    public void onEnable() {
        setupBase();
        setupEconomy();
        setupSqlite();
        setupACF();
        setupCommands();
    }

    private void setupBase() {
        saveDefaultConfig();
        instance = this;
    }




    /* =================================        ЭКОНОМИКА       ================================= */

    @Getter
    private static Economy economy;

    private void setupEconomy() {
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }




    /* =================================         SQLITE       ================================= */

    public static Connection sqlite;

    private void setupSqlite() {
        Connection tempConnection;
        try {
            tempConnection = DriverManager.getConnection("jdbc:sqlite://" + RedAge.getInstance().getDataFolder().getAbsolutePath() + File.separator + "sqlite.db");
        } catch (SQLException e) {
            tempConnection = null;
            RedAge.log("CRITICAL: Cannot connect to SQLite database");
            e.printStackTrace();
        }
        sqlite = tempConnection;
    }



    /* =================================           ACF       ================================= */

    private void setupACF() {
        AdminACF.setup(this);
    }

    private static final List<Player> loggers = new ArrayList<>();



    /* =================================         КОМАНДЫ       ================================= */

    private void setupCommands() {
        AdminACF.registerSimpleAdminCommand("location", "- показывает текущую локацию", ((sender, args) -> {
            sender.sendMessage(ChatColor.GRAY + "Блок: " + ChatColor.WHITE + formatLocation(((Player) sender).getLocation()));
            sender.sendMessage(ChatColor.GRAY + "Чанк: " + ChatColor.WHITE + ((Player) sender).getLocation().getChunk().toString());
            return true;
        }));

        AdminACF.registerSimpleAdminCommand("createWorld", "[name] - создать новый мир", (((sender, args) -> {
            if (args.length == 0) return false;

            World w = Bukkit.getWorld(args[0]);
            if (w != null) {
                sender.sendMessage("Мир уже существует");
                return true;
            }

            w = Bukkit.createWorld(new WorldCreator(args[0]));
            sender.sendMessage("Мир " + args[0] + " создан");

            w.setAutoSave(true);
            w.save();
            return true;
        })));

        AdminACF.registerSimpleAdminCommand("tpWorld", "[name] - телепортироваться в мир", ((sender, args) -> {
            if (args.length == 0) return false;

            World w = Bukkit.createWorld(new WorldCreator(args[0]));

            Player p = (Player) sender;
            p.teleport(w.getSpawnLocation());
            return true;
        }));

        AdminACF.registerSimpleAdminCommand("debug", "- переключить режим отладки", ((sender, args) -> {
            Player p = (Player) sender;

            if (!loggers.contains(p)) {
                loggers.add(p);
                p.sendMessage("Теперь ты видишь некоторое дерьмо");
            } else {
                loggers.remove(p);
                p.sendMessage("Теперь ты слеп");
            }
            return true;
        }));
    }



    /* =================================        UTILS       ================================= */

    public static void log(String msg) {
        System.out.println(ChatColor.RED + "" + ChatColor.BOLD + "[RedAge] " + ChatColor.WHITE + msg);
        loggers.forEach(l -> l.sendMessage("Дебаг: " + msg));
    }

    public static void say(CommandSender s, String msg) {
        s.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[RedAge] " + ChatColor.WHITE + msg);
    }

    public static void broadcast(String s) {
        Bukkit.getOnlinePlayers().forEach(player -> say(player, s));
    }

    public static String formatLocation(Location l) {
        return l.getWorld().getName() + " # " + (int) l.getX() + " # " + (int) l.getY() + " # " + (int) l.getZ();
    }

    public static Location formatLocation(String s) {
        String[] split = s.split("#");
        return new Location(
                Bukkit.createWorld(new WorldCreator(split[0].replace(" ", ""))),
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3])
        );
    }
}

