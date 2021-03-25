package ru.baronessdev.personal.clans.request;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.baronessdev.personal.clans.Clans;
import ru.baronessdev.personal.clans.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

public class RequestManager {

    private static final List<CommandListener> listeners = new ArrayList<>();

    public static boolean sendRequest(Player p, String command, int timeout, RequestTask task) {
        if (listeners.stream().anyMatch(l -> l.getCommand().equals(command))) {
            // такой реквест уже был отправлен игроку
            return false;
        }

        CommandListener listener = new CommandListener(p, command, task);
        listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, Clans.plugin);

        ThreadUtil.runLater(timeout, () -> cancelRequest(listener));
        return true;
    }

    private static void cancelRequest(CommandListener l) {
        listeners.remove(l);
        HandlerList.unregisterAll(l);
    }

    static class CommandListener implements Listener {

        private final Player p;
        private final String command;
        private final RequestTask task;

        CommandListener(Player p, String command, RequestTask task) {
            this.p = p;
            this.command = command;
            this.task = task;
        }

        public String getCommand() {
            return command;
        }

        @EventHandler
        private void onCommand(PlayerCommandPreprocessEvent e) {
            if (e.getMessage().equals(command) && e.getPlayer().equals(p)) {
                // реквест подтвержден
                task.process();
                e.setCancelled(true);
                cancelRequest(this);
            }
        }
    }
}
