package ru.baronessdev.personal.redage.clans.gui.available;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.baronessdev.personal.redage.clans.Data;
import ru.baronessdev.personal.redage.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.ItemBuilder;
import ru.baronessdev.personal.redage.redagemain.util.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MembersGUI extends GUI {

    HashMap<Integer, Inventory> pages = new HashMap<>();
    int progress = 0;

    public MembersGUI(Player p) {
        super(p);
    }

    @Override
    public Inventory buildMenu() {
        List<String> members = new ArrayList<>(Data.getInstance().getClan(p).getMembers());
        int currentPage = 0;
        while (!members.isEmpty()) {
            currentPage++;

            Inventory page = createPage(currentPage, members);
            page.setItem(39, new ItemBuilder(Material.WOOL)
                    .setData((short) 14)
                    .setName("§c§lНазад")
                    .build());
            page.setItem(41, new ItemBuilder(Material.WOOL)
                    .setData((short) 13)
                    .setName("§a§lДалее")
                    .build());

            pages.put(currentPage, page);


            int i = 0;
            while (i < 28 && !members.isEmpty()) {
                members.remove(0);
                i++;
            }
        }

        updateProgressPlus();
        return menu;
    }

    @SuppressWarnings("deprecation")
    private Inventory createPage(int count, List<String> members) {
        Inventory inv = Bukkit.createInventory(null, 45, "Список соклановцев [Страница " + count + "]");
        int i = 0;
        for (String member : members) {
            i++;
            if (i > 27) return inv;

            ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(member);
            meta.setDisplayName("§c§l" + member);
            item.setItemMeta(meta);


            inv.setItem(i - 1, item);
        }
        return inv;
    }

    private void updateProgressMinus() {
        progress--;
        Inventory menu = pages.get(progress);
        if (menu == null) menu = pages.get(pages.size());

        this.menu = menu;
    }

    private void updateProgressPlus() {
        progress++;
        Inventory menu = pages.get(progress);
        if (menu == null) menu = pages.get(1);

        this.menu = menu;
    }

    @Override
    public Task getAction(int slot) {
        switch (slot) {

            case 39: {
                return () -> {
                    updateProgressMinus();
                    p.openInventory(menu);
                };
            }
            case 41: {
                return () -> {
                    updateProgressPlus();
                    p.openInventory(menu);
                };
            }

            default:
                return () -> {
                };
        }
    }
}
