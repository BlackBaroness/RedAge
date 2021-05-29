package ru.baronessdev.personal.redage.clans.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.baronessdev.personal.redage.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.Task;

import java.util.Optional;

public class GuiHandler implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        Player p = (Player) e.getWhoClicked();
        GUI gui = CurrentGUI.memory.get(p);
        if (gui == null || !gui.getMenu().equals(e.getClickedInventory())) return;

        Optional<Task> optional = Optional.ofNullable(gui.getAction(e.getSlot()));
        optional.ifPresent(action -> {
            e.setCancelled(true);
            action.execute();
        });
    }
}
