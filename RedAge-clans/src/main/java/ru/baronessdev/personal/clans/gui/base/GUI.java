package ru.baronessdev.personal.clans.gui.base;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.baronessdev.personal.clans.Data;
import ru.baronessdev.personal.clans.gui.CurrentGUI;
import ru.baronessdev.personal.redage.redagemain.util.Task;

public abstract class GUI {

    protected final Player p;
    protected Inventory menu;

    public GUI(Player p) {
        this.p = p;
    }

    public abstract Inventory buildMenu();

    public abstract Task getAction(int slot);

    public Inventory getMenu() {
        return menu;
    }

    protected boolean clanRequired() {
        boolean b = !Data.getInstance().hasClan(p);
        if (b) p.closeInventory();
        return b;
    }

    protected void openMenu(GUI gui) {
        p.openInventory(gui.buildMenu());
        CurrentGUI.memory.put(p, gui);
    }
}
