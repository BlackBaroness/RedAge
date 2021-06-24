package ru.baronessdev.personal.redage.redagemain.database.mysql;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Language;
import ru.baronessdev.personal.redage.redagemain.util.Task;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class MySQL {

    private Connection connection;

    private final JavaPlugin plugin;
    private final String ip;
    private final int port;
    private final String databaseName;
    private final String user;
    private final String password;
    private final String[] extra;

    public MySQL(JavaPlugin plugin, String ip, int port, String databaseName, String user, String password, String... extra) {
        this.plugin = plugin;
        this.ip = ip;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.extra = extra;

        connect();
    }

    private void connect() {
        try {
            StringBuilder extraArgs = new StringBuilder();
            Arrays.stream(extra).forEach(s -> extraArgs.append("?").append(s));

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + ip + ":" + port + "/" + databaseName + extraArgs,
                    user,
                    password
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void execute(boolean async, @Language("SQL") String query) {
        log(query);
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
    public ResultSet executeQuery(@Language("SQL") String query) {
        log(query);
        checkConnection();
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    private boolean isConnected() {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void checkConnection() {
        if (!isConnected()) connect();
    }

    private void log(@Language("SQL") String query) {
        System.out.println(ChatColor.LIGHT_PURPLE + "[MySQL] " + ChatColor.AQUA + "[" + plugin.getClass().getSimpleName() + "] " + query);
    }
}
