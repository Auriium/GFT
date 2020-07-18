package dev.magicmq.duels.controllers.player;

import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.QueueController;
import dev.magicmq.duels.controllers.scoreboard.ScoreboardController;
import dev.magicmq.duels.storage.SQLStorage;
import dev.magicmq.duels.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class PlayerController {

    private static PlayerController instance;

    private HashMap<Integer, ItemStack> spawnHotbar;
    private HashMap<Integer, ItemStack> preGameHotbar;
    private HashMap<Integer, ItemStack> spectateHotbar;

    private HashSet<DuelsPlayer> players;

    private PlayerController() {
        ConfigurationSection spawnHotbarConfig = PluginConfig.getSpawnHotbar();
        spawnHotbar = new HashMap<>();
        for (String key : spawnHotbarConfig.getKeys(false)) {
            spawnHotbar.put(Integer.parseInt(key), ItemUtils.parseGUIItem(spawnHotbarConfig.getConfigurationSection(key)));
        }

        ConfigurationSection preGameHotbarConfig = PluginConfig.getPreGameHotbar();
        preGameHotbar = new HashMap<>();
        for (String key : preGameHotbarConfig.getKeys(false)) {
            preGameHotbar.put(Integer.parseInt(key), ItemUtils.parseGUIItem(preGameHotbarConfig.getConfigurationSection(key)));
        }

        ConfigurationSection spectateHotbarConfig = PluginConfig.getSpectateHotbar();
        spectateHotbar = new HashMap<>();
        for (String key : spectateHotbarConfig.getKeys(false)) {
            spectateHotbar.put(Integer.parseInt(key), ItemUtils.parseGUIItem(spectateHotbarConfig.getConfigurationSection(key)));
        }

        players = new HashSet<>();
    }

    public void playerJoined(Player player) {
        SQLStorage.get().loadPlayer(player);
        giveSpawnInv(player);
    }

    public void giveSpawnInv(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        for (Map.Entry<Integer, ItemStack> entry : spawnHotbar.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().clone());
        }
    }

    public void joinCallback(Player player, int kills, int deaths, int wins, int gamesPlayed, int losses, int shotsFired, int shotsHit, List<String> unlockedKits, boolean newPlayer) {
        if (player.isOnline()) {
            players.add(new DuelsPlayer(
                    player,
                    kills,
                    deaths,
                    wins,
                    gamesPlayed,
                    losses,
                    shotsFired,
                    shotsHit,
                    unlockedKits,
                    newPlayer
            ));
            ScoreboardController.get().addPlayer(getDuelsPlayer(player));
        }
    }

    public void playerQuit(DuelsPlayer player) {
        if (QueueController.get().getQueuePlayerIsIn(player) != null)
            QueueController.get().removePlayerFromQueue(player);
        if (player.isInGame()) {
            player.getCurrentGame().playerQuit(player);
        }

        ScoreboardController.get().removePlayer(player);

        if (player.isInDatabase()) {
            SQLStorage.get().updatePlayer(player, true);
        } else {
            SQLStorage.get().insertPlayer(player, true);
        }

        players.remove(player);
    }

    public HashSet<DuelsPlayer> getPlayers() {
        return players;
    }

    public DuelsPlayer getDuelsPlayer(Player player) {
        return getDuelsPlayer(player.getUniqueId());
    }

    public DuelsPlayer getDuelsPlayer(UUID uuid) {
        for (DuelsPlayer duelsPlayer : players) {
            if (duelsPlayer.getUniqueId().equals(uuid))
                return duelsPlayer;
        }
        return null;
    }

    public HashMap<Integer, ItemStack> getPreGameHotbar() {
        return preGameHotbar;
    }

    public HashMap<Integer, ItemStack> getSpectateHotbar() {
        return spectateHotbar;
    }

    public static PlayerController get() {
        if (instance == null) {
            instance = new PlayerController();
        }
        return instance;
    }

}
