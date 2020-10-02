package com.elytraforce.gunfight.controllers.game;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.elytraforce.gunfight.Main;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BombObject {
	
	private Location plantLocation;
	private Block plantedBlock;
	private ArmorStand bombStand;
	private DuelsPlayer planter;
	private Duel instance;
	
	public BombObject(Location plantLocation, Duel instance) {
		this.plantLocation = plantLocation;
		this.plantedBlock = null;
		this.bombStand = null;
		this.planter = null;
		this.instance = instance;
	}
	
	//rework this to be a boolean that is checked.
	public void attemptPlant(DuelsPlayer player) {
		this.planter = player;
		
		Block planted = player.asBukkitPlayer().getTargetBlock(null, 5).getLocation().add(0, 1, 0).getBlock();

		if (planted.getLocation().distance(this.plantLocation) < 4) {
			this.plantedBlock = planted;
			
			//generate stand (but prepare to delete it)
			this.bombStand = this.plantedBlock.getLocation().getWorld().spawn(this.plantedBlock.getLocation(), ArmorStand.class);
			
			this.bombStand.setGravity(false);
	        this.bombStand.setCanPickupItems(false);
	        this.bombStand.setVisible(true);
	        
	        new BukkitRunnable() {
				
				int progress = 0;
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 20, 100);
				
	            public void run() {
	            	progress++;
	            	planter.asBukkitPlayer().addPotionEffect(effect);
	            	
	            	
	            	if (progress > 9) {
	            		
	            		this.cancel();
	            		
	            	}
	            	
	            	if (!check()) {
	            		
	            		
	            		planter.asBukkitPlayer().removePotionEffect(PotionEffectType.SLOW);
	            		this.cancel();
	            	}
	            	

	            	
	            }
	        }.runTaskTimer(Main.get(), (long)0L, (long)20L);
	        
	        //You cannot plant there!
	        sendActionBar(player.asBukkitPlayer(), "&cYou cannot plant the bomb there!");
	        return;
		}
		
		return;
	}
	
	public boolean check() {
		
		if (this.planter == null) {
			return false;
		}
		
		if (this.planter.asBukkitPlayer().getTargetBlock(null, 5).getLocation().add(0, 1, 0).getBlock() != BombObject.this.plantedBlock) {
			return false;
		}
		
		
		return true;
	}
	
	public void deploy() {
		
		
		
        //call duel bomb planted.
		
	}
	
	public void defuse() {
		
	}
	
	public void detonate() {
		
	}
	
	
	public void sendActionBar(Player player, String actionbar) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',actionbar)));
	}
	

}
