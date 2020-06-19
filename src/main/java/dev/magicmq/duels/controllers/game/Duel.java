package dev.magicmq.duels.controllers.game;

import com.grinderwolf.swm.api.world.SlimeWorld;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.kits.KitsController;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Duel {

    private UUID uniqueId;
    private DuelType type;
    private World world;
    private SlimeWorld slimeWorld;
    private HashSet<DuelsPlayer> players;
    private boolean started;

    private int preGameTime;
    private int gameTime;
    private int postGameTime;

    public Duel(UUID uniqueId, DuelType type, World world, SlimeWorld slimeWorld, HashSet<DuelsPlayer> players) {
        this.uniqueId = uniqueId;
        this.type = type;
        this.world = world;
        this.slimeWorld = slimeWorld;

        this.players = players;

        preGameTime = PluginConfig.getPreGameTime() + 1;
        gameTime = PluginConfig.getGameTime() + 1;
        postGameTime = PluginConfig.getPostGameTime() + 1;

        int i = 0;
        for (DuelsPlayer player : getPlayers()) {
            player.setCurrentGame(this);
            if (i < type.getMaxPlayers() / 2) {
                player.setTeam(Team.ONE);
            } else {
                player.setTeam(Team.TWO);
            }
            player.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 5, false));
            player.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5, false));
            KitsController.get().openKitInventory(player.asBukkitPlayer());
        }
    }

    public void tick() {
        if (preGameTime > 0) {
            preGameTime--;
            getPlayers().forEach(player -> player.asBukkitPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PluginConfig.getMessage("action-bar.pre-game")
                    .replace("%time%", formatTime(preGameTime)))));
            if (preGameTime == 0) {
                startGame();
            } else {

            }
        } else if (gameTime > 0) {
            gameTime--;
            getPlayers().forEach(player -> player.asBukkitPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PluginConfig.getMessage("action-bar.game")
                    .replace("%time%", formatTime(gameTime)))));
            if (gameTime == 0) {
                endGame(null);
            } else {

            }
        } else if (postGameTime > 0) {
            postGameTime--;
            getPlayers().forEach(player -> player.asBukkitPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PluginConfig.getMessage("action-bar.post-game")
                    .replace("%time%", formatTime(postGameTime)))));
            if (postGameTime == 0) {
                DuelController.get().endGame(this);
            } else {

            }
        }
    }

    public void playerQuit(DuelsPlayer player) {
        if (preGameTime > 0 || gameTime > 0) {
            players.remove(player);
            if (player.isDead()) {
                player.asBukkitPlayer().setGameMode(GameMode.SURVIVAL);
            }

        }
    }

    public void playerDied(DuelsPlayer player) {
        if (player.asBukkitPlayer().getKiller() != null) {
            DuelsPlayer killer = PlayerController.get().getDuelsPlayer(player.asBukkitPlayer().getKiller());
            killer.setKills(killer.getKills() + 1);
        }
        player.setDeaths(player.getDeaths() + 1);
        player.died();
        player.asBukkitPlayer().setGameMode(GameMode.SPECTATOR);
        if (getAlivePlayers(Team.ONE) == 0) {
            endGame(Team.TWO);
        } else if (getAlivePlayers(Team.TWO) == 0) {
            endGame(Team.ONE);
        }
    }

    public void startGame() {
        started = true;
        for (DuelsPlayer player : getPlayers()) {
            player.asBukkitPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
            player.asBukkitPlayer().removePotionEffect(PotionEffectType.SLOW);
        }
    }

    public boolean hasStarted() {
        return started;
    }

    public void endGame(Team winner) {
        for (DuelsPlayer player : players) {
            player.asBukkitPlayer().setGameMode(GameMode.SPECTATOR);
            player.setGamesPlayed(player.getGamesPlayed() + 1);
        }
        if (winner != null) {
            getPlayers(winner).forEach(player -> player.setWins(player.getWins() + 1));
            getPlayers(winner == Team.ONE ? Team.TWO : Team.ONE).forEach(player -> player.setLosses(player.getLosses() + 1));
        }
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public DuelType getType() {
        return type;
    }

    public World getWorld() {
        return world;
    }

    public SlimeWorld getSlimeWorld() {
        return slimeWorld;
    }

    public HashSet<DuelsPlayer> getPlayers() {
        return players;
    }

    public HashSet<DuelsPlayer> getPlayers(Team team) {
        HashSet<DuelsPlayer> players = new HashSet<>();
        for (DuelsPlayer player : this.players) {
            if (player.getTeam() == team)
                players.add(player);
        }
        return players;
    }

    public int getAlivePlayers(Team team) {
        int i = 0;
        for (DuelsPlayer player : getPlayers(team)) {
            if (!player.isDead())
                i++;
        }
        return i;
    }

    private String formatTime(int time) {
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        long millis = TimeUnit.SECONDS.toMillis(time);
        return format.format(new Date(millis));
    }

    @Override
    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof Duel))
            return false;
        return uniqueId.equals(((Duel) toCompare).getUniqueId());
    }

    public enum Team {

        ONE, TWO

    }
}
