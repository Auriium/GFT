package com.elytraforce.gunfight.controllers.game.gamemodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.game.BombObject;
import com.elytraforce.gunfight.controllers.game.Duel;
import com.elytraforce.gunfight.controllers.game.Duel.Team;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType.Type;
import com.elytraforce.gunfight.controllers.kits.KitsController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.controllers.scoreboard.ScoreboardController;
import com.elytraforce.gunfight.utils.ParticleUtils;

public class ThreeVThreeBomb implements GameType{
private Duel duel;
	
	public ThreeVThreeBomb(Duel duel) {
		this.duel = duel;
	}

	@Override
	public int getMaxPlayers() {
		// TODO Auto-generated method stub
		return 6;
	}

	@Override
	public int getMinPlayers() {
		// TODO Auto-generated method stub
		return 6;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "3v3 Bomb";
	}

	@Override
	public void startup() {
		
		//TODO: set up bomb here
		duel.setBomb(new BombObject(duel));
		
		ArrayList<DuelsPlayer> red = new ArrayList<>();
        ArrayList<DuelsPlayer> blue = new ArrayList<>();
        
        for (DuelsPlayer player : duel.getAllPlayers()) {
        	PlayerInventory inv = player.asBukkitPlayer().getInventory();
            inv.clear();
            for (Map.Entry<Integer, ItemStack> entry : PlayerController.get().getPreGameHotbar().entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue().clone());
            }

            //team setting over here
            player.setCurrentGame(duel);
            
            Boolean teamSwitch = new Random().nextBoolean();
            
            if (teamSwitch) {
            	red.add(player);
            } else {
            	blue.add(player);
            }
        }
        
        //should add the players to virtual storage teams. lets say there are 3 red and 1 blue
        while (red.size() > this.getMaxPlayers() / 2) {
        	blue.add(red.get(0));
        	red.remove(red.get(0));
        }
        
        while (blue.size() > this.getMaxPlayers() / 2) {
        	red.add(blue.get(0));
        	blue.remove(blue.get(0));
        }
        
        for (DuelsPlayer redPlayer : red) {
        	redPlayer.setTeam(Team.ONE);
        	redPlayer.asBukkitPlayer().teleport(duel.getTeamOneSpawns().get(red.indexOf(redPlayer)));
        	duel.getPlayersDisplay().put(redPlayer, redPlayer.getTeam().getDisplayColor() + "⬛ " + redPlayer.asBukkitPlayer().getName());
        }
        
        
        for (DuelsPlayer bluePlayer : blue) {
        	bluePlayer.setTeam(Team.TWO);
        	bluePlayer.asBukkitPlayer().teleport(duel.getTeamTwoSpawns().get(blue.indexOf(bluePlayer)));
        	duel.getPlayersDisplay().put(bluePlayer, bluePlayer.getTeam().getDisplayColor() + "⬛ " + bluePlayer.asBukkitPlayer().getName());
        }
        
        
        for (DuelsPlayer player : duel.getAllPlayers()) {

            ScoreboardController.get().changePlayer(player);

            player.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 5, false));
            player.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5, false));
            KitsController.get().openKitsInventory(player.asBukkitPlayer());

            player.sendMessage(PluginConfig.getMessage("entered-game")
                    .replace("%team%", player.getTeam().getDisplayColor() + player.getTeam().getDisplayName()));
        }
        
        //TODO: select a random red and give them bomb
	}

	@Override
	public boolean requiresMaxPlayers() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.THREE_V_THREE_BOMB;
	}
	
	@Override
	public void tick() {
		//**PRE GAME RELATED INFO**
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
          //**CURRENT GAME RELATED INFO**
        } else if (duel().gameTime > 0) {
        	duel().gameTime--;
        	
        	for (Location location : duel().getBombLocations()) {
        		ParticleUtils.particleCircle(location, Particle.REDSTONE, 400);
        	}
            
            for (DuelsPlayer p : duel().getSpectators()) {
        		//send actionbar p.asBukkitPlayer();
        		duel().sendActionBar(p.asBukkitPlayer(), "&aType /quit to return to GFT lobby!");
        	}
            
            //bomb handling shit here
            if (duel().getBomb() != null && duel().getBomb().isBombPlanted()) {
            	double progress = duel().gameTime / 40;
            	
            	duel().getBomb().getBossbar().setProgress(progress);
            	duel().getBomb().getBossbar().setTitle(duel().colorString("&cBomb detonates in &e&l00:" + duel().coolCountdownFormat()));
            }
            
            HashMap<Integer, String> messages = PluginConfig.getGameCountdown();
            if (messages.containsKey(duel().gameTime)) {
                duel().getAllPlayers().forEach(player -> {
                    player.sendMessage(messages.get(duel().gameTime));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f);
                });
            }
            if (duel().gameTime == 0) {
            	if (duel().getBomb() != null && duel().getBomb().isBombPlanted()) {
            		duel().getBomb().detonate();
            		
            		duel().endGame(Team.ONE);
            		return;
            	} 
            	duel().endGame(null);  
            }
          //**POST GAME RELATED INFO**
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
	
	@Override
	public void playerDiedInternal(DuelsPlayer player) {
		player.setDeaths(player.getDeaths() + 1);
        player.died();

        if (player.asBukkitPlayer().getKiller() != null) {
            DuelsPlayer killer = PlayerController.get().getDuelsPlayer(player.asBukkitPlayer().getKiller());
            killer.setKills(killer.getKills() + 1);
        }

        duel().getPlayersDisplay().put(player, player.getTeam().getDisplayColor() + "" + ChatColor.STRIKETHROUGH + "⬛ " + player.asBukkitPlayer().getName());

        if (duel().getBomb().isBombPlanted()) {
        	if (duel().getAlivePlayers(Team.ONE) == 0) {
        		//nothing should happen
            } else if (duel().getAlivePlayers(Team.TWO) == 0) {
            	duel().endGame(Team.ONE);
                return;
            }
        } else {
        	if (duel().getAlivePlayers(Team.ONE) == 0) {
            	duel().endGame(Team.TWO);
                return;
            } else if (duel().getAlivePlayers(Team.TWO) == 0) {
            	duel().endGame(Team.ONE);
                return;
            }
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
	
	
	@Override
	public Duel duel() {
		return this.duel;
	}
}
