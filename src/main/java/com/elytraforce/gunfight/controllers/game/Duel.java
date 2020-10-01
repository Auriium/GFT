package com.elytraforce.gunfight.controllers.game;

import org.bukkit.*;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;

import com.elytraforce.gunfight.Main;
import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.kits.Kit;
import com.elytraforce.gunfight.controllers.kits.KitsController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.controllers.scoreboard.ScoreboardController;
import com.elytraforce.gunfight.storage.SQLStorage;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Duel {

    private UUID uniqueId;
    private DuelType type;
    private World world;
    private String mapName;
    private HashSet<DuelsPlayer> players;
    private HashSet<DuelsPlayer> playersCopy;
    private HashMap<DuelsPlayer, String> playersDisplay;
    private HashSet<DuelsPlayer> spectators;
    private boolean started;
    private boolean ended;
    private Location teleportLocation;

    private int preGameTime;
    private int gameTime;
    private int postGameTime;
    
    public void addSpectator(DuelsPlayer player) {
    	PlayerInventory inv = player.asBukkitPlayer().getInventory();
        inv.clear();
        for (Map.Entry<Integer, ItemStack> entry : PlayerController.get().getSpectateHotbar().entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().clone());
        }

        ScoreboardController.get().changePlayer(player);
        cleanupPlayer(player, GameMode.SPECTATOR, true);
        
        player.setSpectatingGame(this);
        player.setSpectating(true);
        this.spectators.add(player);
        
        player.asBukkitPlayer().teleport(this.teleportLocation);
    }
    
    public void removeSpectator(DuelsPlayer player) {
    	cleanupPlayer(player, GameMode.SURVIVAL, false);
        player.endGame();
        player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
        PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
        ScoreboardController.get().changePlayer(player);
        
        player.setSpectatingGame(null);
        player.setSpectating(false);
        this.spectators.remove(player);
        
        player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
        PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
        ScoreboardController.get().changePlayer(player);
    }

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
        
        this.teleportLocation = teamOneSpawns.get(0);
        
        this.spectators = new HashSet<DuelsPlayer>();

        //int i = 0;
        //int j = 0;
        
        if (this.type == DuelType.ONE_V_ONE || this.type == DuelType.TWO_V_TWO || 
        		this.type == DuelType.THREE_V_THREE || this.type == DuelType.TWO_V_TWO_BOMB || this.type == DuelType.THREE_V_THREE_BOMB) {
        	
        	
        }
        
        ArrayList<DuelsPlayer> red = new ArrayList<>();
        ArrayList<DuelsPlayer> blue = new ArrayList<>();
        
        for (DuelsPlayer player : players) {
        	PlayerInventory inv = player.asBukkitPlayer().getInventory();
            inv.clear();
            for (Map.Entry<Integer, ItemStack> entry : PlayerController.get().getPreGameHotbar().entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue().clone());
            }

            //team setting over here
            player.setCurrentGame(this);
            
        	int randomNum = ThreadLocalRandom.current().nextInt(0, 1 + 1);
            
            if (randomNum == 0) {
            	red.add(player);
            } else {
            	blue.add(player);
            }
        }
        
        //should add the players to virtual storage teams. lets say there are 3 red and 1 blue
        while (red.size() > type.getMaxPlayers() / 2) {
        	blue.add(red.get(0));
        	red.remove(red.get(0));
        }
        
        while (blue.size() > type.getMaxPlayers() / 2) {
        	red.add(blue.get(0));
        	blue.remove(blue.get(0));
        }
        
        for (DuelsPlayer redPlayer : red) {
        	redPlayer.setTeam(Team.ONE);
        	redPlayer.asBukkitPlayer().teleport(teamOneSpawns.get(red.indexOf(redPlayer)));
        }
        
        for (DuelsPlayer bluePlayer : blue) {
        	bluePlayer.setTeam(Team.ONE);
        	bluePlayer.asBukkitPlayer().teleport(teamTwoSpawns.get(blue.indexOf(bluePlayer)));
        }
        
        
        for (DuelsPlayer player : players) {
            
            //if (i < type.getMaxPlayers() / 2) {
            //    player.setTeam(Team.ONE);
            //    player.asBukkitPlayer().teleport(teamOneSpawns.get(i));
            //    i++;
            //} else {
            //    player.setTeam(Team.TWO);
            //    player.asBukkitPlayer().teleport(teamTwoSpawns.get(j));
            //    j++;
            //}

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
    
    public void sendActionBar(Player player, String actionbar) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',actionbar)));
	}

    public void tick() {
    	for (DuelsPlayer p : this.spectators) {
    		//send actionbar p.asBukkitPlayer();
    		this.sendActionBar(p.asBukkitPlayer(), "&aType /quit to return to GFT lobby!");
    	}
        if (preGameTime > 0) {
            preGameTime--;
            HashMap<Integer, String> messages = PluginConfig.getPreGameCountdown();
            if (messages.containsKey(preGameTime)) {
                players.forEach(player -> {
                    player.sendMessage(messages.get(preGameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f);
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
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f);
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
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }
            if (postGameTime == 0) {
                closeGame();
            }
        }
    }
    
    //Titles Shit
    public void gameStartTitle() {
    	for (DuelsPlayer player : players) {
    		player.asBukkitPlayer().sendTitle(colorString("&4&lGame Start!"), colorString("&7GFT - &e&l" + type.getDisplayName()), 10, 10, 10);
    	}
    	
    	for (DuelsPlayer player : spectators) {
    		player.asBukkitPlayer().sendTitle(colorString("&4&lGame Start!"), colorString("&7GFT - &e&l" + type.getDisplayName()), 10, 10, 10);
    	}
    	
    	new BukkitRunnable() {
            public void run() {
            	
            	for (DuelsPlayer player : players) {
            		player.asBukkitPlayer().sendTitle(colorString("&4&lYou are team"), colorString(player.getTeam().displayColor + player.getTeam().getDisplayName()), 10, 10, 10);
            	}
            	
            	for (DuelsPlayer player : spectators) {
            		player.asBukkitPlayer().sendTitle(colorString("&e&lSPECTATING"), "");
            	}
            	
            }
        }.runTaskLater(Main.get(), (long)20L);
    }
    
    public void gameEndTitle(DuelsPlayer player, Team winner, boolean didWin) {
    	
    	String winningTeam;
    	ChatColor winningColor;
    	
    	if (!(winner == null)) {
    		winningTeam = winner.getDisplayName();
    		winningColor = winner.getDisplayColor();
    	} else {
    		winningTeam = "None";
    		winningColor = ChatColor.YELLOW;
    	}
    	
    	player.asBukkitPlayer().sendTitle(colorString("&4&lGame Over!"), colorString(winningColor + "" + ChatColor.BOLD + winningTeam + " &e&lWon!"), 10, 10, 10);
    	
    	new BukkitRunnable() {
            public void run() {
            	
            		player.asBukkitPlayer().sendTitle(colorString("&c&lSending you to lobby!"), "", 10, 10, 10);
            	
            }
        }.runTaskLater(Main.get(), (long)20L);
    }
    
    public String colorString(String string) {
    	return ChatColor.translateAlternateColorCodes('&', string);
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

        cleanupPlayer(player, GameMode.SURVIVAL, false);
        player.endGame();
        player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
        PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
        ScoreboardController.get().changePlayer(player);
    }

    public void playerDied(DuelsPlayer player) {
        player.setDeaths(player.getDeaths() + 1);
        player.died();

        if (player.asBukkitPlayer().getKiller() != null) {
            DuelsPlayer killer = PlayerController.get().getDuelsPlayer(player.asBukkitPlayer().getKiller());
            killer.setKills(killer.getKills() + 1);
            /*if (type != DuelType.ONE_V_ONE) {
                killer.asBukkitPlayer().sendMessage(PluginConfig.getMessage("killed-player")
                        .replace("%player%", player.asBukkitPlayer().getName())
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam())));
            }*/
        }

        playersDisplay.put(player, player.getTeam().getDisplayColor() + "" + ChatColor.STRIKETHROUGH + "⬛ " + player.asBukkitPlayer().getName());

        /*if (type != DuelType.ONE_V_ONE) {
            if (player.asBukkitPlayer().getKiller() != null) {
                player.sendMessage(PluginConfig.getMessage("killed")
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam()))
                        .replace("%killer%", player.asBukkitPlayer().getKiller().getName()));
            } else {
                player.sendMessage(PluginConfig.getMessage("died")
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam())));
            }
        }*/

        if (getAlivePlayers(Team.ONE) == 0) {
            endGame(Team.TWO);
            return;
        } else if (getAlivePlayers(Team.TWO) == 0) {
            endGame(Team.ONE);
            return;
        }

        if (type != DuelType.ONE_V_ONE) {
            if (player.asBukkitPlayer().getKiller() != null) {
                players.forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("kill-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%killer%", player.asBukkitPlayer().getKiller().getName())
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam()))));
                spectators.forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("kill-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%killer%", player.asBukkitPlayer().getKiller().getName())
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam()))));
            } else {
                players.forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("die-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam()))));
                spectators.forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("die-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%amount%", "" + getAlivePlayers(player.getTeam()))));
            }
        }
    }

    public void playerRespawned(DuelsPlayer player) {
        cleanupPlayer(player, GameMode.SPECTATOR, true);
        
    }

    @SuppressWarnings("deprecation")
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
            teamOne.setColor(Team.ONE.getDisplayColor());
            teamOne.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            teamOne.setCanSeeFriendlyInvisibles(true);
            org.bukkit.scoreboard.Team teamTwo = scoreboard.registerNewTeam("TWO");
            teamTwo.setAllowFriendlyFire(false);
            teamTwo.setColor(Team.TWO.getDisplayColor());
            teamTwo.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            teamTwo.setCanSeeFriendlyInvisibles(true);
            getPlayers(Team.ONE).forEach(toAdd -> teamOne.addPlayer(toAdd.asBukkitPlayer()));
            getPlayers(Team.TWO).forEach(toAdd -> teamTwo.addPlayer(toAdd.asBukkitPlayer()));

            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 2f);
        }
        this.gameStartTitle();
    }

    public boolean hasStarted() {
        return started;
    }

    public boolean hasEnded() {
        return ended;
    }

    public void endGame(Team winner) {
        preGameTime = 0;
        gameTime = 0;
        ended = true;

        if (!started)
            started = true;

        for (DuelsPlayer player : players) {
            player.asBukkitPlayer().closeInventory();
            cleanupPlayer(player, GameMode.SPECTATOR, true);
            
        }
        
        for (DuelsPlayer player : this.spectators) {
        	this.gameEndTitle(player, winner, false);
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
                        this.gameEndTitle(player, winner, true);
                    } else {
                    	this.gameEndTitle(player, winner, false);
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
            cleanupPlayer(player, GameMode.SURVIVAL, false);
            player.endGame();
            player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
            PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
            ScoreboardController.get().changePlayer(player);
        }
        
        for (DuelsPlayer player : spectators) {
            removeSpectator(player);
        }
        
        this.spectators.clear();
        
        
        //redundant check because earlier we were getting a strange crash due to players being in the world at
        //the time of the crash
        for (Player player : world.getPlayers()) {
        	if (player.getWorld() == world) {
        		
        		player.teleport(PluginConfig.getSpawnLocation());
        	}
        }
        
        Bukkit.unloadWorld(world, false);
       // 
        //
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

    private void cleanupPlayer(DuelsPlayer player, GameMode gameMode, boolean setInventory) {
        player.asBukkitPlayer().getActivePotionEffects().forEach(effect -> player.asBukkitPlayer().removePotionEffect(effect.getType()));
        if (gameMode == GameMode.SURVIVAL) {
            player.asBukkitPlayer().setFlying(false);
            player.asBukkitPlayer().setAllowFlight(false);
            player.asBukkitPlayer().setGameMode(GameMode.SURVIVAL);
        } else if (gameMode == GameMode.SPECTATOR) {
            player.asBukkitPlayer().setGameMode(GameMode.SPECTATOR);
            player.asBukkitPlayer().setFlySpeed(0.2f);
        }
        player.asBukkitPlayer().setFoodLevel(18);
        player.asBukkitPlayer().setFireTicks(0);
        if (setInventory) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.get(), () -> {
                PlayerInventory inv = player.asBukkitPlayer().getInventory();
                inv.clear();
                for (Map.Entry<Integer, ItemStack> entry : PlayerController.get().getSpectateHotbar().entrySet()) {
                    inv.setItem(entry.getKey(), entry.getValue().clone());
                }
            }, 1L);
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
