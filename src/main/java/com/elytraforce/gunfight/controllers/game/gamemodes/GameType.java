package com.elytraforce.gunfight.controllers.game.gamemodes;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.game.Duel;
import com.elytraforce.gunfight.controllers.game.Duel.Team;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType.Type;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;

public interface GameType {
	
	public int getMaxPlayers();
	
	public int getMinPlayers();
	
	public String getDisplayName();
	
	public void startup();
	
	public boolean requiresMaxPlayers();
	
	public Type getType();
	
	public default void tick() {
		if (duel().getPreGameTime() > 0) {
			
            duel().preGameTime--;
            HashMap<Integer, String> messages = PluginConfig.getPreGameCountdown();
            if (messages.containsKey(duel().getPreGameTime())) {
                duel().getAllPlayers().forEach(player -> {
                    player.sendMessage(messages.get(duel().preGameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }

            HashMap<Integer, String> titleMessages = PluginConfig.getGameCountdownTitle();
            if (titleMessages.containsKey(duel().preGameTime)) {
                String[] split = titleMessages.get(duel().preGameTime).split("\\|");
                duel().getAllPlayers().forEach(player -> player.asBukkitPlayer().sendTitle(split[0], split[1], 5, 20, 10));
            }
            if (duel().preGameTime == 0) {
                duel().startGame();
            }
        } else if (duel().gameTime > 0) {
        	duel().gameTime--;
            
            for (DuelsPlayer p : duel().getSpectators()) {
        		//send actionbar p.asBukkitPlayer();
        		duel().sendActionBar(p.asBukkitPlayer(), "&aType /quit to return to GFT lobby!");
        	}
            
            //bomb handling shit here
            
            HashMap<Integer, String> messages = PluginConfig.getGameCountdown();
            if (messages.containsKey(duel().gameTime)) {
                duel().getAllPlayers().forEach(player -> {
                    player.sendMessage(messages.get(duel().gameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }
            if (duel().gameTime == 0) {
            	duel().endGame(null);

                
            }
        } else if (duel().postGameTime > 0) {
        	duel().postGameTime--;
            HashMap<Integer, String> messages = PluginConfig.getPostGameCountdown();
            if (messages.containsKey(duel().postGameTime)) {
                duel().getAllPlayers().forEach(player -> {
                    player.sendMessage(messages.get(duel().postGameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }
            if (duel().postGameTime == 0) {
                duel().closeGame();
            }
        }
	}
	
	public default void playerDiedInternal(DuelsPlayer player) {
		player.setDeaths(player.getDeaths() + 1);
        player.died();

        if (player.asBukkitPlayer().getKiller() != null) {
            DuelsPlayer killer = PlayerController.get().getDuelsPlayer(player.asBukkitPlayer().getKiller());
            killer.setKills(killer.getKills() + 1);
        }

        duel().getPlayersDisplay().put(player, player.getTeam().getDisplayColor() + "" + ChatColor.STRIKETHROUGH + "⬛ " + player.asBukkitPlayer().getName());

        if (duel().getAlivePlayers(Team.ONE) == 0) {
        	duel().endGame(Team.TWO);
            return;
        } else if (duel().getAlivePlayers(Team.TWO) == 0) {
        	duel().endGame(Team.ONE);
            return;
        }

        if (duel().getType() != Type.ONE_V_ONE) {
            if (player.asBukkitPlayer().getKiller() != null) {
                duel().getAllPlayers().forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("kill-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%killer%", player.asBukkitPlayer().getKiller().getName())
                        .replace("%amount%", "" + duel().getAlivePlayers(player.getTeam()))));
                duel().getSpectators().forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("kill-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%killer%", player.asBukkitPlayer().getKiller().getName())
                        .replace("%amount%", "" + duel().getAlivePlayers(player.getTeam()))));
            } else {
            	duel().getAllPlayers().forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("die-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%amount%", "" + duel().getAlivePlayers(player.getTeam()))));
                duel().getSpectators().forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("die-broadcast")
                        .replace("%player%", player.getName())
                        .replace("%amount%", "" + duel().getAlivePlayers(player.getTeam()))));
            }
        }
	}
	
	public Duel duel();
	
	enum Type {
		ONE_V_ONE(new OneVOneGame(null)),
		ONE_V_ONE_BOMB(new OneVOneBomb(null)),
		TWO_V_TWO(new TwoVTwoGame(null)),
	    THREE_V_THREE(new ThreeVThreeGame(null)),
		TWO_V_TWO_BOMB(new TwoVTwoBomb(null)),
		THREE_V_THREE_BOMB(new ThreeVThreeBomb(null)),
		THE_HIDDEN(null);
		
		private GameType type;
		
		Type(GameType type) {
			this.type = type;
		}
		
		public GameType getGameType() {
			return type;
		}
	}
	
	
}
