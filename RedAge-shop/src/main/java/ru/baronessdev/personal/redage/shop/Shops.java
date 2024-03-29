package ru.baronessdev.personal.redage.shop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.money.Money;
import ru.baronessdev.personal.redage.redagemain.AdminACF;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.ArrayList;
import java.util.List;

public final class Shops extends JavaPlugin implements Listener {

    private final List<Shop> shops = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load();

        AdminACF.registerSimpleAdminCommand("shop", "- перезагружает все магазины", ((sender, args) -> {
            load();
            RedAge.say(sender, "Магазины перезагружены.");
            return true;
        }));

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void load() {
        shops.clear();
        reloadConfig();

        FileConfiguration cfg = getConfig();
        cfg.getKeys(false).forEach(key -> {
            Shop shop = new Shop(key, cfg.getString(key + ".chunk"));

            for (int i = 0; i < 9 * 6; i++) {
                if (!cfg.contains(key + "." + i)) continue;
                Product product = new Product(
                        cfg.getInt(key + "." + i + ".price"),
                        cfg.getItemStack(key + "." + i + ".icon"),
                        cfg.getString(key + "." + i + ".command")
                );

                shop.products.put(i, product);
            }
            shops.add(shop);
        });
    }

    @EventHandler
    private void onClick(PlayerInteractEntityEvent e) {
        String chunk = e.getRightClicked().getLocation().toString();
        Shop shop = shops.stream()
                .filter(maybeShop -> maybeShop.getChunk().equals(chunk))
                .findFirst().orElse(null);
        if (shop == null) return;
        e.setCancelled(true);

        Inventory inv = Bukkit.createInventory(null, 9 * 6, shop.getName());
        shop.products.forEach((key, value) -> inv.setItem(key, value.getIcon()));

        e.getPlayer().openInventory(inv);
    }

    @EventHandler
    private void onClickShop(InventoryClickEvent e) {
        String title = e.getWhoClicked().getOpenInventory().getTitle();
        Shop shop = shops.stream()
                .filter(maybeShop -> maybeShop.getName().equals(title))
                .findFirst().orElse(null);
        if (shop == null) return;
        Product product = shop.products.get(e.getSlot());
        if (product == null) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        p.closeInventory();

        int price = product.getPrice();
        int balance = Money.getBalance(p);
        if (balance < price) {
            p.sendMessage(ChatColor.RED + "Недостаточно средств для покупки!");
            return;
        }

        Money.setBalance(p, balance - price);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), product.getCommand().replace("<p>", p.getName()));
        p.sendMessage(ChatColor.GREEN + "Вы успешно приобрели товар за " + price + " redcoin.");
    }
}
