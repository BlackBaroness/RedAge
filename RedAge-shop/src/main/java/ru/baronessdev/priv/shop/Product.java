package ru.baronessdev.priv.shop;

import org.bukkit.inventory.ItemStack;

public class Product {

    private final int price;
    private final ItemStack icon;
    private final String command;

    public Product(int price, ItemStack icon, String command) {
        this.price = price;
        this.icon = icon;
        this.command = command;
    }

    public int getPrice() {
        return price;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getCommand() {
        return command;
    }
}
