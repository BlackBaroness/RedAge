package ru.baronessdev.personal.redage.redagemain;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class RedAge extends JavaPlugin implements Listener {

    private static final HashMap<String, AdminSubCommand> subCommands = new HashMap<>();
    private static final HashMap<String, String> descriptions = new HashMap<>();
    private static final List<Player> loggers = new ArrayList<>();

    protected static JavaPlugin instance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        getCommand("redage").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                help(sender);
                return true;
            }

            AdminSubCommand sub = subCommands.get(args[0].toLowerCase());

            String[] newArgs = (String[]) ArrayUtils.remove(args, 0);
            if (sub == null) {
                help(sender);
                return true;
            }

            try {
                if (!sub.onCommand(sender, newArgs))
                    help(sender);
            } catch (ClassCastException e) {
                say(sender, "Данная команда доступна только для игроков.");
            }
            return true;
        });

        registerAdminCommand("location", "- показывает текущую локацию", ((sender, args) -> {
            sender.sendMessage(ChatColor.GRAY + "Блок: " + ChatColor.WHITE + formatLocation(((Player) sender).getLocation()));
            sender.sendMessage(ChatColor.GRAY + "Чанк: " + ChatColor.WHITE + ((Player) sender).getLocation().getChunk().toString());
            return true;
        }));

        registerAdminCommand("createWorld", "[name] - создать новый мир", (((sender, args) -> {
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

        registerAdminCommand("tpWorld", "[name] - телепортироваться в мир", ((sender, args) -> {
            if (args.length == 0) return false;

            World w = Bukkit.createWorld(new WorldCreator(args[0]));

            Player p = (Player) sender;
            p.teleport(w.getSpawnLocation());
            return true;
        }));

        registerAdminCommand("debug", "- переключить режим отладки", ((sender, args) -> {
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

    private static void help(CommandSender sender) {
        say(sender, "Доступные команды: ");
        subCommands.forEach((s, adminSubCommand) -> say(sender, s + " " + descriptions.getOrDefault(s, "описание не найдено")));
    }

    public static void registerAdminCommand(String command, String description, AdminSubCommand e) {
        subCommands.put(command.toLowerCase(), e);
        descriptions.put(command.toLowerCase(), description);
        log("Admin subcommand registered: " + command);
    }

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
                Bukkit.createWorld(new WorldCreator(split[0])),
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3])
        );
    }

    protected static JavaPlugin getInstance() {
        return instance;
    }
}

