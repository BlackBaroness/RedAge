package ru.baronessdev.personal.clans;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.clans.commands.ClanCommand;
import ru.baronessdev.personal.clans.data.Data;
import ru.baronessdev.personal.clans.objects.Clan;
import ru.baronessdev.personal.clans.war.WarManager;
import ru.baronessdev.personal.redage.redagemain.RedAge;

public final class Clans extends JavaPlugin {

    public static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        Data.setup(this);
        getCommand("clan").setExecutor(new ClanCommand());

        RedAge.registerAdminCommand("clans", "- управление системой кланов", ((sender, args) -> {
            if (args.length == 0) {
                helpAdmin(sender);
                return true;
            }

            switch (args[0]) {
                case "rename": {
                    if (args.length != 3)
                        helpAdmin(sender);

                    Clan clan = Data.getClan(args[1]);
                    if (clanNotExists(clan, sender)) return true;
                    assert clan != null;

                    RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " изменил название клана «" + clan.getName() + "» на «" + args[2] + "»");
                    clan.setName(args[2]);
                    Data.saveClan(clan);
                    return true;
                }
                case "delete": {
                    if (args.length != 2) {
                        helpAdmin(sender);
                        return true;
                    }

                    Clan clan = Data.getClan(args[1]);
                    if (clanNotExists(clan, sender)) return true;
                    assert clan != null;

                    Data.deleteClan(clan);
                    RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " удалил клан «" + clan.getName() + "»");
                    return true;
                }
                case "reload": {
                    Data.setup(this);
                    RedAge.say(sender, "Кланы перезагружены.");
                }
            }
            return true;
        }));

        WarManager.setup();
    }

    public static boolean clanNotExists(Clan c, CommandSender s) {
        if (c == null) {
            RedAge.say(s, ChatColor.RED + "Указанный клан не существует.");
            return true;
        }
        return false;
    }

    private void helpAdmin(CommandSender s) {
        RedAge.say(s, ChatColor.AQUA + "clans rename [клан] [название]" + ChatColor.WHITE + " - переименовать клан");
        RedAge.say(s, ChatColor.AQUA + "clans delete [клан]" + ChatColor.WHITE + " - удалить клан");
        RedAge.say(s, ChatColor.AQUA + "clans reload" + ChatColor.WHITE + " - перезагрузить плагин кланов");
    }
}
