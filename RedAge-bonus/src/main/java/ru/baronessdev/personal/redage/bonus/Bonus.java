package ru.baronessdev.personal.redage.bonus;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.redagemain.ACF;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class Bonus extends JavaPlugin {

    @Override
    public void onEnable() {
        ACF.addCommand(new BonusCommand());
    }

    private long needTime(Player p) {
        long x = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "data.yml")).getLong(p.getName());
        if (x == 0) return 0;

        long y = 86400000;
        long z = System.currentTimeMillis();

        return (z - y > x) ? 0 : ~(z - (x + y));
    }

    @CommandAlias("bonus")
    @SuppressWarnings("unused")
    public class BonusCommand extends BaseCommand {

        @CatchUnknown
        @Default
        public void use(Player p) {
            long needTime = needTime(p);

            if (needTime != 0) {
                long last = TimeUnit.MILLISECONDS.toSeconds(needTime);
                long hours = last / 3600;
                long minutes = (last % 3600) / 60;
                long seconds = last % 60;

                RedAge.say(p, String.format("§fДо следующего сбора бонуса: §c%d:%d:%d", hours, minutes, seconds));
                return;
            }

            File f = new File(getDataFolder() + File.separator + "data.yml");
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            cfg.set(p.getName(), System.currentTimeMillis());
            ThreadUtil.execute(() -> {
                try {
                    cfg.save(f);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            int random = ThreadLocalRandom.current().nextInt(100, 1001);
            RedAge.getEconomy().depositPlayer(p, random);

            RedAge.say(p, ChatColor.GREEN + "Вы получили бонус в размере " + ChatColor.BOLD + random + ChatColor.RESET + ChatColor.GREEN + "$!");
        }
    }
}
