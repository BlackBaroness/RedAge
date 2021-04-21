package ru.baronessdev.personal.redage.kits;

import java.util.List;

public class Kit {

    private final String name;
    private final List<String> contain;

    public Kit(String name, List<String> contain) {
        this.name = name;
        this.contain = contain;
    }

    public String getName() {
        return name;
    }

    public List<String> getContain() {
        return contain;
    }
}
