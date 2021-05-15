package ru.baronessdev.personal.clans.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.baronessdev.personal.clans.Data;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.clans.request.RequestManager;
import ru.baronessdev.personal.clans.util.ItemBuilder;
import ru.baronessdev.personal.clans.util.SmartMessagesUtil;
import ru.baronessdev.personal.clans.war.WarManager;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.*;
import java.util.regex.Pattern;

@CommandAlias("clan|c")
@SuppressWarnings("unused")
public class ClanCommand extends BaseCommand {

    @CatchUnknown
    @Default
    @HelpCommand
    @CommandCompletion("@clanHelp")
    public void unknown(Player p) {
        Clan c = Data.getClan(p);
        String clan = (c != null) ? c.getName() : "нет";

        p.sendMessage(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Команды кланов" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
        p.sendMessage(ChatColor.BOLD + "Ваш клан: " + ChatColor.AQUA + "" + ChatColor.BOLD + clan + ((c != null)
                ? ChatColor.RESET + "" + ChatColor.BOLD + " c рейтингом " + ChatColor.AQUA + "" + ChatColor.BOLD + c.getRating()
                : ""));
        p.sendMessage("/c create [название]" + ChatColor.RED + " - создать клан;");
        p.sendMessage("/c invite [игрок]" + ChatColor.RED + " - пригласить игрока в клан;");
        p.sendMessage("/c kick [игрок]" + ChatColor.RED + " - исключить игрока из клана;");
        p.sendMessage("/c setflag" + ChatColor.RED + " - установить знамя;");
        p.sendMessage("/c leave" + ChatColor.RED + " - покинуть клан;");
        p.sendMessage("/c gui" + ChatColor.RED + " - открыть меню клана / боевого пропуска;");
        p.sendMessage("/c top" + ChatColor.RED + " - рейтинг кланов;");
        p.sendMessage("/cw" + ChatColor.RED + " - система войн;");

        p.sendMessage(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━─━" + ChatColor.RED + "─━─━─━─━─━─━╯");
    }

    @Subcommand("create")
    public void create(Player p, String[] args) {
        if (args.length != 2) {
            unknown(p);
            return;
        }

        if (Data.hasClan(p)) {
            RedAge.say(p, ChatColor.RED + "Сначала покиньте ваш текущий клан!");
            return;
        }

        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        if (!pattern.matcher(args[1]).matches()) {
            RedAge.say(p, ChatColor.RED + "Название клана может состоять только из латиницы, цифр и \"_\"!");
            return;
        }

        if (Data.nameExists(args[1])) {
            RedAge.say(p, ChatColor.RED + "Клан с таким названием уже существует!");
            return;
        }

        List<String> members = new ArrayList<>();
        members.add(p.getName().toLowerCase());
        Data.saveClan(new Clan(UUID.randomUUID(),
                new ItemStack(Material.STONE),
                args[1],
                p.getName(),
                0,
                false,
                0,
                members, new Date().getTime()));
        RedAge.broadcast(ChatColor.GREEN + p.getName() + " создаёт клан «" + args[1] + "»!");

        RedAge.say(p, ChatColor.AQUA + "Клан создан! " + ChatColor.WHITE + "Что же дальше?");
        RedAge.say(p, "Установите знамя своего клана через " + ChatColor.RED + "/clan setflag" + ChatColor.WHITE + ",");
        p.sendMessage("                     наберите соклановцев через " + ChatColor.RED + "/clan invite" + ChatColor.WHITE + "");
        p.sendMessage("                          и погружайтесь в противостояние кланов!");
    }

    @Subcommand("accept")
    @CommandCompletion("@clans")
    public void accept(Player p, String[] args) {
        if (args.length != 2) {
            unknown(p);
            return;
        }

        RedAge.say(p, ChatColor.RED + "Приглашение не найдено.");
    }

    @Subcommand("invite")
    @CommandCompletion("@players")
    public void invite(Player p, String[] args) {
        if (args.length != 2) {
            unknown(p);
            return;
        }

        Clan clan = Data.getClan(p);
        if (clan == null) {
            RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
            return;
        }

        if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
            RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
            return;
        }

        Player invited = Bukkit.getPlayer(args[1]);
        if (invited == null) {
            RedAge.say(p, ChatColor.RED + "Игрок не найден.");
            return;
        }

        if (Data.hasClan(invited)) {
            RedAge.say(p, ChatColor.RED + "Игрок уже состоит в клане.");
            return;
        }

        if (!RequestManager.sendRequest(invited,
                "/clan accept " + clan.getName(),
                30,
                () -> {
                    Clan actual = Data.getClan(clan.getUuid());
                    if (actual == null) return;

                    actual.getMembers().add(invited.getName().toLowerCase());
                    Data.saveClan(actual);

                    RedAge.say(invited, "Вы вступили в клан " + ChatColor.RED + actual.getName() + ChatColor.WHITE + ".");
                    actual.broadcast("В вашем клане новый участник: " + ChatColor.RED + invited.getName() + ChatColor.WHITE + "!");
                })) {
            RedAge.say(p, "У этого игрока уже есть приглашение от вас.");
        }

        invited.sendMessage("Клан " + clan.getName() + " приглашает вас вступить в свои ряды! Запрос активен " + ChatColor.RED + "30 " + ChatColor.WHITE + "секунд.");
        new SmartMessagesUtil("Введите " + ChatColor.RED + "/clan accept " + clan.getName() + ChatColor.WHITE + " или " + ChatColor.RED + "нажмите сюда " + ChatColor.WHITE + "для принятия.")
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Принять приглашение").create()))
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan accept " + clan.getName()))
                .send(invited);
        clan.broadcast("Игрок " + ChatColor.RED + invited.getName() + ChatColor.WHITE + " был приглашён в ваш клан.");
    }

    @Subcommand("kick")
    @CommandCompletion("@members")
    public void kick(Player p, String[] args) {
        if (args.length != 2) {
            unknown(p);
            return;
        }

        if (args[1].equalsIgnoreCase(p.getName())) {
            RedAge.say(p, ChatColor.RED + "Вы не можете исключить сами себя.");
            return;
        }

        Clan clan = Data.getClan(p);
        if (clan == null) {
            RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
            return;
        }

        if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
            RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
            return;
        }

        String nick = args[1].toLowerCase();
        if (!clan.getMembers().contains(nick)) {
            RedAge.say(p, ChatColor.RED + "Игрок не находится в клане.");
            return;
        }

        if (WarManager.getWar(clan) != null) {
            RedAge.say(p, ChatColor.RED + "Вы не можете исключить игрока во время войны.");
            return;
        }

        clan.getMembers().remove(nick);
        Data.saveClan(clan);

        clan.broadcast("Игрок " + ChatColor.RED + args[1] + ChatColor.WHITE + " был " + ChatColor.RED + "исключён " + ChatColor.WHITE + "из клана.");
        Optional.ofNullable(Bukkit.getPlayer(nick)).ifPresent(player -> RedAge.say(player, "Вы были " + ChatColor.RED + "исключены" + ChatColor.WHITE + " из клана " + ChatColor.RED + clan.getName() + ChatColor.WHITE + "."));
    }

