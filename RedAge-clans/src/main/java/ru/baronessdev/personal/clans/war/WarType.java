package ru.baronessdev.personal.clans.war;

public enum WarType {

    x3(3),
    x5(5),
    x10(10);

    private final int players;

    WarType(int players) {
        this.players = players;
    }

    public int getPlayers() {
        return players;
    }
}
