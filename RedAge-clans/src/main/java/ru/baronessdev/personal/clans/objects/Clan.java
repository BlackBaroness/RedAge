package ru.baronessdev.personal.clans.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.clans.util.ThreadUtil;

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

    public Clan(UUID uuid, ItemStack icon, String name, String owner, int rating, boolean hasBattlePass, int battlePassPoints, List<String> members) {
        this.uuid = uuid;
        this.icon = icon;
        this.name = name;
        this.owner = owner.toLowerCase();
        this.rating = rating;
        this.hasBattlePass = hasBattlePass;
        this.battlePassPoints = battlePassPoints;
        this.members = members;
    }

    public void broadcast(String s) {
        ThreadUtil.execute(() ->
                members.forEach(member -> Optional
                        .ofNullable(Bukkit.getPlayer(member))
                        .ifPresent(player -> RedAge.say(player, s))));
    }
}
