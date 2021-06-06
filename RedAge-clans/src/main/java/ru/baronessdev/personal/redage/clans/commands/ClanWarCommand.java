package ru.baronessdev.personal.redage.clans.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.baronessdev.personal.redage.clans.Data;
import ru.baronessdev.personal.redage.clans.obj.Clan;
import ru.baronessdev.personal.redage.clans.request.RequestManager;
import ru.baronessdev.personal.redage.clans.war.War;
import ru.baronessdev.personal.redage.clans.war.WarManager;
import ru.baronessdev.personal.redage.clans.war.WarType;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.util.SmartMessagesUtil;

@CommandAlias("clanwar|cw")
@SuppressWarnings("unused")
public class ClanWarCommand extends BaseCommand {

    @CatchUnknown
    @Default
    @HelpCommand
    public void unknown(Player p) {
        p.sendMessage(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Войны кланов" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
        p.sendMessage("/cw send [клан] [формат]" + ChatColor.RED + " - пригласить клан на кланвар;");
        p.sendMessage("/cw accept [клан]" + ChatColor.RED + " - принять приглашение на кланвар;");
        p.sendMessage("/cw join" + ChatColor.RED + " - присоединиться к команде для текущей войны;");
        p.sendMessage("/cw kick [игрок]" + ChatColor.RED + " - исключить игрока из команды для текущей войны;");
        p.sendMessage("/cw giveup" + ChatColor.RED + " - сдаться (моментальный проигрыш);");
        p.sendMessage("/cw gui" + ChatColor.RED + " - открыть меню клановой войны;");
        /*
        p.sendMessage(" ");
        new SmartMessagesUtil("Вы можете узнать больше о системе войн, " + ChatColor.RED + "нажав сюда.")
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Узнать больше").create()))
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://vk.com/baroness_dev"))
                .send(p);
         */
        p.sendMessage(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━" + ChatColor.RED + "─━─━─━─━─━─━╯");
    }

    @Subcommand("send")
    @CommandCompletion("@clans")
    public void send(Player p, String[] args) {
        if (args.length != 2) {
            unknown(p);
            return;
        }

        Clan clan = checkClan(p);
        if (clan == null) return;

        if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
            RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
            return;
        }

        if (clan.getName().equalsIgnoreCase(args[0])) {
            RedAge.say(p, ChatColor.RED + "Вы не можете объявить войну своему клану.");
            return;
        }

        Clan enemyClan = Data.getInstance().getClan(args[0]);
        if (enemyClan == null) {
            RedAge.say(p, ChatColor.RED + "Клан не существует.");
            return;
        }

        Player enemyLeader = Bukkit.getPlayer(enemyClan.getOwner());
        if (enemyLeader == null) {
            RedAge.say(p, ChatColor.RED + "Лидер вражеского клана не в сети.");
            return;
        }

        if (WarManager.warExists()) {
            RedAge.say(p, ChatColor.RED + "Дождитесь окончания текущей войны.");
            return;
        }

        WarType type;
        try {
            type = WarType.valueOf(args[1]);
        } catch (Exception e) {
            RedAge.say(p, "Доступные форматы: x3, x5, x10");
            return;
        }

        if (WarManager.warCount.getOrDefault(clan.getUuid(), 0) >= 3) {
            RedAge.say(p, "Вы превысили дневной лимит на количество войн.");
            return;
        }

        if (WarManager.warCount.getOrDefault(enemyClan.getUuid(), 0) >= 3) {
            RedAge.say(p, "Клан противника превысил дневной лимит на количество войн.");
            return;
        }

        if (!RequestManager.sendRequest(enemyLeader, "/cw accept " + clan.getName(), 60, () -> {
            Clan one = Data.getInstance().getClan(enemyClan.getUuid());
            Clan two = Data.getInstance().getClan(clan.getUuid());

            if (WarManager.warExists()) return;

            assert one != null;
            assert two != null;
            WarManager.scheduleWar(one, two, type);

            one.broadcast("Война скоро начнётся! Присоединяйтесь к ней, используя " + ChatColor.RED + "/cw join" + ChatColor.WHITE + ".");
            two.broadcast("Война скоро начнётся! Присоединяйтесь к ней, используя " + ChatColor.RED + "/cw join" + ChatColor.WHITE + ".");
        })) {
            RedAge.say(p, "У этого клана уже есть запрос от вас.");
            return;
        }

        enemyLeader.sendMessage("Клан " + clan.getName() + " приглашает вас провести войну [" + type.name() + "]! Запрос активен " + ChatColor.RED + "60 " + ChatColor.WHITE + "секунд.");
        new SmartMessagesUtil("Введите " + ChatColor.RED + "/cw accept " + clan.getName() + ChatColor.WHITE + " или " + ChatColor.RED + "нажмите сюда " + ChatColor.WHITE + "для принятия.")
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Начать войну").create()))
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cw accept " + clan.getName()))
                .send(enemyLeader);
        RedAge.say(p, "Вы отправили запрос на войну.");
    }

    @Subcommand("accept")
    @CommandCompletion("@clans")
    public void accept(Player p, String[] args) {
        if (args.length != 1) {
            unknown(p);
            return;
        }

        RedAge.say(p, "Этот клан не присылал вам запросов на войну.");
    }

    @Subcommand("join")
    public void join(Player p) {
        Clan clan = checkClan(p);
        if (clan == null) return;

        War war = WarManager.getWar(clan);
        if (war == null) {
            RedAge.say(p, "Ваш клан не готовится к войне.");
            return;
        }

        if (war.getTimer() != null) {
            RedAge.say(p, "Война уже началась.");
            return;
        }

        if (war.getFirstClanPlayers().contains(p) || war.getSecondClanPlayers().contains(p)) {
            RedAge.say(p, "Вы уже присоединились.");
            return;
        }

        int add = war.add(clan.getUuid(), p);
        if (add != -1) {
            RedAge.say(p, "Вы присоединились в боевой отряд своего клана.");
            clan.broadcast(ChatColor.RED + p.getName() + ChatColor.RESET + " присоединился к войне [" + add + "/" + war.getType().getPlayers() + "]");
        } else {
            RedAge.say(p, "Отряд для войны переполнен!");
        }
    }

    @Subcommand("kick")
    @CommandCompletion("@members")
    public void kick(Player p, String[] args) {
        if (args.length != 1) {
            unknown(p);
            return;
        }

        Clan clan = checkClan(p);
        if (clan == null) return;

        if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
            RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
            return;
        }

        War war = WarManager.getWar(clan);
        if (war == null) {
            RedAge.say(p, "Ваш клан не готовится к войне.");
            return;
        }

        if (war.getTimer() != null) {
            RedAge.say(p, "Война уже началась.");
            return;
        }

        int remove = war.remove(clan.getUuid(), Bukkit.getPlayer(args[0]));
        if (remove == -1) {
            RedAge.say(p, "Игрок не находится в вашем боевом отряде.");
            return;
        }

        clan.broadcast(ChatColor.RED + args[0] + ChatColor.RESET + " отстранён от войны [" + remove + "/" + war.getType().getPlayers() + "]");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Subcommand("giveup")
    public void giveup(Player p) {
        Clan clan = checkClan(p);
        if (clan == null) return;

        if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
            RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
            return;
        }

        War war = WarManager.getWar(clan);
        if (war == null) {
            RedAge.say(p, "Ваш клан не готовится к войне.");
            return;
        }

        if (war.getTimer() != null) {
            RedAge.say(p, "Война уже началась.");
            return;
        }

        WarManager.giveUp(clan);
    }


    private Clan checkClan(Player p) {
        Clan clan = Data.getInstance().getClan(p);
        if (clan == null) {
            RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
        }
        return clan;
    }
}
