package ru.baronessdev.personal.redage.buyer;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class Buyer extends JavaPlugin implements Listener {

    private final int hour3 = 60 * 60 * 3;
    private int time = 0;
    private final Inventory inv = Bukkit.createInventory(null, 9 * 6, "Скупщик");
    private final HashMap<Material, Integer> prices = new HashMap<>();
    private String location;
    private final Economy econ = RedAge.getEconomy();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        load();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            time = time + 1;
            if (time == hour3) {
                time = 0;
                update();
            }

            int last = hour3 - time;
            int hours = last / 3600;
            int minutes = (last % 3600) / 60;
            int seconds = last % 60;
            String timeString = String.format("§fДо обновления: §c%02d:%02d:%02d", hours, minutes, seconds);
            inv.setItem(40, new ItemBuilder(Material.COMPASS).setName("§cОбновление списка предложений").setLore(timeString).build());
        }, 0, 20);

        RedAge.registerAdminCommand("buyer", "- перезагружает скупщика", ((sender, args) -> {
            update();
            return true;
        }));
    }

    private void update() {
        load();
        Bukkit.broadcastMessage("§f§lСкупщик обновлён! §c§lВремя распродаваться!");
        time = 0;
    }

    private void load() {
        reloadConfig();
        FileConfiguration cfg = getConfig();
        prices.clear();
        location = cfg.getString("loc");
        Set<String> keysSet = cfg.getKeys(false);
        List<String> keysTest = new ArrayList<>(keysSet);
        keysTest.remove("loc");

        List<Material> materials = new ArrayList<>();
        for (String s : keysTest) {
            Material m = Material.getMaterial(cfg.getString(s).toUpperCase());
            if (m == null) {
                System.out.println(ChatColor.RED + s + " is null! " + cfg.getString(s).toUpperCase());
            } else materials.add(m);
        }

        for (int i = 0; i < 27; i++) {
            Material material;
            int lucky;
            if (materials.size() < 27) {
                RedAge.log("СЛИШКОМ МАЛО ТОВАРОВ: " + materials.size());
                return;
            }

            lucky = ThreadLocalRandom.current().nextInt(materials.size());
            material = materials.get(lucky);

            int price = cfg.getInt((String.valueOf(lucky)));

            RedAge.log("Создаю новый товар: " + material);
            inv.setItem(i, new ItemBuilder(material).setLore(
                    ChatColor.WHITE + "\"Куплю за " + ChatColor.RED + price + ChatColor.WHITE + " монет.\"",
                    "",
                    ChatColor.GOLD + "§cНажмите §fЛКМ§c, чтобы продать §f1§c предмет",
                    " §cили §fПКМ§c, чтобы продать §fвсе§c предметы."

            ).build());
            prices.put(material, price);
        }
    }

    @EventHandler
    private void onClick(PlayerInteractEntityEvent e) {
        if (RedAge.formatLocation(e.getRightClicked().getLocation()).equals(location)) {
            e.setCancelled(true);
            e.getPlayer().openInventory(inv);
        }
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getTitle() == null) return;
        if (!e.getClickedInventory().getTitle().equals("Скупщик")) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) return;

        e.setCancelled(true);

        if (e.getSlot() > 27) return;
        Material item = e.getCurrentItem().getType();
        Player p = (Player) e.getWhoClicked();

        boolean right = e.getClick().isRightClick();
        int toTake = (right) ? howManyItems(p, item) : 1;

        if (toTake == 0) return;
        if (howManyItems(p, item) < toTake) return;

        removeItems(p, item, toTake);
        int money = toTake * prices.get(item);
        econ.depositPlayer(p, money);
        if (right) p.closeInventory();

        RedAge.say(p, "Вы получили " + ChatColor.RED + money + "$" + ChatColor.WHITE + ".");
    }

    private void removeItems(Player player, Material material, int amount) {
        for (int m = 0; m < player.getInventory().getContents().length; m++) {
            ItemStack item = player.getInventory().getItem(m);
            if (item == null) continue;

            if (item.getType().equals(material)) {
                int var = item.getAmount();
                if (var < amount) {
                    player.getInventory().setItem(m, new ItemStack(Material.AIR));
                    amount = amount - var;
                    continue;
                }
                item.setAmount(var - amount);
                player.getInventory().setItem(m, item);
                return;
            }
        }
    }

    private int howManyItems(Player player, Material material) {
        int i = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType().equals(material)) i = i + item.getAmount();
        }
        return i;
    }
}

