package ru.baronessdev.personal.redage.redagemain;

import ru.baronessdev.personal.redage.redagemain.util.Task;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class SQLite {

    private final Consumer<Connection> connectTask;
    private final Connection connection;

    public SQLite(String tableName, String... args) {
        Connection tempConnection;
        try {
            tempConnection = DriverManager.getConnection("jdbc:sqlite://" + RedAge.getInstance().getDataFolder().getAbsolutePath() + File.separator + "sqlite.db");
        } catch (SQLException e) {
            tempConnection = null;
            RedAge.log("CRITICAL: Cannot connect to SQLite database");
            e.printStackTrace();
        }
        connection = tempConnection;

        HashMap<String, String> columns = new HashMap<>();
        List<String> extra = new ArrayList<>();
        String last = null;
        for (String arg : args) {
            if (arg.startsWith("# ")) {
                extra.add(arg.substring(2));
                continue;
            }

            if (last == null) {
                last = arg;
                continue;
            }

            columns.put(last, arg);
            last = null;
        }


        final StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
        columns.forEach((column, value) -> {
            builder.append("`")
                    .append(column)
                    .append("` ")
                    .append(value);
        });
        if (!extra.isEmpty()) extra.forEach(builder::append);
        builder.append(");");

        connectTask = (con -> execute(false, builder.toString()));
        connectTask.accept(connection);
    }

    public void execute(boolean async, final String query) {
        Task task = () -> {
            checkConnection();
            try {
                connection.createStatement().execute(query);
            } catch (SQLException e) {
                RedAge.log(query);
                e.printStackTrace();
            }
        };

        if (async) {
            ThreadUtil.execute(task);
        } else task.execute();

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
