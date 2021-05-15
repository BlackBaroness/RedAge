package ru.baronessdev.personal.clans.obj;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import ru.baronessdev.personal.clans.util.ThreadUtil;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Clan {

    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private ItemStack icon;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String owner;
    @Getter
    @Setter
    private int rating;
    @Getter
    @Setter
    private boolean hasBattlePass;
    @Getter
    @Setter
    private int battlePassPoints;
    @Getter
    @Setter
    private List<String> members;
    @Getter
    private final long creationTime;

    public Clan(UUID uuid, ItemStack icon, String name, String owner, int rating, boolean hasBattlePass, int battlePassPoints, List<String> members, long creationTime) {
        this.uuid = uuid;
        this.icon = icon;
        this.name = name;
        this.owner = owner.toLowerCase();
        this.rating = rating;
        this.hasBattlePass = hasBattlePass;
        this.battlePassPoints = battlePassPoints;
        this.members = members;
        this.creationTime = creationTime;
    }

    public void broadcast(String s) {
        ThreadUtil.execute(() ->
                members.forEach(member -> Optional
                        .ofNullable(Bukkit.getPlayer(member))
                        .ifPresent(player -> RedAge.say(player, s))));
    }

    public static final Comparator<Clan> COMPARE_BY_RATING = (lhs, rhs) -> lhs.getRating() + rhs.getRating();
}