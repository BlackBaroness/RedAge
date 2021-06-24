package ru.baronessdev.personal.redage.redagemain.database.sqlite;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.Task;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class SQLite {

    private final Consumer<Connection> connectTask;
    private final Connection connection = RedAge.sqlite;
    private final String tableName;
    private final JavaPlugin plugin;

    protected SQLite(String tableName, List<Column> columns, List<String> extra, List<String> indexes, JavaPlugin plugin) {
        this.tableName = tableName;
        this.plugin = plugin;

        final StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
        columns.forEach(builder::append);

        if (!extra.isEmpty()) extra.forEach(builder::append);

        builder.deleteCharAt(builder.length() - 1);
        builder.append(");");

        connectTask = (con -> {
            execute(false, builder.toString());
            indexes.forEach(in -> execute(false, in));
        });
        connectTask.accept(connection);
    }

    public void execute(boolean async, String... queries) {
        synchronized (SQLite.class) {
            for (String query : queries) {
                query = setPlaceholder(query);
                log(query);
                String finalQuery = query;
                Task task = () -> {
                    checkConnection();
                    try {
                        connection.createStatement().execute(finalQuery);
                    } catch (SQLException e) {
                        RedAge.log(finalQuery);
                        e.printStackTrace();
                    }
                };

                if (async) {
                    ThreadUtil.execute(task);
                } else task.execute();
            }
        }
    }

    public ResultSet executeQuery(String query) {
        query = setPlaceholder(query);
        log(query);
        checkConnection();
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            RedAge.log(query);
            e.printStackTrace();
        }
        return rs;
    }

    private String setPlaceholder(String s) {
        return s.replace("!table!", "`" + tableName + "`");
    }

    private boolean isConnected() {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void checkConnection() {
        if (!isConnected()) connectTask.accept(connection);
    }

    private void log(String query) {
        System.out.println(ChatColor.LIGHT_PURPLE + "[SQLite] " + ChatColor.AQUA + "[" + plugin.getClass().getSimpleName() + "] " + query);
    }
}
