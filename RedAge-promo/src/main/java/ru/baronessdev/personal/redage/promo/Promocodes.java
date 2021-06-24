package ru.baronessdev.personal.redage.promo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.redagemain.ACF;
import ru.baronessdev.personal.redage.redagemain.AdminACF;
import ru.baronessdev.personal.redage.redagemain.RedAge;
import ru.baronessdev.personal.redage.redagemain.database.mysql.MySQL;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class Promocodes extends JavaPlugin {

    private MySQL database;
    private final List<Promo> promoList = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        database = new MySQL(this, "31.31.196.189",
                3306,
                "u1357059_site",
                "u1357059_site",
                "nK6iD7nS7beB1h",
                "useSSL=false");

        AdminACF.addCommand("promo", "- управление промокодами", new PromocodesAdminCommand());
        ACF.addCommand(new PromocodesPlayerCommand());

        update();
    }

    private void update() {
        promoList.clear();

        // постоянные
        try {
            ResultSet rs = database.executeQuery("SELECT * FROM `promocodes`");
            while (rs.next()) {
                String command;

                switch (rs.getInt("type")) {
                    case 0: {
                        command = "redage kit warrior <p>";
                        break;
                    }
                    case 1: {
                        command = "redage kit phantom <p>";
                        break;
                    }
                    case 2: {
                        command = "redage kit griefer <p>";
                        break;
                    }

                    default:
                        continue;
                }

                promoList.add(new Promo(
                        rs.getString("name"),
                        rs.getString("youtuber"),
                        Integer.MAX_VALUE,
                        command
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // временные
        getConfig().getKeys(false).stream()
                .filter(key -> !key.equals("data"))
                .forEach(key -> promoList.add(new Promo(
                        key,
                        "RedAge",
                        getConfig().getInt(key + ".usages"),
                        getConfig().getString(key + ".command")
                )));
    }

    @SuppressWarnings("unused")
    @CommandAlias("promo")
    class PromocodesPlayerCommand extends BaseCommand {

        @CommandCompletion("@nothing")
        @Default
        public void unknown(Player p) {
            RedAge.say(p, "Использование: /promo [промокод]");
            RedAge.say(p, "Вы можете узнать промокоды от ютуберов / в официальном паблике RedAge:");
            RedAge.say(p, "https://vk.com/redage_mc");
        }

        @CommandCompletion("@nothing")
        @CatchUnknown
        public void use(Player p, String[] args) {
            String promoName = args[0];

            Optional<Promo> promo = promoList.stream().filter(maybe -> maybe.getName().equals(promoName)).findFirst();
            if (promo.isEmpty()) {
                RedAge.say(p, "Промокода с названием §l" + promoName + "§f не существует.");
                return;
            }

            List<String> list = getConfig().getStringList("data." + p.getName());
            if (list.contains(promoName)) {
                RedAge.say(p, "Вы уже использовали этот промокод.");
                return;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), promo.get().getCommand().replace("<p>", p.getName()));

            list.add(promoName);
            getConfig().set("data." + p.getName(), list);
            saveConfig();

            promo.get().setUsages(promo.get().getUsages() - 1);

            if (promo.get().getUsages() == 0) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "redage promo del " + promoName);
            }

            RedAge.say(p, "Вы активировали промокод §l" + promoName + "§f");
            RedAge.say(p, "  от §l" + promo.get().getAuthor() + "§f.");
        }
    }

    @SuppressWarnings("unused")
    @CommandAlias("redage")
    @Subcommand("promo")
    @CommandPermission("admin")
    class PromocodesAdminCommand extends BaseCommand {

        @Default
        @CatchUnknown
        public void unknown(CommandSender p) {
            RedAge.say(p, "/redage promo add [имя] [кол-во] [команда] - добавить временный промокод");
            RedAge.say(p, "/redage promo del [имя] - удаляет промокод (только для временных)");
            RedAge.say(p, "/redage promo info [имя] - выводит инфу о промокоде");
            RedAge.say(p, "/redage promo list - выводит список промокодов");
            RedAge.say(p, "/redage promo reload - перезагружает промокоды");
        }

        @Subcommand("add")
        public void add(CommandSender p, String[] args) {
            if (args.length < 3) {
                unknown(p);
                return;
            }

            String name = args[0];

            if (name.equals("data")) {
                RedAge.say(p, "Имя data запрещено");
                return;
            }

            Optional<Promo> promo = promoList.stream().filter(maybe -> maybe.getName().equals(name)).findFirst();
            if (promo.isPresent()) {
                RedAge.say(p, "Имя занято");
                return;
            }

            int usages;
            try {
                usages = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                RedAge.say(p, "количество цифрой пиши дурак");
                return;
            }

            args = (String[]) ArrayUtils.remove(args, 0);
            args = (String[]) ArrayUtils.remove(args, 0);

            StringBuilder command = new StringBuilder();
            Arrays.stream(args).forEach(s -> command.append(s).append(" "));

            getConfig().set(name + ".usages", usages);
            getConfig().set(name + ".command", command.toString());
            saveConfig();

            update();
            RedAge.say(p, "Промокод добавлен");
        }

        @Subcommand("del")
        public void del(CommandSender p, String[] args) {
            if (args.length != 1) {
                unknown(p);
                return;
            }

            String name = args[0];

            Optional<Promo> promo = promoList.stream().filter(maybe -> maybe.getName().equals(name)).findFirst();
            if (promo.isEmpty()) {
                RedAge.say(p, "Промокод не найден");
                return;
            }

            getConfig().set(name, null);
            getConfig().getConfigurationSection("data").getKeys(false).forEach(s -> {
                List<String> l = getConfig().getStringList("data." + s);
                l.remove(name);
                getConfig().set("data." + s, l);
            });
            saveConfig();
            update();

            RedAge.say(p, "Промокод удалён. Если он не временный, он останется.");
            RedAge.say(p, "Также, сброшены использования этого промокода.");
        }

        @Subcommand("info")
        public void info(CommandSender p, String[] args) {
            if (args.length != 1) {
                unknown(p);
                return;
            }

            String name = args[0];

            Optional<Promo> pr = promoList.stream().filter(maybe -> maybe.getName().equals(name)).findFirst();
            if (pr.isEmpty()) {
                RedAge.say(p, "Промокод не найден");
                return;
            }

            Promo promo = pr.get();
            RedAge.say(p, "Название: " + promo.getName());
            RedAge.say(p, "Автор: " + promo.getAuthor());
            RedAge.say(p, "Команда: " + promo.getCommand());
            RedAge.say(p, "Использований осталось: " + promo.getUsages());
        }

        @Subcommand("list")
        public void list(CommandSender p) {
            RedAge.say(p, "=== Список промокодов ===");
            promoList.forEach(promo -> RedAge.say(p, promo.getName()));
            RedAge.say(p, "=========================");
        }

        @Subcommand("reload")
        public void reload(CommandSender p) {
            update();
            RedAge.say(p, "Промокоды перезагружены");
        }
    }
}
