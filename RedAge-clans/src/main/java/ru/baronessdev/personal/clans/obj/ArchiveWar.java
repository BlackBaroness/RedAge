package ru.baronessdev.personal.clans.obj;

import java.util.List;

public class ArchiveWar {

    private final String firstClan;
    private final String secondClan;
    private final List<String> firstClanMembers;
    private final List<String> secondClanMembers;
    private final long date;

    public ArchiveWar(String firstClan, String secondClan, List<String> firstClanMembers, List<String> secondClanMembers, long date) {
        this.firstClan = firstClan;
        this.secondClan = secondClan;
        this.firstClanMembers = firstClanMembers;
        this.secondClanMembers = secondClanMembers;
        this.date = date;
    }
}
