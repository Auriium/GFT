package dev.magicmq.duels.config;

import dev.magicmq.duels.Duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PluginConfig {

    private static FileConfiguration config;

    static {
        config = Duels.get().getConfig();
    }

    public static ConfigurationSection getSqlInfo() {
        return config.getConfigurationSection("database");
    }

    public static long getAutosaveInterval() {
        return config.getLong("autosave-interval");
    }

    public static float getSoundVolume() {
        return (float) config.getDouble("sound-volume");
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

    public static String getLobbyServerName() {
        return config.getString("lobby-server-name");
    }

    public static List<String> getTeamOneSpawns(String name) {
        return config.getStringList("game-spawns." + name + ".team-one");
    }

    public static List<String> getTeamTwoSpawns(String name) {
        return config.getStringList("game-spawns." + name + ".team-two");
    }

    public static List<String> getAllowedBlocks() {
        return config.getStringList("allowed-blocks");
    }

    public static List<String> getWinCommands() {
        return config.getStringList("win-commands");
    }

    public static ConfigurationSection getQueueGui() {
        return config.getConfigurationSection("queue-gui");
    }

    public static String getDefaultKit() {
        return config.getString("default-kit");
    }

    public static String getKitsGuiName() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("kits-gui-name"));
    }

    public static ConfigurationSection getSpawnHotbar() {
        return config.getConfigurationSection("spawn-hotbar");
    }

    public static ConfigurationSection getPreGameHotbar() {
        return config.getConfigurationSection("pre-game-hotbar");
    }

    public static ConfigurationSection getSpectateHotbar() {
        return config.getConfigurationSection("spectate-hotbar");
    }

    public static String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.plugin-prefix") + config.getString("messages." + key));
    }

    public static String getBareMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages." + key));
    }

    public static HashMap<Integer, String> getPreGameCountdown() {
        HashMap<Integer, String> messages = new HashMap<>();
        for (String key : config.getConfigurationSection("messages.pre-game-countdown").getKeys(false)) {
            messages.put(Integer.parseInt(key), ChatColor.translateAlternateColorCodes('&', config.getString("messages.plugin-prefix") + config.getString("messages.pre-game-countdown." + key)));
        }
        return messages;
    }

    public static HashMap<Integer, String> getGameCountdown() {
        HashMap<Integer, String> messages = new HashMap<>();
        for (String key : config.getConfigurationSection("messages.game-countdown").getKeys(false)) {
            messages.put(Integer.parseInt(key), ChatColor.translateAlternateColorCodes('&', config.getString("messages.plugin-prefix") + config.getString("messages.game-countdown." + key)));
        }
        return messages;
    }

    public static HashMap<Integer, String> getGameCountdownTitle() {
        HashMap<Integer, String> messages = new HashMap<>();
        for (String key : config.getConfigurationSection("messages.game-countdown-title").getKeys(false)) {
            messages.put(Integer.parseInt(key), ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-countdown-title." + key)));
        }
        return messages;
    }

    public static HashMap<Integer, String> getPostGameCountdown() {
        HashMap<Integer, String> messages = new HashMap<>();
        for (String key : config.getConfigurationSection("messages.post-game-countdown").getKeys(false)) {
            messages.put(Integer.parseInt(key), ChatColor.translateAlternateColorCodes('&', config.getString("messages.plugin-prefix") + config.getString("messages.post-game-countdown." + key)));
        }
        return messages;
    }

    public static List<String> getMultilineMessage(String key) {
        return config.getStringList("messages." + key).stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static String getScoreboardTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("scoreboards.title"));
    }

    public static List<String> getSpawnScoreboard() {
        return config.getStringList("scoreboards.spawn").stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static List<String> getPreGame1v1Scoreboard() {
        return config.getStringList("scoreboards.pre-game-1v1").stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static List<String> getPreGameTeamScoreboard() {
        return config.getStringList("scoreboards.pre-game-team").stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static List<String> getGame1v1Scoreboard() {
        return config.getStringList("scoreboards.game-1v1").stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static List<String> getGameTeamScoreboard() {
        return config.getStringList("scoreboards.game-team").stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }
}
