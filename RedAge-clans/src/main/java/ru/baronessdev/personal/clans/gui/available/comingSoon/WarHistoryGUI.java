package ru.baronessdev.personal.clans.gui.available.comingSoon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.baronessdev.personal.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.Task;

public class WarHistoryGUI extends GUI {

    public WarHistoryGUI(Player p) {
        super(p);
    }

    @Override
    public Inventory buildMenu() {
        return Bukkit.createInventory(null, 9, "Coming soon");
    }

    @Override
    public Task getAction(int slot) {
        return null;
    }
}