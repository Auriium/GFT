package dev.magicmq.duels.controllers.player;

import dev.magicmq.duels.controllers.game.Duel;
import dev.magicmq.duels.controllers.kits.Kit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class DuelsPlayer {

    private final Player player;
    private final UUID uniqueId;
    private final String name;
    private int kills;
    private int deaths;
    private int wins;
    private int gamesPlayed;
    private int losses;
    private int shotsFired;
    private int shotsHit;
    private List<String> unlockedKits;
    private boolean inDatabase;

    private Duel currentGame;
    private Kit selectedKit;
    private Duel.Team team;
    private boolean dead;

    public DuelsPlayer(Player player, int kills, int deaths, int wins, int gamesPlayed, int losses, int shotsFired, int shotsHit, List<String> unlockedKits, boolean inDatabase) {
        this.player = player;
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();
        this.kills = kills;
        this.deaths = deaths;
        this.wins = wins;
        this.gamesPlayed = gamesPlayed;
        this.losses = losses;
        this.shotsFired = shotsFired;
        this.shotsHit = shotsHit;
        this.unlockedKits = unlockedKits;
        this.inDatabase = inDatabase;
    }

    public Player asBukkitPlayer() {
        return player;
    }

    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
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

    public int getShotsHit() {
        return shotsHit;
    }

    public void setShotsHit(int shotsHit) {
        this.shotsHit = shotsHit;
    }

    public List<String> getUnlockedKits() {
        return unlockedKits;
    }

    public void addUnlockedKit(String name) {
        unlockedKits.add(name);
    }

    public Kit getSelectedKit() {
        return selectedKit;
    }

    public void setSelectedKit(Kit selectedKit) {
        this.selectedKit = selectedKit;
    }

    public boolean isInDatabase() {
        return inDatabase;
    }

    public void setInDatabase(boolean inDatabase) {
        this.inDatabase = inDatabase;
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
        this.currentGame = null;
        this.selectedKit = null;
        this.team = null;
        this.dead = false;
    }

    @Override
    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof DuelsPlayer))
            return false;
        return uniqueId.equals(((DuelsPlayer) toCompare).uniqueId);
    }
}
