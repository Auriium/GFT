package com.elytraforce.gunfight;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.elytraforce.gunfight.controllers.game.BombObject;
import com.elytraforce.gunfight.controllers.game.Duel;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.game.gamemodes.OneVOneBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.ThreeVThreeBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.TwoVTwoBomb;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BombListener implements Listener{
	
	@EventHandler
    public void onTakeArmorBomb(PlayerArmorStandManipulateEvent event) {
    	DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
    	if (player.getCurrentGame() != null && player.getCurrentGame().hasStarted()) {
    		event.setCancelled(true);
    		return;
    	} 
    }
    
    @EventHandler
    public void onDefuseBomb(PlayerInteractAtEntityEvent event) {
    	
    	DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
    	
    	if (player.getCurrentGame() != null && player.getCurrentGame().hasStarted()) {
    		
    		if (!(player.getCurrentGame().getType() == GameType.Type.ONE_V_ONE_BOMB || player.getCurrentGame().getType() == GameType.Type.TWO_V_TWO_BOMB || player.getCurrentGame().getType() == GameType.Type.THREE_V_THREE_BOMB)) {
    			return;
        	}
    		
    		if (player.getTeam() == Duel.Team.TWO) {
    			
    			BombObject bomb = player.getCurrentGame().getBomb();
    			
    			if (!bomb.isBombPlanted()) { return; }
    			
    			if (event.getRightClicked() instanceof ArmorStand) {
    				if (event.getRightClicked().getCustomName().equals("Bomb")) {
            				
            				//add 1 to the bomb counter, record it
            				bomb.attemptDefuse(player);
            				int oldBombProgress = bomb.getDefuseProgress();
            				
            				new BukkitRunnable() {
            					public void run() {
            						if (bomb.isDefused()) {
            							return;
            						}
            						
            						if (oldBombProgress == bomb.getDefuseProgress()) {
            							bomb.resetDefuseProgress();
            						}
            					}
            					
            				}.runTaskLater(Main.get(), 10L);
    				}
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onPlantBomb(PlayerInteractEvent event) {
    	
    	DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
    	
    	if (player.getCurrentGame() != null && player.getCurrentGame().hasStarted()) {
    		
    		if (!(player.getCurrentGame().getType() == GameType.Type.ONE_V_ONE_BOMB || player.getCurrentGame().getType() == GameType.Type.TWO_V_TWO_BOMB || player.getCurrentGame().getType() == GameType.Type.THREE_V_THREE_BOMB)) {
    			return;
        	}
    		
    		if (player.getTeam() == Duel.Team.ONE) {
        		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

        			Block attemptPlant = event.getClickedBlock();
        			
        			if (!attemptPlant.getLocation().add(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
        				sendActionBar(player.asBukkitPlayer(), "&cYou cannot plant the bomb there!");
        				return;
        			}
        			
        			if (isCloseToList(player.getCurrentGame().getBombLocations(),attemptPlant.getLocation())) {
        				BombObject bomb = player.getCurrentGame().getBomb();
        				if (bomb.isAttemptingPlant()) {
        					
        					if (!attemptPlant.getLocation().equals(bomb.getPlantedBlock().getLocation())) {
        						return;
        					}
        					

        				} else {
        					//they have not attempted to plant yet so therefore we must set the first block location.
        					bomb.setPlantedBlock(attemptPlant);
        					
        				}
        				
        				//add 1 to the bomb counter, record it
        				bomb.attempt(player);
        				int oldBombProgress = bomb.getProgress();
        				
        				new BukkitRunnable() {
        					public void run() {
        						if (bomb.isBombPlanted()) {
        							return;
        						}
        						
        						if (oldBombProgress == bomb.getProgress()) {
        							
        							bomb.resetPlantProgress();
        						}
        					}
        					
        				}.runTaskLater(Main.get(), 10L);
        			} else {
        				sendActionBar(player.asBukkitPlayer(), "&cYou cannot plant the bomb there!");
        			}        			
        		}
        	} 
        }
    }
    
    public boolean isCloseToList(List<Location> list, Location target) {
    	for (Location location : list) {
    		if (target.distance(location) < 4) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public void sendActionBar(Player player, String actionbar) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',actionbar)));
	}
}
