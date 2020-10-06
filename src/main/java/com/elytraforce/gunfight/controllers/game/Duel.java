package com.elytraforce.gunfight.controllers.game;

import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;

import com.elytraforce.gunfight.Main;
import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.QueueController;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType.Type;
import com.elytraforce.gunfight.controllers.game.gamemodes.OneVOneBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.OneVOneGame;
import com.elytraforce.gunfight.controllers.game.gamemodes.ThreeVThreeBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.ThreeVThreeGame;
import com.elytraforce.gunfight.controllers.game.gamemodes.TwoVTwoBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.TwoVTwoGame;
import com.elytraforce.gunfight.controllers.kits.Kit;
import com.elytraforce.gunfight.controllers.kits.KitsController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.controllers.scoreboard.ScoreboardController;
import com.elytraforce.gunfight.storage.SQLStorage;

import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

@SuppressWarnings("deprecation")
public class Duel {

    private UUID uniqueId;
    private GameType.Type type;
    private GameType gameType;
    
    private World world;
    private String mapName;
    private HashSet<DuelsPlayer> players;
    private HashSet<DuelsPlayer> playersCopy;
    private HashMap<DuelsPlayer, String> playersDisplay;
    private HashSet<DuelsPlayer> spectators;
    
    private boolean started;
    private boolean ended;
    private Location teleportLocation;

    public int preGameTime;
    public int gameTime; 
    public int postGameTime;
    
    private List<Location> teamOneSpawns;
    private List<Location> teamTwoSpawns;
    private List<Location> bombLocations;
    private BombObject bombObject;  
    
    public int getPreGameTime() { return this.preGameTime; }
    public void setPreGameTime(int time) { this.preGameTime = time; }
    public int getGameTime() { return this.gameTime; }
    public void setGameTime(int time) { this.gameTime = time; }
    public int getPostGameTime() { return this.postGameTime; }
    public void setPostGameTime(int time) { this.postGameTime = time; }
    
    public boolean hasStarted() { return started; }
    public boolean hasEnded() { return ended; }
    
    public BombObject getBomb() { return this.bombObject; }
    public void setBomb(BombObject object) { this.bombObject = object; }
    
    public Collection<String> getPlayersDisplayValues() { return playersDisplay.values(); }
    public HashMap<DuelsPlayer, String> getPlayersDisplay() { return this.playersDisplay; }
    
    public List<Location> getBombLocations() { return this.bombLocations; }
    public List<Location> getTeamOneSpawns() { return this.teamOneSpawns; }
    public List<Location> getTeamTwoSpawns() { return this.teamTwoSpawns; }
    
    public HashSet<DuelsPlayer> getAllPlayers() { return this.players; }
    public HashSet<DuelsPlayer> getSpectators() { return this.spectators; }

    public UUID getUniqueId() { return uniqueId; }
    public String getMapName() { return mapName; }
    
    public GameType.Type getType() { return type; }
    public GameType getGameType() { return this.gameType; }
    
    public String colorString(String string) { return ChatColor.translateAlternateColorCodes('&', string); }
    
    @SuppressWarnings("unchecked")
	public Duel(UUID uniqueId, GameType.Type type, World world, String mapName, 
	HashSet<DuelsPlayer> players, List<Location> teamOneSpawns, List<Location> teamTwoSpawns, 
	List<Location> bombLocation) {
    	
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
        this.teamOneSpawns = teamOneSpawns;
        this.teamTwoSpawns = teamTwoSpawns;
        
        this.bombLocations = new ArrayList<>();
        this.bombLocations = bombLocation;
        
        switch(type) {
        	case ONE_V_ONE:
        		this.gameType = new OneVOneGame(this);
        		break;
        	case TWO_V_TWO:
        		this.gameType = new TwoVTwoGame(this);
        		break;
        	case THREE_V_THREE:
        		this.gameType = new ThreeVThreeGame(this);
        		break;
        	case ONE_V_ONE_BOMB:
        		this.gameType = new OneVOneBomb(this);
        		break;
        	case TWO_V_TWO_BOMB:
        		this.gameType = new TwoVTwoBomb(this);
        		break;
        	case THREE_V_THREE_BOMB:
        		this.gameType = new ThreeVThreeBomb(this);
        		break;
        	default:
        		break;
        }
        
        gameType.startup();
    }
    
    public void tick() {
    	gameType.tick();
    }
    
    public void addSpectator(DuelsPlayer player) {
    	if (!(QueueController.get().getQueuePlayerIsIn(player) == null)) {
    		QueueController.get().removePlayerFromQueue(player);
    	}
    	PlayerInventory inv = player.asBukkitPlayer().getInventory();
        inv.clear();
        for (Map.Entry<Integer, ItemStack> entry : PlayerController.get().getSpectateHotbar().entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().clone());
        }

        
        cleanupPlayer(player, GameMode.SPECTATOR, true);
        
