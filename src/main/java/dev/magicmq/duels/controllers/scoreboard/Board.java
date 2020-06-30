package dev.magicmq.duels.controllers.scoreboard;

import com.google.common.collect.Lists;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Board {

    private DuelsPlayer associated;
    private Scoreboard scoreboard;
    private Objective objective;

    private String title;
    private List<String> format;
    private HashMap<Integer, String> oldLines;

    public Board(Scoreboard scoreboard, Objective objective, DuelsPlayer associated) {
        this.associated = associated;
        this.scoreboard = scoreboard;
        this.objective = objective;

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        title = ChatColor.translateAlternateColorCodes('&', PluginConfig.getScoreboardTitle());
        format = ScoreboardController.get().getAppropriateScoreboard(associated);
        oldLines = new HashMap<>();

        setup();
    }

    private void setup() {
        objective.setDisplayName(ScoreboardController.get().replaceVariables(associated, title));

        List<String> formatCopy = new ArrayList<>();
        Lists.newArrayList(format).forEach(string -> {
            if (string.equals("%players%")) {
                if (associated.isInGame()) {
                    formatCopy.addAll(associated.getCurrentGame().getPlayersDisplay());
                }
            } else {
                formatCopy.add(ScoreboardController.get().replaceVariables(associated, string));
            }
        });

        int i = formatCopy.size();
        for (String string : formatCopy) {
            string = ScoreboardController.get().replaceVariables(associated, string);
            objective.getScore(string).setScore(i);
            oldLines.put(i, string);
            i--;
        }
        associated.asBukkitPlayer().setScoreboard(scoreboard);
    }

    public void update() {
        List<String> formatCopy = new ArrayList<>();
        Lists.newArrayList(format).forEach(string -> {
            if (string.equals("%players%")) {
                if (associated.isInGame()) {
                    formatCopy.addAll(associated.getCurrentGame().getPlayersDisplay());
                }
            } else {
                formatCopy.add(ScoreboardController.get().replaceVariables(associated, string));
            }
        });
        Collections.reverse(formatCopy);

        int i = formatCopy.size();
        for (String string : formatCopy) {
            String oldLine = oldLines.get(i);
            string = ScoreboardController.get().replaceVariables(associated, formatCopy.get(i - 1));
            if (!oldLine.equals(string)) {
                Score score = objective.getScore(string);
                scoreboard.resetScores(oldLine);
                score.setScore(i);
                oldLines.put(i, string);
            }
            i--;
        }
    }

    public void unload() {
        associated.asBukkitPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public DuelsPlayer getPlayer() {
        return associated;
    }
}
