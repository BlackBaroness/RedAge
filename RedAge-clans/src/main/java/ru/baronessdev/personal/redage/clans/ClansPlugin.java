package ru.baronessdev.personal.redage.clans;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.clans.commands.ClanAdminCommand;
import ru.baronessdev.personal.redage.clans.commands.ClanCommand;
import ru.baronessdev.personal.redage.clans.commands.ClanWarCommand;
import ru.baronessdev.personal.redage.clans.gui.GuiHandler;
import ru.baronessdev.personal.redage.clans.obj.Clan;
import ru.baronessdev.personal.redage.clans.util.ClanChatUtil;
import ru.baronessdev.personal.redage.clans.war.WarListener;
import ru.baronessdev.personal.redage.redagemain.ACF;
import ru.baronessdev.personal.redage.redagemain.AdminACF;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClansPlugin extends JavaPlugin {

    public static JavaPlugin plugin;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = this;

        // подключение базы данных
        Data.getInstance().setup();

        // регистрация команд
        ACF.addCommand(new ClanCommand());
        ACF.addCommand(new ClanWarCommand());

        // регистрация заполнений
        ACF.addCompletion("members", c -> {
            Clan clan = Data.getInstance().getClan(c.getPlayer());
            return (clan == null) ? new ArrayList<>() : clan.getMembers();
        });
        ACF.addCompletion("clans", c -> {
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
        AdminACF.addCompletion("clans", c -> {
            List<String> l = new ArrayList<>();
            Data.getInstance().getClans().forEach(clan -> l.add(clan.getName()));
            return l;
        });


        // регистрация слушателя войны
        Bukkit.getPluginManager().registerEvents(new WarListener(), this);

        // регистрация слушателя гуи
        Bukkit.getPluginManager().registerEvents(new GuiHandler(), this);

        // регистрация субкоманды /redage clans
        AdminACF.addCommand("clans", " - управление кланами", new ClanAdminCommand());

        // регистрация papi плейсхолдеров
        new PlaceholderAPIHook().register();

        // регистрация /cc
        ACF.addCommand(new CCCommand());
    }

    public static boolean clanNotExists(Clan c, CommandSender s) {
        if (c == null) {
            RedAge.say(s, ChatColor.RED + "Указанный клан не существует.");
            return true;
        }
        return false;
    }

    @CommandAlias("cc")
    public class CCCommand extends BaseCommand {

        @Default
        public void base(Player p) {
            p.sendMessage("/cc [сообщение] - написать в клановый чат");
        }

        @CatchUnknown
        public void msg(Player p, String[] args) {
            StringBuilder b = new StringBuilder();
            Arrays.stream(args).forEach(s -> b.append(s).append(" "));
            ClanChatUtil.process(p, b.toString());
        }
    }
}
