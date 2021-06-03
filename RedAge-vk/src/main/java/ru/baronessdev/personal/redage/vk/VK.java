package ru.baronessdev.personal.redage.vk;

import com.google.common.collect.ImmutableList;
import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;

public final class VK extends JavaPlugin implements Listener {

    List<String> protectedList = ImmutableList.of(
            "scotch3000",
            "Matt",
            "sanyanekurit",
            "Commie_kun",
            "heyw",
            "Black_Baroness",
            "amoralez",
            "Lucifer144"
    );

    HashMap<String, String> authorized = new HashMap<>();
    HashMap<String, Integer> registered = new HashMap<>();

    Group group;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getConfig().getKeys(false).forEach((key -> {
            if (key.equals("$")) return;

            registered.put(key, getConfig().getInt(key));
        }));

        Bukkit.getPluginManager().registerEvents(this, this);


        group = new Group("2a752c124aec4498ad662b743dcfa298badda22c2741ad3b1e7149b7c8bf0dcdc24724293d663bdd61646");

        group.onSimpleTextMessage(message -> {
                    String[] split = message.getText().split(" ");
                    if (split.length != 2) return;
                    if (!protectedList.contains(split[0])) return;

                    if (!registered.containsKey(split[0])) {
                        getConfig().set(split[0], message.authorId());
                        saveConfig();
                        registered.put(split[0], message.authorId());
                    } else {
                        Integer obj = registered.get(split[0]);
                        Integer id = message.authorId();
                        if (!id.equals(obj)) return;
                    }

                    authorized.put(split[0], split[1]);

                    new Message()
                            .from(group)
                            .to(message.authorId())
                            .text("OK")
                            .send();
                }
        );

        Logger coreLogger = (Logger) org.apache.logging.log4j.LogManager.getRootLogger();
        coreLogger.addFilter(new LogFilter());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        String nick = e.getName();
        if (!protectedList.contains(nick)) return;

        String ip = e.getAddress().toString().replace("/", "");
        if (!authorized.containsKey(nick) || !authorized.get(nick).equals(ip)) {
            disallow(nick, ip, e);
            return;
        }

        new Message().from(group)
                .to(registered.get(nick))
                .text("Выполнен вход с IP адреса " + ip)
                .send();
    }

    private void disallow(String nick, String ip, AsyncPlayerPreLoginEvent e) {
        e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        e.setKickMessage(
                "Система защиты отклонила ваш вход \n" +
                        "Ник: " + nick + "\n" +
                        "IP: " + ip
        );

        new Message().from(group)
                .to(registered.get(nick))
                .text("Предотвращён вход с IP адреса " + ip)
                .send();
    }
}
