package ru.baronessdev.personal.clans.war;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class War {

    @Getter
    private final UUID uuid;
    @Getter
    private final WarType type;
    @Getter
    private final UUID firstClan;
    @Getter
    private final UUID secondClan;
    @Getter
    private final List<Player> firstClanPlayers;
    @Getter
    private final List<Player> secondClanPlayers;
    public War(UUID uuid, WarType type, UUID firstClan, UUID secondClan, List<Player> firstClanPlayers, List<Player> secondClanPlayers) {
        this.uuid = uuid;
        this.type = type;
        this.firstClan = firstClan;
        this.secondClan = secondClan;
        this.firstClanPlayers = firstClanPlayers;
        this.secondClanPlayers = secondClanPlayers;
    }
}
