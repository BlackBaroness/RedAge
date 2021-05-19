package ru.baronessdev.personal.redage.redagemain.database;

import java.util.ArrayList;
import java.util.List;

public class SQLiteBuilder {

    private final String tableName;
    private final List<Column> columnList = new ArrayList<>();
    private final List<String> extra = new ArrayList<>();
    private final List<String> indexList = new ArrayList<>();

    public SQLiteBuilder(String tableName) {
        this.tableName = tableName;
    }

    public SQLiteBuilder addColumn(Column column) {
        columnList.add(column);
        return this;
    }

    public SQLiteBuilder addPrimaryKey(String name) {
        extra.add("PRIMARY KEY (`" + name + "`),");
        return this;
    }

    public SQLiteBuilder addIndex(String name) {
        StringBuilder b = new StringBuilder("(");
        columnList.forEach(column -> b.append(column.getName()).append(", "));

        b.deleteCharAt(b.length() - 1);
        b.deleteCharAt(b.length() - 1);

        b.append(")");

        indexList.add("CREATE INDEX IF NOT EXISTS " + name + " ON '" + tableName + "' " + b);
        return this;
    }

    public SQLite build() {
        return new SQLite(tableName, columnList, extra, indexList);
    }
}
