package ru.baronessdev.personal.redage.kits;

import java.util.List;

public class Kit {

    private final String name;
    private final long delay;
    private final List<String> contain;

    public Kit(String name, long delay, List<String> contain) {
        this.name = name;
        this.delay = delay;
        this.contain = contain;
    }

    public String getName() {
        return name;
    }

    public List<String> getContain() {
        return contain;
    }

    public long getDelay() {
        return delay;
    }
}
