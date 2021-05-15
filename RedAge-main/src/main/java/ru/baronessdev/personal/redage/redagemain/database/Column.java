package ru.baronessdev.personal.redage.redagemain.database;

public class Column {

    private final String name;
    private final ColumnType type;
    private boolean notNull = true;
    private String defaultValue;

    public Column(String name, ColumnType type) {
        this.name = name;
        this.type = type;
    }

    public Column setDefaultValue(Object defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
        return this;
    }

    public Column setNotNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }

    @Override
    public String toString() {
        return "`" + name + "` " + type +
                ((type.size != 0) ? type.size : "") +
                ((notNull) ? " NOT NULL" : "") +
                ((defaultValue != null)
                        ? " DEFAULT '" + defaultValue + "'"
                        : "") +
                ",";
    }

    public String getName() {
        return name;
    }
}
