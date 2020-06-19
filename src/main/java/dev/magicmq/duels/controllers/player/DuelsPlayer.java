package dev.magicmq.duels.controllers.player;

import dev.magicmq.duels.controllers.game.Duel;
import org.bukkit.entity.Player;

import java.util.List;

public class DuelsPlayer {

    private Player player;
    private Duel currentGame;
    private int kills;
    private int deaths;
    private int wins;
    private int gamesPlayed;
    private int losses;
    private int shotsFired;
    private List<String> unlockedKits;

    private Duel.Team team;
    private boolean dead;

    public DuelsPlayer(Player player, int kills, int deaths, int wins, int gamesPlayed, int losses, int shotsFired, List<String> unlockedKits) {
        this.player = player;
        this.kills = kills;
        this.deaths = deaths;
        this.wins = wins;
        this.gamesPlayed = gamesPlayed;
        this.losses = losses;
        this.shotsFired = shotsFired;
        this.unlockedKits = unlockedKits;
    }

    public Player asBukkitPlayer() {
        return player;
    }

    public boolean isInGame() {
        return currentGame != null;
    }

    public Duel getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Duel currentGame) {
        this.currentGame = currentGame;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getShotsFired() {
        return shotsFired;
    }

    public void setShotsFired(int shotsFired) {
        this.shotsFired = shotsFired;
    }

    public List<String> getUnlockedKits() {
        return unlockedKits;
    }

    public void addUnlockedKit(String name) {
        unlockedKits.add(name);
    }

    public Duel.Team getTeam() {
        return team;
    }

    public void setTeam(Duel.Team team) {
        this.team = team;
    }

    public boolean isDead() {
        return dead;
    }

    public void died() {
        this.dead = true;
    }

    public void endGame() {
        this.team = null;
        this.dead = false;
    }

    @Override
    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof DuelsPlayer))
            return false;
        return player.getUniqueId().equals(((DuelsPlayer) toCompare).asBukkitPlayer().getUniqueId());
    }
}
