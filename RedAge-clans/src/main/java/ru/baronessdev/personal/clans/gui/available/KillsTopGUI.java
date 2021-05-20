package ru.baronessdev.personal.clans.gui.available;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.baronessdev.personal.clans.Data;
import ru.baronessdev.personal.clans.gui.base.GUI;
import ru.baronessdev.personal.redage.redagemain.util.Task;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class KillsTopGUI extends GUI {
    public KillsTopGUI(Player p) {
        super(p);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Inventory buildMenu() {
        menu = Bukkit.createInventory(null, 45, "Топ убийств на клановых войнах");
        AtomicInteger i = new AtomicInteger(1);

        Data.getInstance().getTopKills().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(stringIntegerEntry -> {
                    if (i.get() > 45) return;

                    ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    meta.setOwner(stringIntegerEntry.getKey());
                    meta.setDisplayName("§c§l" + stringIntegerEntry.getKey() + " §f(" + i + ")");
                    meta.setLore(ImmutableList.of("§c§lУбийств: §f" + stringIntegerEntry.getValue()));

                    item.setItemMeta(meta);
                    menu.addItem(item);
                    i.getAndIncrement();
                });

        return menu;
    }

    @Override
    public Task getAction(int slot) {
        return null;
    }
}
