package ru.baronessdev.personal.jobs;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.personal.redage.RedAge;

public final class Jobs extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RedAge.registerAdminCommand("jobs", "- перезагружает точки работ", ((sender, args) -> {
            // todo релоад
            return true;
        }));
    }

    private boolean isMinerChunk(Location l) {
        String chunk = l.getChunk().toString();
        return getConfig().getStringList("minerChunks").stream().anyMatch(s -> s.equals(chunk));
    }

    private boolean isWoodcutterChunk(Location l) {
        String chunk = l.getChunk().toString();
        return getConfig().getStringList("woodcutterChunks").stream().anyMatch(s -> s.equals(chunk));
    }
}
