package ru.baronessdev.personal.clans.gui.available;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.baronessdev.personal.clans.Data;
import ru.baronessdev.personal.clans.gui.base.GUI;
import ru.baronessdev.personal.clans.obj.Clan;
import ru.baronessdev.personal.clans.war.WarManager;
import ru.baronessdev.personal.redage.redagemain.util.ItemBuilder;
import ru.baronessdev.personal.redage.redagemain.util.Task;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WarPossibleClansGUI extends GUI {

    HashMap<Integer, UUID> clicks = new HashMap<>();

    public WarPossibleClansGUI(Player p) {
        super(p);
    }

    @Override
    public Inventory buildMenu() {
        menu = Bukkit.createInventory(null, 9, "Выберите клан для войны");
        Clan currentClan = Data.getInstance().getClan(p);
        if (currentClan == null) {
            return Bukkit.createInventory(null, 9, "Вы не состоите в клане");
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        format.setTimeZone(TimeZone.getTimeZone("GMT+3"));

        int i = 0;
        List<Clan> maybeClans = new ArrayList<>();
        Data.getInstance().getClans().stream()
                .filter(clan -> clan != currentClan)
                .filter(clan -> WarManager.warCount.getOrDefault(clan.getUuid(), 0) < 3)
                .filter(clan -> Bukkit.getPlayer(clan.getOwner()) != null)
                .forEach(maybeClans::add);

        int max = Math.min(maybeClans.size(), 8);
        while (clicks.size() != max) {
            int lucky = ThreadLocalRandom.current().nextInt(max);
            Clan clan = maybeClans.get(lucky);

            ItemStack icon = clan.getIcon();
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§b" + clan.getName());
            meta.setLore(ImmutableList.of(
                    "§fГлава: " + ChatColor.GOLD + clan.getOwner(),
                    "§fУчастников: " + ChatColor.GOLD + clan.getMembers().size(),
                    "§fДата создания: " + ChatColor.GOLD + format.format(new Date(clan.getCreationTime())),
                    "§fРейтинг: " + ChatColor.GOLD + clan.getRating(),
                    "",
                    "§cНажмите, чтобы объявить войну."
            ));
            icon.setItemMeta(meta);

            menu.setItem(i, icon);
            clicks.put(i, clan.getUuid());
            i++;
        }

        menu.setItem(8, new ItemBuilder(Material.DROPPER)
                .setName("§aОбновить список")
                .setLore(
                        "При каждом обновлении списка",
                        "вы получаете случайный набор кланов.",
                        "",
                        "Все кланы из списка могут воевать прямо сейчас."
                )
                .build()
        );
        return menu;
    }

    @Override
    public Task getAction(int slot) {
        if (slot == 8) {
            return () -> openMenu(new WarPossibleClansGUI(p));
        }

        if (clicks.containsKey(slot)) {
            return () -> openMenu(new WarPossibleFormatsGUI(p, clicks.get(slot)));
        }

        return null;
    }
}
