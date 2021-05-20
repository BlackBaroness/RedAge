package ru.baronessdev.personal.redage.redagemain.database;

import org.bukkit.ChatColor;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.Task;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class SQLite {

    private final Consumer<Connection> connectTask;
    private final Connection connection;
    private final String tableName;

    public SQLite(String tableName, List<Column> columns, List<String> extra, List<String> indexes) {
        this.tableName = tableName;
        Connection tempConnection;
        try {
            tempConnection = DriverManager.getConnection("jdbc:sqlite://" + RedAge.getInstance().getDataFolder().getAbsolutePath() + File.separator + "sqlite.db");
        } catch (SQLException e) {
            tempConnection = null;
            RedAge.log("CRITICAL: Cannot connect to SQLite database");
            e.printStackTrace();
        }
        connection = tempConnection;


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
                System.out.println(ChatColor.AQUA + query);
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
        System.out.println(ChatColor.AQUA + query);
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
}
