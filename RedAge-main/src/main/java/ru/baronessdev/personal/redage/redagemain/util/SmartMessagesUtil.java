package ru.baronessdev.personal.redage.redagemain.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class SmartMessagesUtil {

    private final TextComponent message;

    public SmartMessagesUtil(String message) {
        this.message = new TextComponent(message);
    }

    public SmartMessagesUtil setHoverEvent(HoverEvent e) {
        message.setHoverEvent(e);
        return this;
    }

    public SmartMessagesUtil setClickEvent(ClickEvent e) {
        message.setClickEvent(e);
        return this;
    }

    public void send(Player p) {
        p.spigot().sendMessage(message);
    }
}
