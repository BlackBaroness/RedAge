package ru.baronessdev.personal.redage.redagemain.database.sqlite;

public enum ColumnType {

    BIGINT,
    TINYINT,
    VARCHAR;

    int size = 0;

    public ColumnType setSize(int size) {
        if (this == VARCHAR) this.size = size;
        return this;
    }
}
