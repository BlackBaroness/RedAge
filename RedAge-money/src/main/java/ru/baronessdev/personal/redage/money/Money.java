package ru.baronessdev.personal.redage.money;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.redagemain.AdminACF;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.database.mysql.MySQL;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class Money extends JavaPlugin implements Listener {

    private static MySQL database;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        connect();

        AdminACF.addCommand("redcoin", " - управление донат валютой", new RedCoinCommand());
    }

    private void connect() {
        RedAge.log("Подключаюсь к MySQL");
        database = new MySQL(this, "localhost", 3306, "donate_money", "local_user", "password");

        database.execute(false, "CREATE TABLE IF NOT EXISTS `data` (" +
                "`nick` varchar(30) NOT NULL," +
                "`balance` INT," +
                "PRIMARY KEY (`nick`) USING BTREE" +
                ") ENGINE=InnoDB;");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            setBalance(p, 0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        String s = e.getMessage();
        if (s.equals("/bal") || s.equals("/balance") || s.equals("/money")) {
            e.setCancelled(true);
            Player p = e.getPlayer();

            RedAge.say(p, "Ваши балансы:");
            RedAge.say(p, "Игровая валюта: " +
                    ChatColor.GREEN + "" +
                    ChatColor.BOLD +
                    RedAge.getEconomy().getBalance(p) +
                    ChatColor.GREEN + "$");
            RedAge.say(p, "Премиум валюта: " +
                    ChatColor.GREEN + "" +
                    ChatColor.BOLD +
                    getBalance(p) +
                    ChatColor.RESET + " redcoin");
        }
    }

    public static int getBalance(Player p) {
        ResultSet s = database.executeQuery("SELECT `balance` FROM `data` WHERE `nick`=\"" + p.getName() + "\"");
        try {
            if (s.next()) {
                return s.getInt("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setBalance(Player p, int balance) {
        database.execute(false, "DELETE FROM `data` WHERE `nick`=\"" + p.getName() + "\"");
        database.execute(true, "INSERT INTO `data` (`nick`, `balance`) VALUES ('" + p.getName() + "', " + balance + ");");
    }


    @SuppressWarnings("unused")
    @CommandAlias("redage")
    @Subcommand("redcoin")
    @CommandPermission("admin")
    static class RedCoinCommand extends BaseCommand {

        @CatchUnknown
        @Default
        public void unknown(CommandSender sender) {
            help(sender);
        }

        @CommandCompletion("@players")
        @Subcommand("get")
        public void get(CommandSender sender, String[] args) {
            Player v = getPlayer(sender, args[0]);
            if (v == null) return;

            sender.sendMessage(getBalance(v) + "");
        }

        @CommandCompletion("@players")
        @Subcommand("set")
        public void set(CommandSender sender, String[] args) {
            if (args.length != 2) {
                help(sender);
                return;
            }

            Player v = getPlayer(sender, args[0]);
            if (v == null) return;

            int newBalance;
            try {
                newBalance = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("дурак цифру пиши");
                return;
            }

            setBalance(v, newBalance);
            sender.sendMessage("установлено");
        }

        private Player getPlayer(CommandSender s, String st) {
            Player v = Bukkit.getPlayer(st);
            if (v == null) {
                s.sendMessage("игрок оффлайн");
                return null;
            }
            return v;
        }

        private void help(CommandSender s) {
            s.sendMessage("/redage redcoin get [игрок] - получить баланс игрока (онлайн)");
            s.sendMessage("/redage redcoin set [игрок] [баланс] - установить баланс игроку (онлайн)");
        }
    }
}
