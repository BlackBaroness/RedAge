package ru.baronessdev.personal.redage.redagemain;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AdminACF {

    private static PaperCommandManager commandManager;
    private static final HashMap<String, AdminSubCommand> subCommands = new HashMap<>();
    private static final HashMap<String, String> descriptions = new HashMap<>();

    protected static void setup(JavaPlugin plugin) {
        commandManager = new PaperCommandManager(plugin);
        commandManager.getLocales().setDefaultLocale(Locale.forLanguageTag("ru"));
        addCompletion("redageSubcommands", c -> new ArrayList<>(subCommands.keySet()));
        commandManager.registerCommand(new RedAgeCommand());
    }

    public static void addCompletion(String id, CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext> handler) {
        commandManager.getCommandCompletions().registerAsyncCompletion(id, handler);
    }

    public static void addCommand(String name, String description, BaseCommand command) {
        subCommands.put(name, null);
        descriptions.put(name, description);
        commandManager.registerCommand(command);
        RedAge.log("Advanced admin subcommand registered: " + name);
    }

    public static void registerSimpleAdminCommand(String command, String description, AdminSubCommand e) {
        subCommands.put(command.toLowerCase(), e);
        descriptions.put(command.toLowerCase(), description);
        RedAge.log("Simple admin subcommand registered: " + command);
    }

    @SuppressWarnings("unused")
    @CommandAlias("redage")
    @CommandPermission("admin")
    public static class RedAgeCommand extends BaseCommand {

        @CommandCompletion("@redageSubcommands")
        @Default
        @CatchUnknown
        public void unknown(CommandSender sender, String[] args) {
            if (args.length == 0) {
                help(sender);
                return;
            }

            AdminSubCommand sub = subCommands.get(args[0].toLowerCase());

            String[] newArgs = (String[]) ArrayUtils.remove(args, 0);
            if (sub == null) {
                help(sender);
                return;
            }

            try {
                if (!sub.onCommand(sender, newArgs))
                    help(sender);
            } catch (ClassCastException e) {
                RedAge.say(sender, "Данная команда доступна только для игроков.");
            }
        }

        private static void help(CommandSender sender) {
            RedAge.say(sender, "Доступные команды: ");
            subCommands.forEach((s, adminSubCommand) -> RedAge.say(sender, s + " " + descriptions.getOrDefault(s, "описание не найдено")));
        }
    }
}
