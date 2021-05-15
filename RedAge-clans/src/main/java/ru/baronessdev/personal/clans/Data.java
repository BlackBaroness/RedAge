package ru.baronessdev.personal.clans;

import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.database.Column;
import ru.baronessdev.personal.redage.redagemain.database.ColumnType;
import ru.baronessdev.personal.redage.redagemain.database.SQLite;
import ru.baronessdev.personal.redage.redagemain.database.SQLiteBuilder;
import ru.baronessdev.personal.redage.redagemain.util.BooleanUtil;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Data {

    // ============ SINGLETON ============
    private static volatile Data instance;

    public static Data getInstance() {
        Data localInstance = instance;
        if (localInstance == null) {
            synchronized (Data.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Data();
                }
            }
        }
        return localInstance;
    }
    // ===================================


    private List<Clan> clans;

    private SQLite clansDatabase;
    private SQLite membersDatabase;

    @SneakyThrows
    protected void setup() {
        // загрузка базы данных с кланами
        clansDatabase = new SQLiteBuilder("clans_clans")
                .addColumn(new Column("prefix", ColumnType.VARCHAR.setSize(20)).setDefaultValue(""))
                .addColumn(new Column("name", ColumnType.VARCHAR.setSize(100)))
                .addColumn(new Column("uuid", ColumnType.VARCHAR.setSize(100)))
                .addColumn(new Column("owner", ColumnType.VARCHAR.setSize(100)))
                .addColumn(new Column("creation_date", ColumnType.BIGINT))
                .addColumn(new Column("rating", ColumnType.BIGINT).setDefaultValue(0))
                .addColumn(new Column("has_battle_pass", ColumnType.TINYINT).setDefaultValue(0))
                .addColumn(new Column("battle_pass_points", ColumnType.BIGINT).setDefaultValue(0))
                .addPrimaryKey("uuid")
                .addIndex("name")
                .addIndex("uuid")
                .build();

        // загрузка базы данных с участниками
        membersDatabase = new SQLiteBuilder("clans_members")
                .addColumn(new Column("name", ColumnType.VARCHAR.setSize(100)))
                .addColumn(new Column("uuid", ColumnType.VARCHAR.setSize(100)))
                .addPrimaryKey("name")
                .addIndex("uuid")
                .build();

        // кэширование всех кланов
        ResultSet rs = clansDatabase.executeQuery("SELECT * FROM !table!");
        while (rs.next()) {
            String prefix = rs.getString("prefix");
            String name = rs.getString("name");
            UUID id = UUID.fromString(rs.getString("uuid"));
            ItemStack icon = YamlConfiguration.loadConfiguration(new File(RedAge.getInstance().getDataFolder() + File.separator + "clans_icons" + File.separator + id)).getItemStack("-");
            String owner = rs.getString("owner");
            long creationDate = rs.getLong("creation_date");
            long rating = rs.getLong("rating");
            boolean hasBattlePass = BooleanUtil.fromInt(rs.getInt("has_battle_pass"));
            long battlePassPoints = rs.getLong("battle_pass_points");

            List<String> members = new ArrayList<>();
            ResultSet membersResult = membersDatabase.executeQuery("SELECT * FROM !table! WHERE `uuid`=" + id);
            while (membersResult.next()) {
                members.add(membersResult.getString("name"));
            }

            clans.add(new Clan(id, icon, name, owner, rating, hasBattlePass, battlePassPoints, members, prefix, creationDate));
        }
    }

    public Clan getClan(String name) {
        for (Clan clan : clans) {
            if (clan.getName().equals(name)) return clan;
        }
        return null;
    }

    public Clan getClan(UUID uuid) {
        for (Clan clan : clans) {
            if (clan.getUuid().equals(uuid)) return clan;
        }
        return null;
    }


    public Clan getClan(Player p) {
        String lowerCase = p.getName().toLowerCase();
        for (Clan clan : clans) {
            if (clan.getMembers().contains(lowerCase)) return clan;
        }
        return null;
    }

    public void saveClan(Clan clans) {
        clansDatabase.execute(true, );
            ThreadUtil.execute(() -> {
                String path = clan.getUuid().toString();

                core.getConfig().set(path + ".icon", clan.getIcon());
                core.getConfig().set(path + ".name", clan.getName());
                core.getConfig().set(path + ".owner", clan.getOwner());
                core.getConfig().set(path + ".rating", clan.getRating());
                core.getConfig().set(path + ".hasBattlePass", clan.isHasBattlePass());
                core.getConfig().set(path + ".battlePassPoints", clan.getBattlePassPoints());
                core.getConfig().set(path + ".members", clan.getMembers());
                core.getConfig().set(path + ".creationTime", clan.getCreationTime());
                core.saveConfig();
            });
        }
    }

    public static void saveClan(Clan... clans) {
        for (Clan c : clans) saveClan(c);
    }

    public static void deleteClan(Clan clan) {
        core.getConfig().set(clan.getUuid().toString(), null);
        core.saveConfig();
    }

    public static List<Clan> clanListByRating() {
        List<Clan> l = new ArrayList<>();
        core.getConfig().getKeys(false).forEach(k -> {
            if (!k.equals("settings")) {
                l.add(getClan(UUID.fromString(k)));
            }
        });

        l.sort(Clan.COMPARE_BY_RATING);
        return l;
    }
}
