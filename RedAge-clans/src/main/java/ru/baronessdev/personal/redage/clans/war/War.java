package ru.baronessdev.personal.redage.clans.war;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.baronessdev.personal.redage.clans.Data;
import ru.baronessdev.personal.redage.clans.obj.Clan;

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
    @Getter
    @Setter
    private BukkitRunnable timer;

    public War(UUID uuid, WarType type, UUID firstClan, UUID secondClan, List<Player> firstClanPlayers, List<Player> secondClanPlayers) {
        this.uuid = uuid;
        this.type = type;
        this.firstClan = firstClan;
        this.secondClan = secondClan;
        this.firstClanPlayers = firstClanPlayers;
        this.secondClanPlayers = secondClanPlayers;
    }

    public int add(UUID clan, Player p) {
        if (clan.equals(firstClan)) {
            if (firstClanPlayers.size() == type.getPlayers()) return -1; // переполнено
            firstClanPlayers.add(p);
            return firstClanPlayers.size();
        }

        if (clan.equals(secondClan)) {
            if (secondClanPlayers.size() == type.getPlayers()) return -1; // переполнено
            secondClanPlayers.add(p);
            return secondClanPlayers.size();
        }
        return -1;
    }

    public int remove(UUID clan, Player p) {
        if (p == null) return -1;
        if (clan.equals(firstClan)) {
            if (!firstClanPlayers.contains(p)) return -1; // отсутствует
            firstClanPlayers.remove(p);
            return firstClanPlayers.size();
        }

        if (clan.equals(secondClan)) {
            if (!secondClanPlayers.contains(p)) return -1; // отсутствует
            secondClanPlayers.remove(p);
            return secondClanPlayers.size();
        }
        return -1;
    }

    public Clan remove(Player p) {
        if (firstClanPlayers.contains(p)) {
            firstClanPlayers.remove(p);
            return Data.getInstance().getClan(firstClan);
        }

        if (secondClanPlayers.contains(p)) {
            secondClanPlayers.remove(p);
            return Data.getInstance().getClan(secondClan);
        }

        return null;
    }
}
