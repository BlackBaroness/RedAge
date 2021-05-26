package ru.baronessdev.personal.redage.jobs;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.redagemain.RedAge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Jobs extends JavaPlugin implements Listener {

    List<BrokenBlock> reverseBlocks = new ArrayList<>();
    List<Player> cooldownPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RedAge.registerAdminCommand("jobs", "- перезагружает точки работ", ((sender, args) -> {
            reloadConfig();
            sender.sendMessage("дело сделано");
            return true;
        }));
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private boolean isMinerChunk(Location l) {
        String chunk = l.getChunk().toString();
        return getConfig().getStringList("minerChunks").stream().anyMatch(s -> s.equals(chunk));
    }

    private boolean isWoodChunk(Location l) {
        String chunk = l.getChunk().toString();
        return getConfig().getStringList("woodChunks").stream().anyMatch(s -> s.equals(chunk));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        if (cooldownPlayers.contains(p)) return;
        if (!p.getLocation().getWorld().getName().equals("spawn")) return;

        if (RedAge.formatLocation(e.getRightClicked().getLocation()).equals(getConfig().getString("minerLoc"))) {
            addCoolDown(p);
            e.setCancelled(true);
            // клик по майнеру
            double balance = getBalance(p, "miner");

            if (balance == 0) {
                sayMiner(p, "Хей, нужны деньги? Покопайся немного в шахте и возвращайся.");
                return;
            }

            String balanceString = String.format("%1$,.2f", balance);
            setBalance(p, "miner", 0);
            RedAge.getEconomy().depositPlayer(p, balance);
            sayMiner(p, "Отличная работа, продолжай в том же духе!");
            p.sendMessage(ChatColor.GREEN + "Вы заработали " + ChatColor.BOLD + balanceString + ChatColor.GREEN + "$.");
            return;
        }

        if (RedAge.formatLocation(e.getRightClicked().getLocation()).equals(getConfig().getString("woodLoc"))) {
            addCoolDown(p);
            e.setCancelled(true);
            // клик по лесорубу
            double balance = getBalance(p, "wood");

            if (balance == 0) {
                sayWood(p, "Ищешь способ подзаработать? Сруби-ка парочку деревьев.");
                sayWood(p, "Деньги за работу сможешь получить у меня.");
                return;
            }

            String balanceString = String.format("%1$,.2f", balance);
            setBalance(p, "wood", 0);
            RedAge.getEconomy().depositPlayer(p, balance);
            sayWood(p, "Неплохо, держи, заслужил.");
            p.sendMessage(ChatColor.GREEN + "Вы заработали " + ChatColor.BOLD + balanceString + ChatColor.GREEN + "$.");
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!p.getLocation().getWorld().getName().equals("spawn")) return;

        Block block = e.getBlock();
        Location loc = block.getLocation();
        boolean reverse = false;
        String type = "";
        double income = 0.0;
        int time = 0;

        if (isMinerChunk(loc) && block.getType().toString().toLowerCase().contains("ore")) {
            reverse = true;
            type = "miner";
            income = ThreadLocalRandom.current().nextDouble(0.03, 0.05);
            time = 90;
        }

        if (isWoodChunk(loc) && block.getType() == Material.LOG) {
            reverse = true;
            type = "wood";
            income = 0.04;
            time = 60;
        }

        if (reverse) {
            e.setCancelled(true);
            BrokenBlock brokenBlock = new BrokenBlock(block, block.getType(), block.getData(), block.getTypeId(), block.getBiome());
            Bukkit.getScheduler().runTaskLater(this, brokenBlock::restore, 20 * time);
            block.setType(Material.AIR);

            reverseBlocks.add(brokenBlock);

            double newBalance = getBalance(p, type) + income;
            setBalance(p, type, newBalance);

            String incomeString = String.format("%1$,.2f", income);
            String balanceString = String.format("%1$,.2f", newBalance);

            ActionBarAPI.sendActionBar(p, "+ " + ChatColor.RED + incomeString + ChatColor.RESET + "$ (Всего: " + ChatColor.RED + balanceString + ChatColor.RESET + "$)");
        }
    }

    private void sayMiner(Player p, String s) {
        p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Шахтёр Мейсон: " + ChatColor.RESET + s);
    }

    private void sayWood(Player p, String s) {
        p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Лесоруб Джек: " + ChatColor.RESET + s);
    }

    private double getBalance(Player p, String type) {
        return getConfig().getDouble(type + "Points." + p.getName(), 0);
    }

    private void setBalance(Player p, String type, double balance) {
        getConfig().set(type + "Points." + p.getName(), balance);
        saveConfig();
    }

    private void addCoolDown(Player p) {
        cooldownPlayers.add(p);
        Bukkit.getScheduler().runTaskLater(this, () -> cooldownPlayers.remove(p), 20);
    }

    @Override
    public void onDisable() {
        reverseBlocks.forEach(BrokenBlock::restore);
    }
}
