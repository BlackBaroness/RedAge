package ru.baronessdev.personal.clans;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.baronessdev.personal.clans.obj.Clan;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "redageClans";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Baroness";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String params) {
        if (p == null) {
            return "";
        }

        if (params.equals("clan")) {
            Clan c = Data.getInstance().getClan(p);
            return (c == null) ? "" : c.getName() + " ";
        }

        return "";
    }
}
