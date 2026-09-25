package br.com.universe.hud;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;

public final class HudUniVersePlugin extends JavaPlugin implements Listener {

    private final Map<UUID, String> lastLocation = new HashMap<>();
    private final Map<UUID, BukkitTask> pendingLocationChecks = new HashMap<>();
        private final Map<UUID, BossBar> locationBars = new HashMap<>();
    private PlaceholderExpansion expansion;

    private final LegacyComponentSerializer legacy =
            LegacyComponentSerializer.legacySection();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("PlaceholderAPI não foi encontrado. O plugin será desativado.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        expansion = new HudUniVerseExpansion(this);
        expansion.register();

        Bukkit.getPluginManager().registerEvents(this, this);

        long refresh = Math.max(1L, getConfig().getLong("actionbar.refresh-ticks", 10L));
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!getConfig().getBoolean("actionbar.enabled", false)) {
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                sendActionbar(player);
            }
        }, refresh, refresh);

        getLogger().info("HudUniVerse 1.3.1 habilitado para Paper 26.2 / Java 25.");
    }

    @Override
    public void onDisable() {
        pendingLocationChecks.values().forEach(BukkitTask::cancel);
        locationBars.values().forEach(BossBar::removeAll);
        pendingLocationChecks.clear();
        locationBars.clear();
        lastLocation.clear();
        if (expansion != null) {
            expansion.unregister();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline()) {
                return;
            }
            String key = locationKey(player);
            lastLocation.put(player.getUniqueId(), key);

            if (getConfig().getBoolean("location-top.show-on-join", true)) {
                showLocationTop(player);
            }
        }, 10L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        queueLocationCheck(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getWorld() == event.getTo().getWorld()) {
            return;
        }

        queueLocationCheck(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        BukkitTask pending = pendingLocationChecks.remove(uuid);
        if (pending != null) {
            pending.cancel();
        }
        BossBar bar = locationBars.remove(uuid);
        if (bar != null) {
            bar.removePlayer(event.getPlayer());
        }
        lastLocation.remove(uuid);
    }

    /**
     * Towny can report a transient territory value while the player is crossing
     * a claim boundary. Debouncing the check prevents the HUD from flashing
     * "Wilderness -> Town -> Wilderness" for a single crossing.
     */
    private void queueLocationCheck(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask old = pendingLocationChecks.remove(uuid);
        if (old != null) {
            old.cancel();
        }

        long delay = Math.max(1L, getConfig().getLong("location-top.debounce-ticks", 6L));
        pendingLocationChecks.put(uuid, Bukkit.getScheduler().runTaskLater(this, () -> {
            pendingLocationChecks.remove(uuid);
            if (!player.isOnline()) {
                return;
            }

            String current = locationKey(player);
            String previous = lastLocation.put(uuid, current);

            if (previous != null && !previous.equals(current)
                    && getConfig().getBoolean("location-top.enabled", true)) {
                showLocationTop(player);
            }
        }, delay));
    }

    /**
     * Keeps the physical Towny territory visible permanently in the native
     * Minecraft bossbar text area. The bundled 26.2 resource pack hides the
     * bossbar graphics, leaving only this text visible at the top of the HUD.
     *
     * The bar is never hidden when crossing a border. Only its title changes,
     * which prevents the entry/exit flash caused by repeatedly creating and
     * removing bossbars.
     */
    public void showLocationTop(Player player) {
        String locationTown = getLocationTown(player);
        boolean wilderness = locationTown == null || locationTown.isBlank();

        String title;
        if (wilderness) {
            title = getConfig().getString("location-top.wilderness-title", "&7Wilderness");
        } else {
            title = getConfig().getString("location-top.town-title", "&a{town}")
                    .replace("{town}", locationTown);
        }

        String finalTitle = color(title);

        UUID uuid = player.getUniqueId();
        BossBar bar = locationBars.computeIfAbsent(uuid, id ->
                Bukkit.createBossBar(finalTitle, BarColor.WHITE, BarStyle.SOLID));

        bar.setTitle(finalTitle);
        // Keep the bossbar fully filled. The companion resource pack hides
        // both background and progress textures, so the player sees only text.
        bar.setProgress(1.0);
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        bar.setVisible(true);
    }

    private String locationKey(Player player) {
        String locationTown = getLocationTown(player);
        String world = player.getWorld().getName();

        if (locationTown == null || locationTown.isBlank()) {
            locationTown = getConfig().getString("fallbacks.location-town", "Wilderness");
        }

        return world + "|" + locationTown;
    }

    private void sendActionbar(Player player) {
        String format = getConfig().getString(
                "actionbar.format",
                "&b🌡 &f%huduniverse_temperature%°C &8| &d✦ &f%huduniverse_mana%&7/%huduniverse_mana_max% &8| &c⚔ &f%huduniverse_pvp_color%%huduniverse_pvp%"
        );

        String parsed = PlaceholderAPI.setPlaceholders(player, format);
        player.sendActionBar(legacy.deserialize(color(parsed)));
    }

    public String value(Player player, String placeholder, String fallback) {
        if (placeholder == null || placeholder.isBlank()) {
            return fallback;
        }

        String result = PlaceholderAPI.setPlaceholders(player, placeholder);

        if (result == null || result.isBlank() || result.equals(placeholder)
                || result.startsWith("%")) {
            return fallback;
        }

        return ChatColor.stripColor(result).trim().isEmpty() ? fallback : result;
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public String getSeason(Player player) {
        return value(player, getConfig().getString("placeholders.season"), "");
    }

    public String getTemperature(Player player) {
        return value(
                player,
                getConfig().getString("placeholders.temperature"),
                getConfig().getString("fallbacks.temperature", "--")
        );
    }

    public String getMana(Player player) {
        return value(
                player,
                getConfig().getString("placeholders.mana"),
                getConfig().getString("fallbacks.mana", "0")
        );
    }

    public String getManaMax(Player player) {
        return value(
                player,
                getConfig().getString("placeholders.mana-max"),
                getConfig().getString("fallbacks.mana-max", "0")
        );
    }

    /**
     * Returns the town the player belongs to (their Towny resident town).
     * This is intentionally different from getLocationTown(), which reports
     * the town whose claimed land the player is currently standing in.
     */
    public String getTown(Player player) {
        if (hasTowny()) {
            try {
                Town town = TownyAPI.getInstance().getTown(player);
                if (town != null) {
                    return town.getName();
                }
            } catch (Exception ignored) {
                // Fall back to PlaceholderAPI below.
            }
        }

        return value(player, getConfig().getString("placeholders.town"),
                getConfig().getString("fallbacks.town", "Sem cidade"));
    }

    /**
     * Returns the Towny town at the player's current physical location.
     * This is the value shown at the top/center location notification.
     */
    public String getLocationTown(Player player) {
        if (hasTowny()) {
            try {
                Town town = TownyAPI.getInstance().getTown(player.getLocation());
                return town == null ? "" : town.getName();
            } catch (Exception ignored) {
                // Fall back to PlaceholderAPI below.
            }
        }

        String placeholder = getConfig().getString(
                "placeholders.location-town",
                "%townyadvanced_player_location_town%");
        String result = value(player, placeholder, "");
        return result.equalsIgnoreCase("Wilderness") ? "" : result;
    }

    private boolean hasTowny() {
        return Bukkit.getPluginManager().getPlugin("Towny") != null;
    }

    public String getNation(Player player) {
        return value(player, getConfig().getString("placeholders.nation"),
                getConfig().getString("fallbacks.nation", ""));
    }

    public boolean isPvpEnabled(Player player) {
        return player.getWorld().getPVP();
    }

    public void reload() {
        reloadConfig();
    }

    public void showTest(Player player) {
        showLocationTop(player);
        sendActionbar(player);

        player.sendMessage(color(
                getConfig().getString("messages.prefix", "&2[&aHudUniVerse&2] &r")
                        + "&aHUD testado."
        ));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player) && args.length > 0 && args[0].equalsIgnoreCase("test")) {
            sender.sendMessage("Esse comando precisa ser executado por um jogador.");
            return true;
        }

        if (!sender.hasPermission("huduniverse.admin")) {
            sender.sendMessage(color("&cSem permissão."));
            return true;
        }

        String sub = args.length == 0 ? "status" : args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                reload();
                sender.sendMessage(color("&2[&aHudUniVerse&2] &aConfig recarregada."));
            }
            case "test" -> showTest((Player) sender);
            case "status" -> {
                sender.sendMessage(color("&2========== HudUniVerse =========="));
                sender.sendMessage(color("&aPaper: &f" + Bukkit.getServer().getMinecraftVersion()));
                sender.sendMessage(color("&aJava: &f" + System.getProperty("java.version")));
                sender.sendMessage(color("&aPlaceholderAPI: &f" +
                        (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null ? "OK" : "NÃO")));
                sender.sendMessage(color("&aTowny: &f" +
                        (Bukkit.getPluginManager().getPlugin("Towny") != null ? "OK" : "NÃO")));
                sender.sendMessage(color("&aAuraSkills: &f" +
                        (Bukkit.getPluginManager().getPlugin("AuraSkills") != null ? "OK" : "NÃO")));
                sender.sendMessage(color("&aRealisticSeasons: &f" +
                        (Bukkit.getPluginManager().getPlugin("RealisticSeasons") != null ? "OK" : "NÃO")));
                sender.sendMessage(color("&aTAB: &f" +
                        (Bukkit.getPluginManager().getPlugin("TAB") != null ? "OK" : "NÃO")));
                sender.sendMessage(color("&aCidade do jogador: &f" + getTown((Player) sender)));
                sender.sendMessage(color("&aLocalização atual: &f" + (getLocationTown((Player) sender).isBlank() ? "Wilderness" : getLocationTown((Player) sender))));
                sender.sendMessage(color("&aNação: &f" + getNation((Player) sender)));
                sender.sendMessage(color("&aTemporada: &f" + getSeason((Player) sender)));
                sender.sendMessage(color("&aTemperatura: &f" + getTemperature((Player) sender)));
                sender.sendMessage(color("&aMana: &f" + getMana((Player) sender) + "/" + getManaMax((Player) sender)));
                sender.sendMessage(color("&aPVP: &f" + (isPvpEnabled((Player) sender) ? "ON" : "OFF")));
                sender.sendMessage(color("&2================================="));
            }
            default -> sender.sendMessage(color(
                    "&a/huduniverse status &7| &a/huduniverse test &7| &a/huduniverse reload"
            ));
        }

        return true;
    }
}
