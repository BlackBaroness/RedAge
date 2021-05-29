package ru.baronessdev.personal.redage.clans.gui.available;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.baronessdev.personal.redage.clans.Data;
import ru.baronessdev.personal.redage.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.ItemBuilder;
import ru.baronessdev.personal.redage.redagemain.util.Task;

import java.util.UUID;

public class WarPossibleFormatsGUI extends GUI {

    private final String clan;

    public WarPossibleFormatsGUI(Player p, UUID clan) {
        super(p);
        this.clan = Data.getInstance().getClan(clan).getName();
    }

    @Override
    public Inventory buildMenu() {
        menu = Bukkit.createInventory(null, 9, "Выберите формат для войны");

        menu.setItem(2, new ItemBuilder(Material.WOOD_SWORD)
                .setName("§b3x3")
                .setLore(
                        "Формат ограничивает количество бойцов",
                        "для каждой из сторон.",
                        "",
                        "Этот формат позволит вам и вашим врагам",
                        "    иметь по §b3 человека§f в боевом отряде."
                )
                .build()
        );

        menu.setItem(4, new ItemBuilder(Material.IRON_SWORD)
                .setName("§b5x5")
                .setLore(
                        "Формат ограничивает количество бойцов",
                        "для каждой из сторон.",
                        "",
                        "Этот формат позволит вам и вашим врагам",
                        "    иметь по §b5 человек§f в боевом отряде."
                )
                .build()
        );

        menu.setItem(6, new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("§b10x10")
                .setLore(
                        "Формат ограничивает количество бойцов",
                        "для каждой из сторон.",
                        "",
                        "Этот формат позволит вам и вашим врагам",
                        "    иметь по §b10 человек§f в боевом отряде."
                )
                .build()
        );

        return menu;
    }

    @Override
    public Task getAction(int slot) {
        switch (slot) {
            case 2:
                return () -> send("x3");
            case 4:
                return () -> send("x5");
            case 6:
                return () -> send("x10");
            default:
                return null;
        }
    }

    private void send(String format) {
        p.closeInventory();
        p.performCommand("cw send " + clan + " " + format);
    }
}
