package dev.magicmq.duels.controllers.player;

import dev.magicmq.duels.storage.SQLStorage;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

public class PlayerController {

    private static PlayerController instance;

    private HashSet<DuelsPlayer> players;

    private PlayerController() {
        players = new HashSet<>();
    }

    public void playerJoined(Player player) {
        SQLStorage.get().loadPlayer(player);
    }

    public void joinCallback(Player player, int kills, int deaths, int wins, int gamesPlayed, int losses, int shotsFired, List<String> unlockedKits) {
        if (player.isOnline())
            players.add(new DuelsPlayer(
                    player,
                    kills,
                    deaths,
                    wins,
                    gamesPlayed,
                    losses,
                    shotsFired,
                    unlockedKits
            ));
    }

    public void playerQuit(Player player) {
        DuelsPlayer duelsPlayer = getDuelsPlayer(player);
        if (duelsPlayer.isInGame()) {
            duelsPlayer.getCurrentGame().playerQuit(duelsPlayer);
        }
        SQLStorage.get().savePlayer(duelsPlayer);
        players.remove(duelsPlayer);
    }

    public HashSet<DuelsPlayer> getPlayers() {
        return players;
    }

    public DuelsPlayer getDuelsPlayer(Player player) {
        for (DuelsPlayer duelsPlayer : players) {
            if (duelsPlayer.asBukkitPlayer().getUniqueId().equals(player.getUniqueId()))
                return duelsPlayer;
        }
        return null;
    }

    public static PlayerController get() {
        if (instance == null) {
            instance = new PlayerController();
        }
        return instance;
    }

}
