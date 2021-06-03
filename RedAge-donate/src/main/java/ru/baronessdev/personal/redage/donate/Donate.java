package ru.baronessdev.personal.redage.donate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Language;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.Task;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public final class Donate extends JavaPlugin {

    private static Connection connection;

    @Override
    public void onEnable() {
        connect();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            ResultSet rs = executeQuery("SELECT * FROM `data`");
            try {
                HashMap<String, String> hashMap = new HashMap<>();
                while (rs.next()) {
                    String nick = "nick";
                    hashMap.put(
                            rs.getString(nick), rs.getString("group")
                    );
                    execute(true, "DELETE FROM `data` WHERE nick=" + nick);
                }

                hashMap.forEach((nick, group) -> {
                    String msh = (group.contains("key-"))
                            ? "crazycrates give V " + group.split("-")[1] + " " + group.split("-")[2]
                            : "lp user " + nick + " parent add " + group;

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), msh);
                    RedAge.log(msh);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 20 * 60);
    }

    private static void connect() {
        RedAge.log("Подключаюсь к MySQL");
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/donate",
                    "local_user",
                    "password"
            );

            execute(false, "CREATE TABLE IF NOT EXISTS `data` (" +
                    "`nick` varchar(30) NOT NULL," +
                    "`group` varchar(30) NOT NULL" +
                    ") ENGINE=InnoDB;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
