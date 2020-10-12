package com.elytraforce.gunfight;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.elytraforce.gunfight.controllers.game.BombObject;
import com.elytraforce.gunfight.controllers.game.Duel;
import com.elytraforce.gunfight.controllers.game.Duel.Team;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;

import io.github.bananapuncher714.nbteditor.NBTEditor;
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
    	if (player.isSpectating() || player.isDead()) return;
    	
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
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event)
    {
    	
    	if (event.getEntity().getItemStack().isSimilar(Duel.bombItemStack())) {
    		event.getEntity().setInvulnerable(true);
    		Item e = event.getEntity();
    		e.setGlowing(true);
    	}
        
    }
    
    @EventHandler
    public void onPlantBomb(PlayerInteractEvent event) {
    	
    	DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
    	if (player.isSpectating() || player.isDead()) return;
    	
    	//if the player doesnt have the Bomb
    	if (event.getItem() == null) return;
    	
    	if (!event.getItem().isSimilar(Duel.bombItemStack())) {
    		return;
    	}
    	
    	if (player.getCurrentGame() != null && player.getCurrentGame().hasStarted()) {
    		
    		if (!(player.getCurrentGame().getType() == GameType.Type.ONE_V_ONE_BOMB || player.getCurrentGame().getType() == GameType.Type.TWO_V_TWO_BOMB || player.getCurrentGame().getType() == GameType.Type.THREE_V_THREE_BOMB)) {
    			return;
        	}
    		
    		if (player.getTeam() == Duel.Team.ONE) {
        		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

        			Block attemptPlant = event.getClickedBlock();
        			BombObject bomb = player.getCurrentGame().getBomb();
        			
        			if (!attemptPlant.getLocation().add(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
        				if (bomb.isBombPlanted()) { return;}
        				sendActionBar(player.asBukkitPlayer(), "&cYou cannot plant the bomb there!");
        				return;
        			}
        			
        			if (isCloseToList(player.getCurrentGame().getBombLocations(),attemptPlant.getLocation())) {
        				
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
        				if (bomb.isBombPlanted()) { return;}
        				sendActionBar(player.asBukkitPlayer(), "&cYou cannot plant the bomb there!");
        			}        			
        		}
        	} 
        }
    }
    
    @EventHandler
    public void onDropBomb(PlayerDeathEvent event) {
    	DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getEntity());
    	
    	if (player.getTeam() != Team.ONE) return;
    	
    	if (player.getCurrentGame() != null && player.getCurrentGame().hasStarted()) {
    		
    		if (!(player.getCurrentGame().getType() == GameType.Type.ONE_V_ONE_BOMB || player.getCurrentGame().getType() == GameType.Type.TWO_V_TWO_BOMB || player.getCurrentGame().getType() == GameType.Type.THREE_V_THREE_BOMB)) {
    			return;
        	}
    		
    		List<ItemStack> shit = Arrays.asList(player.asBukkitPlayer().getInventory().getContents());
    		shit.forEach(i -> {
    			if (i.isSimilar(Duel.bombItemStack())) {

    				player.getCurrentGame().getBomb().drop(player);
    				return;
    			}
    		});
    		
    	}

    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (NBTEditor.contains(event.getItemDrop().getItemStack(), "action")) {
            event.setCancelled(true);
            return;
        }
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
        
        if (player.isInGame()) {
        	
        	if (event.getItemDrop().getItemStack().isSimilar(Duel.bombItemStack())) {
        		
        		if (player.getTeam() != Team.ONE) {
        			event.setCancelled(true);
        			
        		}
        		
        		player.getCurrentGame().getBomb().drop(player);
        		event.getItemDrop().remove();
        	} else {
        		event.setCancelled(true);
        	}
        	
        }
            
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
    	DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
    	
    	if (player.isInGame()) {
    		
    		if (player.getTeam() == Team.TWO) {
    			event.setCancelled(true);
    			return;
    		}
    		
    		if (event.getItem().getItemStack().isSimilar(Duel.bombItemStack())) {
    			event.setCancelled(true);
    			event.getItem().remove();
    			
    			player.getCurrentGame().getBomb().pickup(player);
    		} else {
    			event.setCancelled(true);
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
