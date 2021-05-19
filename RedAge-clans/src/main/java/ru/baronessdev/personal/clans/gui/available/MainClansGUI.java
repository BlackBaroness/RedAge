package ru.baronessdev.personal.clans.gui.available;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.baronessdev.personal.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.ItemBuilder;
import ru.baronessdev.personal.redage.redagemain.util.Task;

public class MainClansGUI extends GUI {

    public MainClansGUI(Player p) {
        super(p);
    }

    @Override
    public Inventory buildMenu() {
        menu = Bukkit.createInventory(null, 45, "Клановое меню");

        menu.setItem(19, new ItemBuilder(Material.NETHER_STAR)
                .setName("§l§c§lBattle Pass")
                .build()
        );

        menu.setItem(21, new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("§l§c§lКлановая война")
                .build()
        );

        menu.setItem(23, new ItemBuilder(Material.BANNER)
                .setName("§l§c§lРейтинг кланов")
                .build()
        );

        menu.setItem(25, new ItemBuilder(Material.SKULL_ITEM)
                .setName("§l§c§lСписок соклановцев")
                .build()
        );

        menu.setItem(40, new ItemBuilder(Material.WOOL)
                .setData((short) 14)
                .setName("§l§c§lНазад")
                .build()
        );

        return null;
    }

    @Override
    public Task getAction(int slot) {
        switch (slot) {

            case 19: {
                return (clanRequired()) ? () -> {} : () -> openMenu(new BattlePassGUI(p));
            }

            case 21: {
                return (clanRequired()) ? () -> {} : () -> openMenu(new ClanwarGUI(p));
            }

            case 23: {
                return () -> p.performCommand("/c top");
            }

            case 25: {
                return (clanRequired()) ? () -> {} : () -> openMenu(new MembersGUI(p));
            }

            case 40: {
                return () -> p.performCommand("/menu");
            }

            default: return null;
        }
    }
}