        player.setSpectatingGame(this);
        player.setSpectating(true);
        this.spectators.add(player);
        
        player.asBukkitPlayer().teleport(this.teleportLocation);
        
        ScoreboardController.get().changePlayer(player);
    }
    
    public void removeSpectator(DuelsPlayer player) {
    	cleanupPlayer(player, GameMode.SURVIVAL, false);
        
        player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
        PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
        
        player.setSpectatingGame(null);
        player.setSpectating(false);
        players.remove(player);
        
        ScoreboardController.get().changePlayer(player);
    }
    
    //Titles Shit
    
    public void broadcastMessage(String string) {
    	for (DuelsPlayer player : players) {
    		player.asBukkitPlayer().sendMessage(colorString(string));
    		player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1, 1);
    	}
    	
    	for (DuelsPlayer player : spectators) {
    		player.asBukkitPlayer().sendMessage(colorString(string));
    		player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1, 1);
    	}
    }
    
    public void broadcastTitle(String string) {
    	for (DuelsPlayer player : players) {
    		player.asBukkitPlayer().sendTitle(colorString(""), colorString(string), 10, 10, 10);
    		player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1, 1);
    	}
    	
    	for (DuelsPlayer player : spectators) {
    		player.asBukkitPlayer().sendTitle(colorString(""), colorString(string), 10, 10, 10);
    		player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1, 1);
    	}
    }
    
    public void broadcastSound(String string) {
    	for (DuelsPlayer player : players) {
    		player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), string, SoundCategory.MASTER, 1, 1);
    	}
    	
    	for (DuelsPlayer player : spectators) {
    		player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), string, SoundCategory.MASTER, 1, 1);
    	}
    }
    
    public void sendTitle(DuelsPlayer player, String string) {
    	player.asBukkitPlayer().sendTitle(colorString(""), colorString(string), 10, 10, 10);
		player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1, 1);
    }
    
    public void gameStartTitle() {
    	for (DuelsPlayer player : players) {
    		player.asBukkitPlayer().sendTitle(colorString("&4&lGame Start!"), colorString("&7GFT - &e&l" + gameType.getDisplayName()), 10, 10, 10);
    	}
    	
    	for (DuelsPlayer player : spectators) {
    		player.asBukkitPlayer().sendTitle(colorString("&4&lGame Start!"), colorString("&7GFT - &e&l" + gameType.getDisplayName()), 10, 10, 10);
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
        }.runTaskLater(Main.get(), (long)40L);
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
    	
    	new BukkitRunnable() {
            public void run() {
            	
            	player.asBukkitPlayer().sendTitle(colorString("&4&lGame Over!"), colorString(winningColor + "" + ChatColor.BOLD + winningTeam + " &7&lWon!"), 10, 10, 10);
            	
            }
        }.runTaskLater(Main.get(), (long)10L);
    	
    	new BukkitRunnable() {
            public void run() {
            	
            		player.asBukkitPlayer().sendTitle(colorString("&c&lSending you to lobby!"), "", 10, 10, 10);
            	
            }
        }.runTaskLater(Main.get(), (long)40L);
    }
    
    //NOT TITLE SHIT
    
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
    	this.gameType.playerDiedInternal(player);
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

    public void endGame(Team winner) {
        preGameTime = 0;
        gameTime = 0;
        ended = true;

        if (!started)
            started = true;
        
        if (this.bombObject != null) {
        	this.bombObject.clean();
        }

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
        	cleanupPlayer(player, GameMode.SURVIVAL, false);
            
            player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
            PlayerController.get().giveSpawnInv(player.asBukkitPlayer());
            
            player.setSpectatingGame(null);
            player.setSpectating(false);
            
            ScoreboardController.get().changePlayer(player);
        }
        
        //redundant check because earlier we were getting a strange crash due to players being in the world at
        //the time of the crash
        
        Bukkit.unloadWorld(world, false);
       // 
        //
        DuelController.get().endGame(this);
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

    public int getAlivePlayers(Team team) {
        int i = 0;
        for (DuelsPlayer player : getPlayers(team)) {
            if (!player.isDead())
                i++;
        }
        return i;
    }
    
    
    
    public void sendActionBar(Player player, String actionbar) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',actionbar)));
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
    
    public String coolCountdownFormat() {
		int x = this.gameTime;
		
		if (x > 9) { 
			return Integer.toString(x);
		} else {
			return "0" + x;
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
    
    //BOMB RELATED STUFF GOES HERE.
    
}
