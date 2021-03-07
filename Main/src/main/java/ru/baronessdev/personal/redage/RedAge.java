package ru.baronessdev.personal.redage;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class RedAge extends JavaPlugin implements Listener {

    private static final HashMap<String, AdminSubCommand> subCommands = new HashMap<>();
    private static final HashMap<String, String> descriptions = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("redAge").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                help(sender);
                return true;
            }

            AdminSubCommand sub = subCommands.get(args[0]);

            String[] newArgs = (String[]) ArrayUtils.remove(args, 0);
            if (sub == null || !sub.onCommand(sender, newArgs)) help(sender);
            return true;
        });
    }

    private static void help(CommandSender sender) {
        say(sender, "Доступные команды: ");
        subCommands.forEach((s, adminSubCommand) -> say(sender, s + " - " + descriptions.getOrDefault(s, "описание не найдено")));
    }

    public static void registerAdminCommand(String command, String description, AdminSubCommand e) {
        subCommands.put(command, e);
        descriptions.put(command, description);
        log("Admin subcommand registered: " + command);
    }

    public static void log(String msg) {
        System.out.println(ChatColor.RED + "" + ChatColor.BOLD + "[RedAge] " + ChatColor.WHITE + msg);
    }

    public static void say(CommandSender s, String msg) {
        s.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[RedAge] " + ChatColor.WHITE + msg);
    }
}
