package ru.baronessdev.personal.buyer;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
    private Economy econ;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        load();
        setupEconomy();

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

        getCommand("reloadbuyer").setExecutor(((sender, command, label, args) -> {
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

        List<String> keys = new ArrayList<>(keysTest);
        for (String s : keysTest) {
            Material m = Material.getMaterial(s.toUpperCase());
            if (m == null) {
                System.out.println(ChatColor.RED + s + " is null!");
                keys.remove(s);
            }
        }

        for (int i = 0; i < 27; i++) {
            boolean wait = true;
            int lucky = 0;
            String s;
            Material material = null;
            while (wait) {
                lucky = ThreadLocalRandom.current().nextInt(keys.size());
                s = keys.get(lucky).toUpperCase();
                material = Material.getMaterial(s);
                if (keys.size() < 27) {
                    wait = false;
                } else wait = prices.containsKey(material);
            }

            int price = cfg.getInt(keys.get(lucky));

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
    private void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equals("/location")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(getLocation(e.getPlayer().getLocation()));
        }
    }

    @EventHandler
    private void onClick(PlayerInteractEntityEvent e) {
        if (getLocation(e.getRightClicked().getLocation()).equals(location)) {
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

        p.sendMessage(ChatColor.WHITE + "Вы получили " + ChatColor.RED + money + "$" + ChatColor.WHITE + ".");
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
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

    private String getLocation(Location l) {
        return l.getWorld().getName() + " # " + (int) l.getX() + " # " + (int) l.getY() + " # " + (int) l.getZ();
    }
}
