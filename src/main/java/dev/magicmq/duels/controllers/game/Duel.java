package dev.magicmq.duels.controllers.game;

import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.kits.Kit;
import dev.magicmq.duels.controllers.kits.KitsController;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import dev.magicmq.duels.controllers.scoreboard.ScoreboardController;
import dev.magicmq.duels.storage.SQLStorage;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Duel {

    private UUID uniqueId;
    private DuelType type;
    private World world;
    private String mapName;
    private HashSet<DuelsPlayer> players;
    private HashSet<DuelsPlayer> playersCopy;
    private HashMap<DuelsPlayer, String> playersDisplay;
    private boolean started;
    private boolean ended;

    private int preGameTime;
    private int gameTime;
    private int postGameTime;

    public Duel(UUID uniqueId, DuelType type, World world, String mapName, HashSet<DuelsPlayer> players, List<Location> teamOneSpawns, List<Location> teamTwoSpawns) {
        this.uniqueId = uniqueId;
        this.type = type;
        this.world = world;
        this.mapName = mapName;

        this.players = players;
        this.playersCopy = (HashSet<DuelsPlayer>) players.clone();
        this.playersDisplay = new HashMap<>();

        preGameTime = PluginConfig.getPreGameTime() + 1;
        gameTime = PluginConfig.getGameTime() + 1;
        postGameTime = PluginConfig.getPostGameTime() + 1;

        int i = 0;
        int j = 0;
        for (DuelsPlayer player : players) {
            PlayerInventory inv = player.asBukkitPlayer().getInventory();
            inv.clear();
            for (Map.Entry<Integer, ItemStack> entry : PlayerController.get().getPreGameHotbar().entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue().clone());
            }

            player.setCurrentGame(this);
            if (i < type.getMaxPlayers() / 2) {
                player.setTeam(Team.ONE);
                player.asBukkitPlayer().teleport(teamOneSpawns.get(i));
                i++;
            } else {
                player.setTeam(Team.TWO);
                player.asBukkitPlayer().teleport(teamTwoSpawns.get(j));
                j++;
            }

            ScoreboardController.get().changePlayer(player);

            player.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 5, false));
            player.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5, false));
            KitsController.get().openKitsInventory(player.asBukkitPlayer());

            playersDisplay.put(player, player.getTeam().getDisplayColor() + "⬛ " + player.asBukkitPlayer().getName());

            if (type == DuelType.ONE_V_ONE)
                player.sendMessage(PluginConfig.getMessage("entered-game-1v1")
                        .replace("%team%", player.getTeam().getDisplayColor() + player.getTeam().getDisplayName()));
            else
                player.sendMessage(PluginConfig.getMessage("entered-game")
                        .replace("%team%", player.getTeam().getDisplayColor() + player.getTeam().getDisplayName()));
        }
    }

    public void tick() {
        if (preGameTime > 0) {
            preGameTime--;
            HashMap<Integer, String> messages = PluginConfig.getPreGameCountdown();
            if (messages.containsKey(preGameTime)) {
                players.forEach(player -> {
                    player.sendMessage(messages.get(preGameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }

            HashMap<Integer, String> titleMessages = PluginConfig.getGameCountdownTitle();
            if (titleMessages.containsKey(preGameTime)) {
                String[] split = titleMessages.get(preGameTime).split("\\|");
                players.forEach(player -> player.asBukkitPlayer().sendTitle(split[0], split[1], 5, 20, 10));
            }
            if (preGameTime == 0) {
                startGame();
            }
        } else if (gameTime > 0) {
            gameTime--;
            HashMap<Integer, String> messages = PluginConfig.getGameCountdown();
            if (messages.containsKey(gameTime)) {
                players.forEach(player -> {
                    player.sendMessage(messages.get(gameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }
            if (gameTime == 0) {
                endGame(null);
            }
        } else if (postGameTime > 0) {
            postGameTime--;
            HashMap<Integer, String> messages = PluginConfig.getPostGameCountdown();
            if (messages.containsKey(postGameTime)) {
                players.forEach(player -> {
                    player.sendMessage(messages.get(postGameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }
            if (postGameTime == 0) {
                closeGame();
            }
        }
    }

    public void playerQuit(DuelsPlayer player) {
        players.remove(player);

        if (!ended) {
            if (getAlivePlayers(Team.ONE) == 0) {
                if (started)
                    endGame(Team.TWO);
                else
                    endGame(null);
            } else if (getAlivePlayers(Team.TWO) == 0) {
                if (started)
                    endGame(Team.ONE);
                else
                    endGame(null);
            }
        }

        playersDisplay.put(player, player.getTeam().getDisplayColor() + "" + ChatColor.STRIKETHROUGH + "⬛ " + player.asBukkitPlayer().getName());

        cleanupPlayer(player, GameMode.SURVIVAL);
        player.endGame();
        player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
        PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
        ScoreboardController.get().changePlayer(player);
    }

    public void playerDied(DuelsPlayer player) {
        player.setDeaths(player.getDeaths() + 1);
        player.died();
        cleanupPlayer(player, GameMode.SPECTATOR);

        if (player.asBukkitPlayer().getKiller() != null) {
            DuelsPlayer killer = PlayerController.get().getDuelsPlayer(player.asBukkitPlayer().getKiller());
            killer.setKills(killer.getKills() + 1);
            if (type != DuelType.ONE_V_ONE) {
                killer.asBukkitPlayer().sendMessage(PluginConfig.getMessage("killed-player")
                        .replace("%player%", player.asBukkitPlayer().getName())
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam())));
            }
        }

        playersDisplay.put(player, player.getTeam().getDisplayColor() + "" + ChatColor.STRIKETHROUGH + "⬛ " + player.asBukkitPlayer().getName());

        if (type != DuelType.ONE_V_ONE)
            player.sendMessage(PluginConfig.getMessage("died")
                    .replace("%amount%", "" + getAlivePlayers(player.getTeam())));

        if (getAlivePlayers(Team.ONE) == 0) {
            endGame(Team.TWO);
            return;
        } else if (getAlivePlayers(Team.TWO) == 0) {
            endGame(Team.ONE);
            return;
        }

        if (type != DuelType.ONE_V_ONE)
            getPlayers(player.getTeam()).stream().filter(toMessage -> !toMessage.equals(player)).forEach(toMessage -> toMessage.asBukkitPlayer().sendMessage(PluginConfig.getMessage("died-other")
                    .replace("%player%", player.asBukkitPlayer().getName())
                    .replace("%amount%", "" + getAlivePlayers(player.getTeam()))));
    }

    public void startGame() {
        started = true;

        for (DuelsPlayer player : players) {
            player.asBukkitPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
            player.asBukkitPlayer().removePotionEffect(PotionEffectType.SLOW);
            player.asBukkitPlayer().closeInventory();
            Kit kit = player.getSelectedKit();
            if (kit != null) {
                player.asBukkitPlayer().getInventory().setArmorContents(kit.getArmor());
                player.asBukkitPlayer().getInventory().setContents(kit.getInventory());
            } else {
                player.asBukkitPlayer().getInventory().setArmorContents(KitsController.get().getDefaultKit().getArmor());
                player.asBukkitPlayer().getInventory().setContents(KitsController.get().getDefaultKit().getInventory());
                player.sendMessage(PluginConfig.getMessage("default-kit")
                        .replace("%kit%", KitsController.get().getDefaultKit().getName()));
            }

            ItemStack[] armor = player.asBukkitPlayer().getInventory().getArmorContents();
            for (ItemStack item : armor) {
                if (item != null && (item.getType() == Material.LEATHER_HELMET
                    || item.getType() == Material.LEATHER_CHESTPLATE
                    || item.getType() == Material.LEATHER_LEGGINGS
                    || item.getType() == Material.LEATHER_BOOTS)) {
                    LeatherArmorMeta meta = (LeatherArmorMeta) armor[0].getItemMeta();
                    meta.setColor(player.getTeam().getArmorColor());
                    item.setItemMeta(meta);
                }
            }
            player.asBukkitPlayer().getInventory().setArmorContents(armor);

            ScoreboardController.get().changePlayer(player);

            Scoreboard scoreboard = player.asBukkitPlayer().getScoreboard();
            org.bukkit.scoreboard.Team teamOne = scoreboard.registerNewTeam("ONE");
            teamOne.setAllowFriendlyFire(false);
            teamOne.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            org.bukkit.scoreboard.Team teamTwo = scoreboard.registerNewTeam("TWO");
            teamTwo.setAllowFriendlyFire(false);
            teamTwo.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            getPlayers(Team.ONE).forEach(toAdd -> teamOne.addPlayer(toAdd.asBukkitPlayer()));
            getPlayers(Team.TWO).forEach(toAdd -> teamTwo.addPlayer(toAdd.asBukkitPlayer()));

            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, PluginConfig.getSoundVolume(), 2f);
        }
    }

    public boolean hasStarted() {
        return started;
    }

    public void endGame(Team winner) {
        preGameTime = 0;
        gameTime = 0;
        ended = true;

        if (!started)
            started = true;

        for (DuelsPlayer player : players) {
            player.asBukkitPlayer().closeInventory();
            cleanupPlayer(player, GameMode.SPECTATOR);
            ((CraftPlayer) player.asBukkitPlayer()).getHandle().getDataWatcher().set(new DataWatcherObject<>(10, DataWatcherRegistry.b), 0);
        }

        for (DuelsPlayer player : playersCopy) {
            if (player.asBukkitPlayer().isOnline()) {
                DuelsPlayer toIncrement = PlayerController.get().getDuelsPlayer(player.getUniqueId());
                toIncrement.setGamesPlayed(toIncrement.getGamesPlayed() + 1);
                if (winner != null) {
                    if (toIncrement.getTeam() == winner) {
                        toIncrement.setWins(toIncrement.getWins() + 1);
                    } else {
                        toIncrement.setLosses(toIncrement.getLosses() + 1);
                    }

                    PluginConfig.getMultilineMessage(player.getTeam() == winner ? "win-match" : "lose-match").forEach(string -> {
                        string = string.replace("%kills%", "" + player.getKills())
                                .replace("%deaths%", "" + player.getDeaths())
                                .replace("%wins%", "" + player.getWins())
                                .replace("%gamesplayed%", "" + player.getGamesPlayed())
                                .replace("%losses%", "" + player.getLosses())
                                .replace("%shotsfired%", "" + player.getShotsFired())
                                .replace("%shotshit%", "" + player.getShotsHit())
                                .replace("%winningteam%", winner.getDisplayName())
                                .replace("%teamcolor%", "" + winner.getDisplayColor())
                                .replace("%losingteam%", winner == Team.ONE ? Team.TWO.displayName : Team.ONE.displayName);
                        player.sendMessage(string);
                    });

                    if (player.getTeam() == winner) {
                        player.asBukkitPlayer().sendTitle(PluginConfig.getBareMessage("win-game-title").split("\\|")[0], PluginConfig.getBareMessage("win-game-title").split("\\|")[1], 5, 20, 10);
                    } else {
                        player.asBukkitPlayer().sendTitle(PluginConfig.getBareMessage("lose-game-title").split("\\|")[0], PluginConfig.getBareMessage("lose-game-title").split("\\|")[1], 5, 20, 10);
                    }
                } else {
                    PluginConfig.getMultilineMessage("no-winner").forEach(string -> {
                        string = string.replace("%kills%", "" + player.getKills())
                                .replace("%deaths%", "" + player.getDeaths())
                                .replace("%wins%", "" + player.getWins())
                                .replace("%gamesplayed%", "" + player.getGamesPlayed())
                                .replace("%losses%", "" + player.getLosses())
                                .replace("%shotsfired%", "" + player.getShotsFired())
                                .replace("%shotshit%", "" + player.getShotsHit());
                        player.sendMessage(string);
                    });
                }
            } else {
                if (player.getTeam() == winner) {
                    player.setWins(player.getWins() + 1);
                } else {
                    player.setLosses(player.getLosses() + 1);
                }
                player.setGamesPlayed(player.getGamesPlayed() + 1);

                if (player.isInDatabase())
                    SQLStorage.get().updatePlayer(player, true);
                else
                    SQLStorage.get().insertPlayer(player, true);
            }

            if (winner != null) {
                if (player.getTeam() == winner)
                    PluginConfig.getWinCommands().forEach(string -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), string.replace("%player%", player.getName())));
            }
        }
    }

    public void closeGame() {
        for (DuelsPlayer player : players) {
            cleanupPlayer(player, GameMode.SURVIVAL);
            player.endGame();
            player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
            PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
            ScoreboardController.get().changePlayer(player);
        }

        Bukkit.unloadWorld(world, false);

        DuelController.get().endGame(this);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public DuelType getType() {
        return type;
    }

    public String getMapName() {
        return mapName;
    }

    public int getTimeLeft() {
        if (started) {
            return gameTime;
        } else {
            return preGameTime;
        }
    }

    public HashSet<DuelsPlayer> getPlayers(Team team) {
        HashSet<DuelsPlayer> players = new HashSet<>();
        for (DuelsPlayer player : this.players) {
            if (player.getTeam() == team)
                players.add(player);
        }
        return players;
    }

    public Collection<String> getPlayersDisplay() {
        return playersDisplay.values();
    }

    public int getAlivePlayers(Team team) {
        int i = 0;
        for (DuelsPlayer player : getPlayers(team)) {
            if (!player.isDead())
                i++;
        }
        return i;
    }

    private void cleanupPlayer(DuelsPlayer player, GameMode gameMode) {
        player.asBukkitPlayer().setGameMode(gameMode);
        if (gameMode == GameMode.SURVIVAL) {
            player.asBukkitPlayer().setFlying(false);
            player.asBukkitPlayer().setAllowFlight(false);
        }
        player.asBukkitPlayer().getActivePotionEffects().forEach(effect -> player.asBukkitPlayer().removePotionEffect(effect.getType()));
        player.asBukkitPlayer().setHealth(player.asBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.asBukkitPlayer().setFoodLevel(18);
        player.asBukkitPlayer().setFireTicks(0);
        PlayerInventory inv = player.asBukkitPlayer().getInventory();
        inv.clear();
        for (Map.Entry<Integer, ItemStack> entry : PlayerController.get().getSpectateHotbar().entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().clone());
        }
    }

    @Override
    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof Duel))
            return false;
        return uniqueId.equals(((Duel) toCompare).getUniqueId());
    }

    public enum Team {

        ONE("Red", ChatColor.RED, Color.fromRGB(255, 0, 0)),
        TWO("Blue", ChatColor.BLUE, Color.fromRGB(0, 0, 255));

        private final String displayName;
        private final ChatColor displayColor;
        private final Color armorColor;

        Team(String displayName, ChatColor displayColor, Color armorColor) {
            this.displayName = displayName;
            this.displayColor = displayColor;
            this.armorColor = armorColor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ChatColor getDisplayColor() {
            return displayColor;
        }

        public Color getArmorColor() {
            return armorColor;
        }
    }
}
