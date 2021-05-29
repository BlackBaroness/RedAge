package ru.baronessdev.personal.redage.clans.obj;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.baronessdev.personal.redage.clans.Data;
import ru.baronessdev.personal.redage.clans.enums.ChangeType;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Clan {

    @Getter
    private final UUID uuid;
    @Getter
    private ItemStack icon;
    @Getter
    private String name;
    @Getter
    private String owner;
    @Getter
    private long rating;
    @Getter
    private boolean hasBattlePass;
    @Getter
    private long battlePassPoints;
    @Getter
    private final List<String> members;
    @Getter
    private String prefix;
    @Getter
    private final long creationTime;

    public Clan(UUID uuid, ItemStack icon, String name, String owner, long rating, boolean hasBattlePass, long battlePassPoints, List<String> members, String prefix, long creationTime) {
        this.uuid = uuid;
        this.icon = icon;
        if (icon == null) this.icon = new ItemStack(Material.COBBLESTONE);
        this.name = name;
        this.owner = owner;
        this.rating = rating;
        this.hasBattlePass = hasBattlePass;
        this.battlePassPoints = battlePassPoints;
        this.members = members;
        this.prefix = prefix;
        this.creationTime = creationTime;
    }

    public void broadcast(String s) {
        ThreadUtil.execute(() ->
                members.forEach(member -> Optional
                        .ofNullable(Bukkit.getPlayer(member))
                        .ifPresent(player -> RedAge.say(player, s))));
    }


    public void setIcon(ItemStack icon) {
        this.icon = icon;
        ThreadUtil.execute(() -> {
            File file = new File(RedAge.getInstance().getDataFolder() + File.separator + "clans_icons" + File.separator + uuid);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            yaml.set("-", icon);
            try {
                yaml.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setName(String name) {
        this.name = name;
        Data.getInstance().saveClan(this, ChangeType.NAME);
    }

    public void setOwner(String owner) {
        this.owner = owner;
        Data.getInstance().saveClan(this, ChangeType.OWNER);
    }

    public void setRating(long rating) {
        this.rating = rating;
        Data.getInstance().saveClan(this, ChangeType.RATING);
    }

    public void setHasBattlePass(boolean hasBattlePass) {
        this.hasBattlePass = hasBattlePass;
        Data.getInstance().saveClan(this, ChangeType.HAS_BATTLE_PASS);
    }

    public void setBattlePassPoints(long battlePassPoints) {
        this.battlePassPoints = battlePassPoints;
        Data.getInstance().saveClan(this, ChangeType.BATTLE_PASS_POINTS);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        Data.getInstance().saveClan(this, ChangeType.PREFIX);
    }

    public void syncMembers() {
        Data.getInstance().saveClan(this, ChangeType.MEMBERS);
    }

}
