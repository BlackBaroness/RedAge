package ru.baronessdev.personal.redage.clans.gui.available;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.baronessdev.personal.redage.clans.gui.available.comingSoon.WarHistoryGUI;
import ru.baronessdev.personal.redage.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.ItemBuilder;
import ru.baronessdev.personal.redage.redagemain.util.Task;

public class ClanwarGUI extends GUI {

    public ClanwarGUI(Player p) {
        super(p);
    }

    @Override
    public Inventory buildMenu() {
        menu = Bukkit.createInventory(null, 45, "Клановая война");

        menu.setItem(20, new ItemBuilder(Material.WATCH)
                .setName("§c§lИстория войн")
                .setLore("§fУзнай, как закончились прошлые битвы")
                .build()
        );

        menu.setItem(22, new ItemBuilder(Material.BOW)
                .setName("§c§lКлановая война")
                .setLore("§fОбъяви клановую войну")
                .build()
        );

        menu.setItem(24, new ItemBuilder(Material.TOTEM)
                .setName("§c§lТоп игроков")
                .setLore("§fУзнай, кто сделал большего всего убийств")
                .build()
        );

        return menu;
    }

    @Override
    public Task getAction(int slot) {
        if (clanRequired()) {
            return () -> {
            };
        }

        switch (slot) {

            case 20: {
                return () -> openMenu(new WarHistoryGUI(p));
            }

            case 22: {
                return () -> openMenu(new WarPossibleClansGUI(p));
            }

            case 24: {
                return () -> openMenu(new KillsTopGUI(p));
            }

            default:
                return null;
        }
    }
}
