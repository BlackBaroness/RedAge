package ru.baronessdev.personal.redage.money;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Language;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.Task;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Money extends JavaPlugin implements Listener {

    private static Connection connection;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        connect();

        RedAge.registerAdminCommand("redcoin", " - управление донат валютой", (player, args) -> {
            if (args.length < 2) {
                help(player);
                return true;
            }

            Player v = Bukkit.getPlayer(args[1]);
            if (v == null) {
                player.sendMessage("игрок оффлайн");
                help(player);
                return true;
            }

            if (args[0].equals("get")) {
                player.sendMessage(getBalance(v) + "");
                return true;
            }

            if (args[0].equals("set") && args.length == 3) {
                int newBalance;
                try {
                    newBalance = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("дурак цифру пиши");
                    return true;
                }

                setBalance(v, newBalance);
                player.sendMessage("установлено");
                return true;
            }

            help(player);
            return false;
        });
    }

    private void help(CommandSender s) {
        s.sendMessage("/redage redcoin get [игрок] - получить баланс игрока (онлайн)");
        s.sendMessage("/redage redcoin set [игрок] [баланс] - установить баланс игроку (онлайн)");
    }

    private static void connect() {
        RedAge.log("Подключаюсь к MySQL");
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/donate_money",
                    "local_user",
                    "password"
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        execute(false, "CREATE TABLE IF NOT EXISTS `data` (" +
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
        ResultSet s = executeQuery("SELECT `balance` FROM `data` WHERE `nick`=\"" + p.getName() + "\"");
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
        execute(false, "DELETE FROM `data` WHERE `nick`=\"" + p.getName() + "\"");
        execute(true, "INSERT INTO `data` (`nick`, `balance`) VALUES ('" + p.getName() + "', " + balance + ");");
    }

    private static void execute(boolean async, @Language("SQL") String query) {
        System.out.println(ChatColor.AQUA + query);
        Task task = () -> {
            checkConnection();
            try {
                connection.createStatement().execute(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        if (async) {
            ThreadUtil.execute(task);
        } else task.execute();
    }

    @SuppressWarnings("SameParameterValue")
    private static ResultSet executeQuery(@Language("SQL") String query) {
        System.out.println(ChatColor.AQUA + query);
        checkConnection();
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    private static boolean isConnected() {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private static void checkConnection() {
        if (!isConnected()) connect();
    }
}
