package ru.baronessdev.personal.clans;

import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.baronessdev.personal.clans.enums.ChangeType;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.database.Column;
import ru.baronessdev.personal.redage.redagemain.database.ColumnType;
import ru.baronessdev.personal.redage.redagemain.database.SQLite;
import ru.baronessdev.personal.redage.redagemain.database.SQLiteBuilder;
import ru.baronessdev.personal.redage.redagemain.util.BooleanUtil;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.sql.ResultSet;
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


    private final List<Clan> clans = new ArrayList<>();

    private SQLite clansDatabase;
    private SQLite membersDatabase;
    private SQLite topKillsDatabase;

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

        // загрузка базы данных с топом убийств
        topKillsDatabase = new SQLiteBuilder("clans_top_kills")
                .addColumn(new Column("name", ColumnType.VARCHAR.setSize(100)))
                .addColumn(new Column("count", ColumnType.BIGINT))
                .addPrimaryKey("name")
                .addIndex("name")
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
            ResultSet membersResult = membersDatabase.executeQuery("SELECT * FROM !table! WHERE `uuid`='" + id + "'");
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

    public void saveClan(Clan clan, ChangeType changeType) {
        String query = null;

        switch (changeType) {
            case NAME: {
                query = "name ='" + clan.getName() + "'";
                break;
            }
            case OWNER: {
                query = "owner ='" + clan.getOwner() + "'";
                break;
            }
            case PREFIX: {
                query = "prefix ='" + clan.getPrefix() + "'";
                break;
            }
            case RATING: {
                query = "rating ='" + clan.getRating() + "'";
                break;
            }
            case HAS_BATTLE_PASS: {
                query = "has_battle_pass ='" + BooleanUtil.toInt(clan.isHasBattlePass()) + "'";
                break;
            }
            case BATTLE_PASS_POINTS: {
                query = "battle_pass_points ='" + clan.getBattlePassPoints() + "'";
                break;
            }
            case MEMBERS: {
                query = "members";
                break;
            }
        }

        if (query.equals("members")) {
            /* обновление данных об участниках
                чтобы не было конфликтов, делаем всё в одном большом потоке */
            ThreadUtil.execute(() -> {
                membersDatabase.execute(false, "DELETE FROM !table! WHERE uuid='" + clan.getUuid() + "'");
                clan.getMembers().forEach(member -> membersDatabase.execute(false, "INSERT INTO !table! (`name`, `uuid`)  VALUES " + String.format("('%s', '%s');", member, clan.getUuid().toString())));
            });
            return;
        }

        clansDatabase.execute(true, "UPDATE !table! SET " + query + " WHERE uuid = '" + clan.getUuid() + "'");
    }

    public void createClan(Clan clan) {
        clans.add(clan);
        clansDatabase.execute(false, "INSERT INTO !table! " +
                "(`prefix`, `name`, `uuid`, `owner`, `creation_date`, `rating`, `has_battle_pass`, `battle_pass_points`)  VALUES " + String.format("('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                clan.getPrefix(), clan.getName(), clan.getUuid().toString(), clan.getOwner().toLowerCase(), clan.getCreationTime(), clan.getRating(), BooleanUtil.toInt(clan.isHasBattlePass()), clan.getBattlePassPoints()));

        clan.syncMembers();
    }

    public void deleteClan(Clan clan) {
        clans.remove(clan);
        clansDatabase.execute(true, "DELETE FROM !table! WHERE uuid='" + clan.getUuid() + "'");
        membersDatabase.execute(true, "DELETE FROM !table! WHERE uuid='" + clan.getUuid() + "'");
    }

    public List<Clan> clanListByRating() {
        List<Clan> l = new ArrayList<>(clans);
        l.sort(Clan.COMPARE_BY_RATING);
        return l;
    }

    /*
        todo тут короче заброски истории войн, потом надо сделать

    public List<ArchiveWar> getArchiveWars(Clan clan) {
        File f = new File(RedAge.getInstance().getDataFolder() + File.separator + "acrhiveWars" + File.separator + clan.getUuid());
        if (!f.exists()) return new ArrayList<>();

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        List<ArchiveWar> l = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            if (cfg.contains(i + ""))
                l.add(loadArchiveWar(cfg.getConfigurationSection(i + "")));
        }
        return l;
    }

    private ArchiveWar loadArchiveWar(ConfigurationSection s) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        format.setTimeZone(TimeZone.getTimeZone("GMT+3"));

        return new ArchiveWar(
                s.getString("first"),
                s.getString("second"),
                s.getStringList("firstList"),
                s.getStringList("secondList"),
                s.getLong("date")
        );
    }

     */

    public List<Clan> getClans() {
        return new ArrayList<>(clans);
    }

    public boolean hasClan(Player p) {
        return getClan(p) != null;
    }
}
