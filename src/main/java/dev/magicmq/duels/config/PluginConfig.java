package dev.magicmq.duels.config;

import dev.magicmq.duels.Duels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    private static FileConfiguration config;

    static {
        reload();
    }

    public static void reload() {
        config = Duels.get().getConfig();
    }

    public static ConfigurationSection getSqlInfo() {
        return config.getConfigurationSection("database");
    }

    public static long getAutosaveInterval() {
        return config.getLong("autosave-interval");
    }

    public static double getWorldBorderSize() {
        return config.getDouble("world-border-size");
    }

    public static int getPreGameTime() {
        return config.getInt("pre-game-time");
    }

    public static int getGameTime() {
        return config.getInt("game-time");
    }

    public static int getPostGameTime() {
        return config.getInt("post-game-time");
    }

    public static Location getSpawnLocation() {
        String[] split = config.getString("spawn-point").split(":");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public static ConfigurationSection getQueueGui() {
        return config.getConfigurationSection("queue-gui");
    }

    public static ConfigurationSection getKitGui() {
        return config.getConfigurationSection("kit-gui");
    }

    public static String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.plugin-prefix") + config.getString("messages." + key));
    }
}
