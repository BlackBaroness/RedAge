package ru.baronessdev.priv.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Shop {

    public final HashMap<Integer, Product> products = new HashMap<>();
    private final String chunk;
    private final String name;

    public Shop(String chunk, String name) {
        this.name = name;
        this.chunk = chunk;
    }

    public String getChunk() {
        return chunk;
    }

    public String getName() {
        return name;
    }
}
