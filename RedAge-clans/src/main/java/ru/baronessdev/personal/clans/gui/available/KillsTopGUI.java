package ru.baronessdev.personal.clans.gui.available;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.baronessdev.personal.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.Task;

public class KillsTopGUI extends GUI {
    public KillsTopGUI(Player p) {
        super(p);
    }

    @Override
    public Inventory buildMenu() {
        return null;
    }

    @Override
    public Task getAction(int slot) {
        return null;
    }
}
