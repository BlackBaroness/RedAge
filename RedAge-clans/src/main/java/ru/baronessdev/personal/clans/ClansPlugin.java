package ru.baronessdev.personal.clans;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.clans.commands.ClanCommand;
import ru.baronessdev.personal.clans.commands.ClanWarCommand;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.clans.war.WarListener;
import ru.baronessdev.personal.clans.war.WarManager;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.SQLite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClansPlugin extends JavaPlugin {

    public static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();

        SQLite database = new SQLite("clans",
                "name", "VARCHAR(255) NOT NULL,",
                "uuid", "VARCHAR(100) NOT NULL,",
                "creation_date", "BIGINT NOT NULL,",
                "last_war_date", "BIGINT NOT NULL,",
                "has_battle_pass", "TINYINT NOT NULL DEFAULT '0',",
                "war_count", "TINYINT NOT NULL DEFAULT '0',",

                "# PRIMARY KEY (`uuid`)"
        );

        // создание команд-менеджера
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.getLocales().setDefaultLocale(Locale.forLanguageTag("ru"));

        // регистрация команд
        commandManager.registerCommand(new ClanCommand());
        commandManager.registerCommand(new ClanWarCommand());

        // регистрация заполнений
        commandManager.getCommandCompletions().registerCompletion("clanHelp", c ->
                ImmutableList.of("create", "invite", "kick", "gui", "leave", "top", "setflag"));

        commandManager.getCommandCompletions().registerCompletion("members", c -> {
            Clan clan = Data.getClan(c.getPlayer());
            return (clan == null) ? new ArrayList<>() : clan.getMembers();
        });

        commandManager.getCommandCompletions().registerCompletion("clans", c -> {
            List<String> l = new ArrayList<>();
            Data.clanListByRating().forEach(clan -> l.add(clan.getName()));
            return l;
        });


        Data.setup(this);
        Bukkit.getPluginManager().registerEvents(new WarListener(), this);

        RedAge.registerAdminCommand("clans", "- управление системой кланов", ((sender, args) -> {
            if (args.length == 0) {
                helpAdmin(sender);
                return true;
            }

            switch (args[0]) {
                case "rename": {
                    if (args.length != 3)
                        helpAdmin(sender);

                    Clan clan = Data.getClan(args[1]);
                    if (clanNotExists(clan, sender)) return true;
                    assert clan != null;

                    RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " изменил название клана «" + clan.getName() + "» на «" + args[2] + "»");
                    clan.setName(args[2]);
                    Data.saveClan(clan);
                    return true;
                }
                case "delete": {
                    if (args.length != 2) {
                        helpAdmin(sender);
                        return true;
                    }

                    Clan clan = Data.getClan(args[1]);
                    if (clanNotExists(clan, sender)) return true;
                    assert clan != null;

                    Data.deleteClan(clan);
                    RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " удалил клан «" + clan.getName() + "»");
                    return true;
                }
                case "reload": {
                    Data.setup(this);
                    RedAge.say(sender, "Кланы перезагружены.");
                }
            }
            return true;
        }));

        WarManager.setup();
    }

    public static boolean clanNotExists(Clan c, CommandSender s) {
        if (c == null) {
            RedAge.say(s, ChatColor.RED + "Указанный клан не существует.");
            return true;
        }
        return false;
    }

    private void helpAdmin(CommandSender s) {
        RedAge.say(s, ChatColor.AQUA + "clans rename [клан] [название]" + ChatColor.WHITE + " - переименовать клан");
        RedAge.say(s, ChatColor.AQUA + "clans delete [клан]" + ChatColor.WHITE + " - удалить клан");
        RedAge.say(s, ChatColor.AQUA + "clans reload" + ChatColor.WHITE + " - перезагрузить плагин кланов");
    }
}
