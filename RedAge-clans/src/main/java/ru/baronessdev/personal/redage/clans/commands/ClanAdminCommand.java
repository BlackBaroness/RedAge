package ru.baronessdev.personal.redage.clans.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import ru.baronessdev.personal.redage.clans.ClansPlugin;
import ru.baronessdev.personal.redage.clans.Data;
import ru.baronessdev.personal.redage.clans.obj.Clan;
import ru.baronessdev.personal.redage.redagemain.RedAge;

@SuppressWarnings("unused")
@CommandAlias("redage")
@Subcommand("clans")
public class ClanAdminCommand extends BaseCommand {

    @CatchUnknown
    @Default
    public void unknown(CommandSender sender) {
        help(sender);
    }

    @CommandCompletion("@clans")
    @Subcommand("rename")
    public void rename(CommandSender sender, String[] args) {
        if (args.length != 2) {
            help(sender);
        }

        Clan clan = Data.getInstance().getClan(args[1]);
        if (ClansPlugin.clanNotExists(clan, sender)) return;

        RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " изменил название клана «" + clan.getName() + "» на «" + args[2] + "»");
        clan.setName(args[2]);
    }

    @CommandCompletion("@clans")
    @Subcommand("delete")
    public void delete(CommandSender sender, String[] args) {
        if (args.length != 1) {
            help(sender);
        }

        Clan clan = Data.getInstance().getClan(args[1]);
        if (ClansPlugin.clanNotExists(clan, sender)) return;

        Data.getInstance().deleteClan(clan);
        RedAge.broadcast(ChatColor.AQUA + "Администратор " + sender.getName() + " удалил клан «" + clan.getName() + "»");
    }

    private void help(CommandSender s) {
        RedAge.say(s, ChatColor.AQUA + "clans rename [клан] [название]" + ChatColor.WHITE + " - переименовать клан");
        RedAge.say(s, ChatColor.AQUA + "clans delete [клан]" + ChatColor.WHITE + " - удалить клан");
    }
}
