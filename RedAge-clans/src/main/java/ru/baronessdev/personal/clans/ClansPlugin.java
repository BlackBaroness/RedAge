package ru.baronessdev.personal.clans;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.clans.commands.ClanCommand;
import ru.baronessdev.personal.clans.commands.ClanWarCommand;
import ru.baronessdev.personal.clans.gui.GuiHandler;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.clans.util.ClanChatUtil;
import ru.baronessdev.personal.clans.war.WarListener;
import ru.baronessdev.personal.clans.war.WarManager;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ClansPlugin extends JavaPlugin {

    public static JavaPlugin plugin;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = this;

        // подключение базы данных
        Data.getInstance().setup();

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
            Clan clan = Data.getInstance().getClan(c.getPlayer());
            return (clan == null) ? new ArrayList<>() : clan.getMembers();
        });
        commandManager.getCommandCompletions().registerCompletion("clans", c -> {
            String myClan;
            try {
                myClan = Data.getInstance().getClan(c.getPlayer()).getName();
            } catch (NullPointerException e) {
                myClan = "";
            }

            List<String> l = new ArrayList<>();
            String finalMyClan = myClan;

            Data.getInstance().getClans()
                    .stream().filter(clan -> !clan.getName().equals(finalMyClan))
                    .forEach(clan -> l.add(clan.getName()));
            return l;
        });

        // регистрация слушателя войны
        Bukkit.getPluginManager().registerEvents(new WarListener(), this);

        // регистрация слушателя гуи
        Bukkit.getPluginManager().registerEvents(new GuiHandler(), this);

        // регистрация субкоманды /redage clans
        RedAge.registerAdminCommand("clans", "- управление системой кланов", ((sender, args) -> {
            if (args.length == 0) {
                helpAdmin(sender);
                return true;
            }

            switch (args[0]) {
                case "rename": {
                    if (args.length != 3)
                        helpAdmin(sender);

                    Clan clan = Data.getInstance().getClan(args[1]);
                    if (clanNotExists(clan, sender)) return true;

                    RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " изменил название клана «" + clan.getName() + "» на «" + args[2] + "»");
                    clan.setName(args[2]);
                    return true;
                }
                case "delete": {
                    if (args.length != 2) {
                        helpAdmin(sender);
                        return true;
                    }

                    Clan clan = Data.getInstance().getClan(args[1]);
                    if (clanNotExists(clan, sender)) return true;

                    Data.getInstance().deleteClan(clan);
                    RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " удалил клан «" + clan.getName() + "»");
                    return true;
                }
            }
            return true;
        }));

        WarManager.setup();

        // регистрация papi плейсхолдеров
        new PlaceholderAPIHook().register();

        // регистрация /cc
        getCommand("cc").setExecutor(((sender, command, label, args) -> {
            if (!(sender instanceof Player)) return true;
            if (args.length == 0) return false;

            StringBuilder b = new StringBuilder();
            Arrays.stream(args).forEach(s -> b.append(s).append(" "));
            ClanChatUtil.process((Player) sender, b.toString());
            return true;
        }));
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
    }
}
