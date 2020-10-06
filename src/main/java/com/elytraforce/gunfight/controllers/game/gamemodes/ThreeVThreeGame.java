package com.elytraforce.gunfight.controllers.game.gamemodes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.game.Duel;
import com.elytraforce.gunfight.controllers.game.Duel.Team;
import com.elytraforce.gunfight.controllers.kits.KitsController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.controllers.scoreboard.ScoreboardController;

public class ThreeVThreeGame implements GameType{
	
	private Duel duel;
	
	public ThreeVThreeGame(Duel duel) {
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
		return "3v3";
	}

	@Override
	public void startup() {
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
        
        
		
	}

	@Override
	public boolean requiresMaxPlayers() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.THREE_V_THREE;
	}
	
	
	@Override
	public Duel duel() {
		return this.duel;
	}

}
