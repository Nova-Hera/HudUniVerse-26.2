package br.com.universe.hud;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HudUniVerseExpansion extends PlaceholderExpansion {

    private final HudUniVersePlugin plugin;

    public HudUniVerseExpansion(HudUniVersePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "huduniverse";
    }

    @Override
    public @NotNull String getAuthor() {
        return "UniVerse";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "town" -> plugin.getTown(player);
            case "nation" -> plugin.getNation(player);
            case "location" -> {
                String location = plugin.getLocationTown(player);
                yield location == null || location.isBlank() ? plugin.getConfig().getString("fallbacks.location-town", "Wilderness") : location;
            }
            case "location_town" -> plugin.getLocationTown(player);
            case "season" -> plugin.getSeason(player);
            case "temperature" -> plugin.getTemperature(player);
            case "mana" -> plugin.getMana(player);
            case "mana_max" -> plugin.getManaMax(player);
            case "biome" -> plugin.getBiome(player);
            case "mana_percent" -> manaPercent(player);
            case "pvp" -> player.getWorld().getPVP()
                    ? plugin.getConfig().getString("pvp.enabled-text", "ON")
                    : plugin.getConfig().getString("pvp.disabled-text", "OFF");
            case "pvp_color" -> player.getWorld().getPVP()
                    ? plugin.getConfig().getString("pvp.enabled-color", "&a")
                    : plugin.getConfig().getString("pvp.disabled-color", "&c");
            case "world" -> player.getWorld().getName();
            case "x" -> String.valueOf(player.getLocation().getBlockX());
            case "y" -> String.valueOf(player.getLocation().getBlockY());
            case "z" -> String.valueOf(player.getLocation().getBlockZ());
            case "coordinates" -> "X: " + player.getLocation().getBlockX()
                    + " Y: " + player.getLocation().getBlockY()
                    + " Z: " + player.getLocation().getBlockZ();
            default -> null;
        };
    }

    private String manaPercent(Player player) {
        try {
            double mana = Double.parseDouble(plugin.getMana(player));
            double max = Double.parseDouble(plugin.getManaMax(player));
            if (max <= 0) {
                return "0";
            }
            return String.valueOf(Math.round((mana / max) * 100.0));
        } catch (NumberFormatException e) {
            return "0";
        }
    }
}
