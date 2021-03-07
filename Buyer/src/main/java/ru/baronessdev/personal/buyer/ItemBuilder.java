package ru.baronessdev.personal.buyer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private String name;
    private int count = 1;
    private short data = 0;
    private List<String> lore;
    private Material material;
    private boolean isUnbreakable = false;

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder(ItemStack itemStack) {
        this.material = itemStack.getType();
        if (itemStack.hasItemMeta()) {
            if (itemStack.getItemMeta().getDisplayName() != null) this.name = itemStack.getItemMeta().getDisplayName();
            if (itemStack.getItemMeta().getLore() != null) this.lore = itemStack.getItemMeta().getLore();
        }
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        this.isUnbreakable = unbreakable;
        return this;
    }

    public ItemBuilder setLore(String... lines) {
        for (int i = 0; i < lines.length; i++) {
            lines[i] = ChatColor.WHITE + lines[i];
        }
        this.lore = Arrays.asList(lines);
        return this;
    }

    public ItemBuilder setLore(List<String> lines) {
        this.lore = lines;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.name = ChatColor.AQUA + this.name;
        return this;
    }

    public ItemBuilder setCount(int count) {
        if (count <= 0) this.count = 1;
        if (count > 64) this.count = 64;
        return this;
    }

    public ItemBuilder setData(short data) {
        this.data = data;
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public ItemStack build() {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (this.name != null) itemMeta.setDisplayName(this.name);
        if (this.lore != null) itemMeta.setLore(this.lore);
        itemStack.setAmount(this.count);
        itemStack.setDurability(this.data);
        itemMeta.setUnbreakable(isUnbreakable);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
