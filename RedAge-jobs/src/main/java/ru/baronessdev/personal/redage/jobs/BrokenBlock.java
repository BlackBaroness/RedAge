package ru.baronessdev.personal.redage.jobs;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

public class BrokenBlock {

    private final Block block;
    private final Material material;
    private final byte data;
    private final int typeID;
    private final Biome biome;

    public BrokenBlock(Block block, Material material, byte data, int typeID, Biome biome) {
        this.block = block;
        this.material = material;
        this.data = data;
        this.typeID = typeID;
        this.biome = biome;
    }

    @SuppressWarnings("deprecation")
    public void restore() {
        block.setType(material);
        block.setData(data);
        block.setTypeId(typeID);
        block.setBiome(biome);
    }
}