    @Subcommand("setflag")
    public void setflag(Player p) {
        Clan clan = Data.getClan(p);
        if (clan == null) {
            RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
            return;
        }

        if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
            RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
            return;
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
            RedAge.say(p, "Возьмите любой предмет в руку, чтобы сделать его знаменем клана.");
            return;
        }

        clan.setIcon(p.getInventory().getItemInMainHand());
        Data.saveClan(clan);

        RedAge.say(p, "Вы успешно изменили знамя своего клана.");
    }

    @Subcommand("top")
    public void top(Player p) {
        Clan var = Data.getClan(p);
        String current = (var != null) ? var.getName() : "";

        Inventory menu = Bukkit.createInventory(null, 54, "Рейтинг кланов");
        int i = 0;
        for (Clan c : Data.clanListByRating()) {
            if (i == 54) break;

            Date date = new Date(c.getCreationTime());
            SimpleDateFormat format = new SimpleDateFormat("HH:mm dd.MM.yyyy");
            format.setTimeZone(TimeZone.getTimeZone("GMT+3"));

            ItemStack icon = new ItemBuilder(c.getIcon())
                    .setName(c.getName())
                    .setLore(
                            "Глава: " + ChatColor.GOLD + c.getOwner(),
                            "Участников: " + ChatColor.GOLD + c.getMembers().size(),
                            "Дата создания: " + ChatColor.GOLD + format.format(date),
                            " ",
                            "Рейтинг: " + ChatColor.GOLD + c.getRating(),
                            (current.equals(c.getName()) ? ChatColor.GREEN + "Вы состоите в этом клане." : "")
                    )
                    .build();
            menu.addItem(icon);
            i++;
        }

        p.openInventory(menu);
    }

    @Subcommand("leave")
    public void leave(Player p) {
        Clan clan = Data.getClan(p);
        if (clan == null) {
            RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
            return;
        }

        if (clan.getOwner().equals(p.getName().toLowerCase())) {
            RedAge.say(p, ChatColor.RED + "Владелец клана не может покинуть клан!");
            return;
        }

        clan.getMembers().remove(p.getName().toLowerCase());
        Data.saveClan(clan);
        clan.broadcast(ChatColor.RED + p.getName() + ChatColor.WHITE + " покинул клан.");
        RedAge.say(p, "Вы покинули клан.");
    }
}
