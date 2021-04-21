package ru.baronessdev.personal.clans.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.baronessdev.personal.clans.data.Data;
import ru.baronessdev.personal.clans.objects.Clan;
import ru.baronessdev.personal.clans.request.RequestManager;
import ru.baronessdev.personal.clans.util.SmartMessagesUtil;
import ru.baronessdev.personal.redage.RedAge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (args.length == 0) {
            help(p);
            return true;
        }

        switch (args[0]) {
            // clan create
            case "create": {
                if (args.length != 2) {
                    help(p);
                    return true;
                }

                if (Data.hasClan(p)) {
                    RedAge.say(p, ChatColor.RED + "Сначала покиньте ваш текущий клан!");
                    return true;
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
                        members));
                RedAge.broadcast(ChatColor.GREEN + p.getName() + " создаёт клан «" + args[1] + "»!");

                RedAge.say(p, ChatColor.AQUA + "Клан создан! " + ChatColor.WHITE + "Что же дальше?");
                RedAge.say(p, "Установите знамя своего клана через " + ChatColor.RED + "/clan gui" + ChatColor.WHITE + ",");
                p.sendMessage("                   наберите соклановцев через " + ChatColor.RED + "/clan invite" + ChatColor.WHITE + "");
                p.sendMessage("                        и погружайтесь в противостояние кланов!");
                return true;
            }

            // clan accept
            case "accept": {
                if (args.length != 2) {
                    help(p);
                    return true;
                }

                RedAge.say(p, ChatColor.RED + "Приглашение не найдено.");
                return true;
            }

            // clan invite
            case "invite": {
                if (args.length != 2) {
                    help(p);
                    return true;
                }

                Clan clan = Data.getClan(p);
                if (clan == null) {
                    RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
                    return true;
                }

                if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
                    RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
                    return true;
                }

                Player invited = Bukkit.getPlayer(args[1]);
                if (invited == null) {
                    RedAge.say(p, ChatColor.RED + "Игрок не найден.");
                    return true;
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
                    return true;
                }

                invited.sendMessage("Клан " + clan.getName() + " приглашает вас вступить в свои ряды! Запрос активен " + ChatColor.RED + "30 " + ChatColor.WHITE + "секунд.");
                new SmartMessagesUtil("Введите " + ChatColor.RED + "/clan accept " + clan.getName() + ChatColor.WHITE + " или " + ChatColor.RED + "нажмите сюда " + ChatColor.WHITE + "для принятия.")
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Принять приглашение").create()))
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan accept " + clan.getName()))
                        .send(invited);
                clan.broadcast("Игрок " + ChatColor.RED + invited.getName() + ChatColor.WHITE + " был приглашён в ваш клан.");
                return true;
            }

            // clan kick
            case "kick": {
                if (args.length != 2) {
                    help(p);
                    return true;
                }

                if (args[1].equalsIgnoreCase(p.getName())) {
                    RedAge.say(p, ChatColor.RED + "Вы не можете исключить сами себя.");
                    return true;
                }

                Clan clan = Data.getClan(p);
                if (clan == null) {
                    RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
                    return true;
                }

                if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
                    RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
                    return true;
                }

                String nick = args[1].toLowerCase();
                if (!clan.getMembers().contains(nick)) {
                    RedAge.say(p, ChatColor.RED + "Игрок не находится в клане.");
                    return true;
                }

                clan.getMembers().remove(nick);
                Data.saveClan(clan);

                clan.broadcast("Игрок " + ChatColor.RED + args[1] + ChatColor.WHITE + " был " + ChatColor.RED + "исключён " + ChatColor.WHITE + "из клана.");
                Optional.ofNullable(Bukkit.getPlayer(nick)).ifPresent(player -> RedAge.say(player, "Вы были " + ChatColor.RED + "исключены" + ChatColor.WHITE + " из клана " + ChatColor.RED + clan.getName() + ChatColor.WHITE + "."));
                return true;
            }

            // clan war
            case "war": {
                if (args.length == 1) {
                    helpWar(p);
                    return true;
                }

                Clan clan = Data.getClan(p);
                if (clan == null) {
                    RedAge.say(p, ChatColor.RED + "Вы не находитесь в клане.");
                    return true;
                }

                switch (args[1]) {
                    // clan war send
                    case "send": {
                        if (args.length != 4) {
                            helpWar(p);
                            return true;
                        }

                        if (!clan.getOwner().equalsIgnoreCase(p.getName())) {
                            RedAge.say(p, ChatColor.RED + "Вы не являетесь лидером клана.");
                            return true;
                        }

                        Clan enemyClan = Data.getClan(args[2]);
                        if (enemyClan == null) {
                            RedAge.say(p, ChatColor.RED + "Клан не существует.");
                            return true;
                        }

                        Player enemyLeader = Bukkit.getPlayer(enemyClan.getOwner());
                        if (enemyLeader == null) {
                            RedAge.say(p, ChatColor.RED + "Лидер вражеского клана не в сети.");
                            return true;
                        }


                    }
                }

                helpWar(p);
                return true;
            }
        }

        help(p);
        return true;
    }

    private void helpWar(Player p) {
        p.sendMessage(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Войны кланов" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
        p.sendMessage("/clan war send [клан] [формат]" + ChatColor.RED + " - пригласить клан на кланвар;");
        p.sendMessage("/clan war accept [клан]" + ChatColor.RED + " - принять приглашение на кланвар;");
        p.sendMessage("/clan war join" + ChatColor.RED + " - присоединиться к команде для текущей войны;");
        p.sendMessage("/clan war kick [игрок]" + ChatColor.RED + " - исключить игрока из команды для текущей войны;");
        p.sendMessage("/clan war giveup" + ChatColor.RED + " - сдаться (моментальный проигрыш);");
        p.sendMessage(" ");
        new SmartMessagesUtil("Вы можете узнать больше о системе войн, " + ChatColor.RED + "нажав сюда.")
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Узнать больше").create()))
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://vk.com/baroness_dev"))
                .send(p);
        p.sendMessage(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━" + ChatColor.RED + "─━─━─━─━─━─━╯");
    }

    private void help(Player p) {
        Clan c = Data.getClan(p);
        String clan = (c != null) ? c.getName() : "нет";

        p.sendMessage(ChatColor.RED + "╭━─━─━─━─━─━─≪ " + ChatColor.WHITE + "Команды кланов" + ChatColor.RED + " ≫─━─━─━─━─━─━╮");
        p.sendMessage(ChatColor.BOLD + "Ваш клан: " + ChatColor.AQUA + "" + ChatColor.BOLD + clan);
        p.sendMessage("/clan create [название]" + ChatColor.RED + " - создать клан;");
        p.sendMessage("/clan invite [игрок]" + ChatColor.RED + " - пригласить игрока в клан;");
        p.sendMessage("/clan kick [игрок]" + ChatColor.RED + " - исключить игрока из клана;");
        p.sendMessage("/clan war" + ChatColor.RED + " - подсказка по системе войн;");
        p.sendMessage("/clan gui" + ChatColor.RED + " - открыть меню клана / боевого пропуска;");
        p.sendMessage("/clan leave" + ChatColor.RED + " - покинуть клан;");
        p.sendMessage(ChatColor.RED + "╰━─━─━─━─━─━─" + ChatColor.WHITE + "━─━─━─━─━─━─━─━" + ChatColor.RED + "─━─━─━─━─━─━╯");
    }
}
