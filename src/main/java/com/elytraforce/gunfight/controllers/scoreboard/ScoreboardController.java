package com.elytraforce.gunfight.controllers.scoreboard;

import org.bukkit.Bukkit;


import org.bukkit.scoreboard.Scoreboard;

import com.elytraforce.gunfight.Main;
import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.QueueController;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScoreboardController {

    private static ScoreboardController instance;

    private List<String> spawnScoreboard;
    private List<String> preGame1v1Scoreboard;
    private List<String> preGameTeamScoreboard;
    private List<String> game1v1Scoreboard;
    private List<String> gameTeamScoreboard;

    private HashSet<Board> scoreboards;

    public ScoreboardController() {
        scoreboards = new HashSet<>();

        spawnScoreboard = PluginConfig.getSpawnScoreboard();
        preGame1v1Scoreboard = PluginConfig.getPreGame1v1Scoreboard();
        preGameTeamScoreboard = PluginConfig.getPreGameTeamScoreboard();
        game1v1Scoreboard = PluginConfig.getGame1v1Scoreboard();
        gameTeamScoreboard = PluginConfig.getGameTeamScoreboard();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.get(), () -> scoreboards.forEach(Board::update), 0L, 10L);
    }

    public void addPlayer(DuelsPlayer player) {
        for (Board board : scoreboards) {
            if (board.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        scoreboards.add(new Board(scoreboard, scoreboard.registerNewObjective("Display", "dummy"), player));
    }

    public void changePlayer(DuelsPlayer player) {
        removePlayer(player);
        addPlayer(player);
    }

    public void removePlayer(DuelsPlayer player) {
        for (Iterator<Board> iterator = scoreboards.iterator(); iterator.hasNext();) {
            Board board = iterator.next();
            if (board.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                board.unload();
                iterator.remove();
            }
        }
    }
 
    public List<String> getAppropriateScoreboard(DuelsPlayer player) {
        if (player.isInGame()) {
        	if (player.getSpectatingGame().getType().equals(GameType.Type.ONE_V_ONE) || player.getSpectatingGame().getType().equals(GameType.Type.ONE_V_ONE_BOMB)) {
                    return game1v1Scoreboard;
                } else {
                    return gameTeamScoreboard;
                }
        }
        if (player.isSpectating()) {
                if (player.getSpectatingGame().getType().equals(GameType.Type.ONE_V_ONE) || player.getSpectatingGame().getType().equals(GameType.Type.ONE_V_ONE_BOMB)) {
                    return game1v1Scoreboard;
                } else {
                    return gameTeamScoreboard;
                }

        } 
        return spawnScoreboard;
    }

    public String replaceVariables(DuelsPlayer player, String string) {
        GameType.Type queue = QueueController.get().getQueuePlayerIsIn(player);
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        if (player.isSpectating()) {
        	return string
                    .replace("%money%", format(Main.get().getEconomy().getBalance(player.asBukkitPlayer())))
                    .replace("%kills%", "" + player.getKills())
                    .replace("%deaths%", "" + player.getDeaths())
                    .replace("%wins%", "" + player.getWins())
                    .replace("%gamesplayed%", "" + player.getGamesPlayed())
                    .replace("%losses%", "" + player.getLosses())
                    .replace("%shotsfired%", "" + player.getShotsFired())
                    .replace("%shotshit%", "" + player.getShotsHit())
                    .replace("%queue%", queue != null ? queue.getGameType().getDisplayName() : "None")
                    .replace("%playersinqueue%", queue != null ? "" + QueueController.get().getNumberInQueue(queue) : "-")
                    .replace("%playersneeded%", queue != null ? "" + queue.getGameType().getMaxPlayers() : "-")
                    .replace("%timequeueing%", queue != null ? format.format(new Date(TimeUnit.SECONDS.toMillis(QueueController.get().getTimeSpentInQueue(player)))) : "-")
                    .replace("%map%", player.isSpectating() ? player.getSpectatingGame().getMapName() : "None")
                    .replace("%kit%", player.getSelectedKit() != null ? player.getSelectedKit().getName() : "None")
                    .replace("%team%", "")
                    .replace("%time%", player.isSpectating() ? format.format(new Date(TimeUnit.SECONDS.toMillis(player.getSpectatingGame().getTimeLeft()))) : "-");
        } else {
        	return string
                    .replace("%money%", format(Main.get().getEconomy().getBalance(player.asBukkitPlayer())))
                    .replace("%kills%", "" + player.getKills())
                    .replace("%deaths%", "" + player.getDeaths())
                    .replace("%wins%", "" + player.getWins())
                    .replace("%gamesplayed%", "" + player.getGamesPlayed())
                    .replace("%losses%", "" + player.getLosses())
                    .replace("%shotsfired%", "" + player.getShotsFired())
                    .replace("%shotshit%", "" + player.getShotsHit())
                    .replace("%queue%", queue != null ? queue.getGameType().getDisplayName() : "None")
                    .replace("%playersinqueue%", queue != null ? "" + QueueController.get().getNumberInQueue(queue) : "-")
                    .replace("%playersneeded%", queue != null ? "" + queue.getGameType().getMaxPlayers() : "-")
                    .replace("%timequeueing%", queue != null ? format.format(new Date(TimeUnit.SECONDS.toMillis(QueueController.get().getTimeSpentInQueue(player)))) : "-")
                    .replace("%map%", player.isInGame() ? player.getCurrentGame().getMapName() : "None")
                    .replace("%kit%", player.getSelectedKit() != null ? player.getSelectedKit().getName() : "None")
                    .replace("%team%", player.isInGame() ? player.getTeam().getDisplayColor() + player.getTeam().getDisplayName() : "None")
                    .replace("%time%", player.isInGame() ? format.format(new Date(TimeUnit.SECONDS.toMillis(player.getCurrentGame().getTimeLeft()))) : "-");
        }
        
    }

    private static String format(double value) {
        if (value != 0) {
            if (value > 100000) {
                int power;
                String suffix = " kmbt";
                String formattedNumber;

                NumberFormat formatter = new DecimalFormat("#,###.#");
                power = (int) StrictMath.log10(value);
                value = value / (Math.pow(10, (power / 3) * 3));
                formattedNumber = formatter.format(value);
                formattedNumber = formattedNumber + suffix.charAt(power / 3);
                return formattedNumber.length() > 4 ? formattedNumber.replaceAll("\\.[0-9]+", "") : formattedNumber;
            } else {
                return new DecimalFormat("###,###.##").format(value);
            }
        } else {
            return "0";
        }
    }

    public static ScoreboardController get() {
        if (instance == null) {
            instance = new ScoreboardController();
        }
        return instance;
    }
}
