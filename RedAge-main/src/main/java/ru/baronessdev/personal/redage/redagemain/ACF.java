package ru.baronessdev.personal.redage.redagemain;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class ACF {

    private static PaperCommandManager commandManager;

    protected static void setup(JavaPlugin plugin) {
        commandManager = new PaperCommandManager(plugin);
        commandManager.getLocales().setDefaultLocale(Locale.forLanguageTag("ru"));
    }

    public static void addCompletion(String id, CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext> handler) {
        commandManager.getCommandCompletions().registerAsyncCompletion(id, handler);
    }

    public static void addCommand(BaseCommand command) {
        commandManager.registerCommand(command);
    }
}
