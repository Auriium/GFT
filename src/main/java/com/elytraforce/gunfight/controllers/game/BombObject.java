package com.elytraforce.gunfight.controllers.game;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.elytraforce.gunfight.Main;
import com.elytraforce.gunfight.controllers.game.Duel.Team;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BombObject {
	
	private Block plantedBlock;
	private ArmorStand bombStand;
	
	private DuelsPlayer planter;
	private DuelsPlayer defuser;
	private Duel instance;
	
	private int bombProgress;
	private int defuseProgress;
	
	private boolean isPlanted;
	private boolean isDefused;
	private boolean attemptingPlant;
	private boolean attemptingDefuse;
	
	private boolean isDropped;
	
	private BossBar bombBar;
	
	
	//Bomb location is where the bomb SHOULD be placed
	
	public BombObject(Duel instance) {
		this.plantedBlock = null;
		this.bombStand = null;
		this.planter = null;
		this.instance = instance;
		
		this.isDefused = false;
		this.isPlanted = false;
		
		this.attemptingPlant = false;
		this.attemptingDefuse = false;
		
		this.bombProgress = 0;
		this.defuseProgress = 0;
		
		this.bombBar = null;
		
		this.isDropped = false;
		
	}
	
	public boolean isDropped() { return this.isDropped; }
	public boolean isDefused() { return this.isDefused; }
	public boolean isAttemptingPlant() { return this.attemptingPlant; }
	public void setAttemptingPlant(boolean e) { this.attemptingPlant = e;}
	
	public int getProgress() { return this.bombProgress;}
	public void setProgress(Integer progress) { this.bombProgress = progress;}
	
	public Block getPlantedBlock() { return this.plantedBlock; }
	
	public void setDefuseProgress(int progress) { this.defuseProgress = progress; }
	public int getDefuseProgress() { return this.defuseProgress; }
	
	public void resetPlantProgress() {
		this.bombProgress = 0;
		this.attemptingPlant = false;
		
		this.plantedBlock = null;
		
		this.bombStand.remove();
		this.bombStand = null;
	}
	
	public void resetDefuseProgress() {
		this.defuseProgress = 0;
		this.attemptingDefuse = false;
	}
	
	public void setPlantedBlock(Block block) { this.plantedBlock = block; }
	public static String colorString(String string) {return ChatColor.translateAlternateColorCodes('&', string);}
	
	public void attemptDefuse(DuelsPlayer player) {
		if (!this.isPlanted) { return; }
		if (player.isDead()) { return; }
		this.defuseProgress++;
		this.defuseProgress++;
		
		if (!attemptingDefuse) {
			this.defuser = player;
			
			attemptingDefuse = true;
		} else {
			if (player != defuser) { return; }
		}
		
		defuseCounter(defuseProgress);
		defuser.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 100));
		
		if (this.defuseProgress >= 100) {
			planter.asBukkitPlayer().removePotionEffect(PotionEffectType.SLOW);
			
			this.defuse();
			
			attemptingDefuse = false;
			isPlanted = false;
			isDefused = true;
			
			//run bomb planted here.
			
		}
	}
	
	public void attempt(DuelsPlayer player) {
		
		if (this.isPlanted) { return; }
		if (player.isDead()) { return; }
		
		this.bombProgress++;
		this.bombProgress++;
		
		if (!attemptingPlant) {
			
			this.planter = player;
			
			attemptingPlant = true;
			Location loc = this.plantedBlock.getLocation().add(0.5,-0.5,0.5);
			
			this.bombStand = planter.asBukkitPlayer().getWorld().spawn(loc, ArmorStand.class);
			
			this.bombStand.setGravity(false);
		    this.bombStand.setCanPickupItems(false);
		    this.bombStand.setVisible(false);
		    this.bombStand.setMarker(true);
		    
		    this.bombStand.setCustomName("Bomb");
		    this.bombStand.setCustomNameVisible(false);
		    
		        
		    this.bombStand.setHelmet(new ItemStack(Material.TNT, 1));
		} else {
			if (player != planter) { return; }
		}
		
		deployCounter(bombProgress);
		planter.asBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 100));
		
		
		if (plantedBlock == null) { return; }
		
		if (this.bombProgress >= 50) {
			planter.asBukkitPlayer().removePotionEffect(PotionEffectType.SLOW);
			player.asBukkitPlayer().getInventory().remove(Duel.bombItemStack());
			this.deploy();
			
			attemptingPlant = false;
			isPlanted = true;
			
			//run bomb planted here.
			
		}
	}
	
	public void drop(DuelsPlayer player) {
		if (this.isDropped) { return; }
		
		this.isDropped = true;
		for (DuelsPlayer p : this.instance.getPlayers(Team.ONE)) {
			p.sendMessage(colorString("&c&l" + player.asBukkitPlayer().getName() + "&7 dropped the bomb!"));
		}
		
		
		Player pPlayer = player.asBukkitPlayer();
		pPlayer.getWorld().dropItemNaturally(player.asBukkitPlayer().getLocation(), Duel.bombItemStack());
	}
	
	public void pickup(DuelsPlayer player) {
		if (!this.isDropped) { return; }
		
		this.isDropped = false;
		for (DuelsPlayer p : this.instance.getPlayers(Team.ONE)) {
			p.sendMessage(colorString("&c&l" + player.asBukkitPlayer().getName() + "&7 picked up the bomb!"));
		}
		
		Player pPlayer = player.asBukkitPlayer();
		pPlayer.getInventory().addItem(Duel.bombItemStack());
	}
	
	public void hasTheBomb(DuelsPlayer player) {
		
		for (DuelsPlayer p : this.instance.getPlayers(Team.ONE)) {
			p.sendMessage(colorString("&c&l" + player.asBukkitPlayer().getName() + "&7 has the bomb!"));
		}
		
		Player pPlayer = player.asBukkitPlayer();
		pPlayer.getInventory().addItem(Duel.bombItemStack());
	}
	
	//rework this to be a boolean that is checked.
	
	public boolean isBombPlanted() {
		return this.isPlanted;
	}
	
	public void setBombPlanted(boolean e) {
		this.isPlanted = e;
	}
	
	public BossBar getBossbar() {
		return this.bombBar;
	}
	
	public void deploy() {
		
		this.isPlanted = true;
		this.instance.broadcastSound("block.metal.break");
		this.instance.broadcastMessage("&c&lBOMB HAS BEEN PLANTED!");
        
		this.instance.setGameTime(46);
		this.bombBar = Bukkit.createBossBar(Duel.colorString("&cBomb detonates in &e&l00:" + coolCountdownFormat()), BarColor.WHITE, BarStyle.SOLID, BarFlag.DARKEN_SKY);
		
		this.bombStand.setMarker(false);
		
		for (DuelsPlayer player : instance.getAllPlayers()) {
			bombBar.addPlayer(player.asBukkitPlayer());
		}
		
		
		
	}
	
	public String coolCountdownFormat() {
		int x = this.instance.getGameTime();
		
		if (x > 9) { 
			return Integer.toString(x);
		} else {
			return "0" + x;
		}
		
		
	}
	
	public void cancelPlant() {
		
		if (this.isPlanted) {
			return;
		}
		
		bombStand.remove();
		bombStand = null;
		

	}
	
	public void clean() {
		if (this.bombBar != null) {
			this.bombBar.removeAll();
		}
		
		if (bombStand != null) {
			bombStand.remove();
			bombStand = null;
		}
		
	}
	
	public void defuse() {
		this.bombBar.removeAll();
		
		bombStand.remove();
		bombStand = null;
		
		instance.endGame(Team.TWO);
	}
	
	public void detonate() {
		this.bombBar.removeAll();
		
		this.bombStand.getLocation().getWorld().createExplosion(this.bombStand.getLocation().add(0, 4, 0), 50, true);
		
		bombStand.remove();
		bombStand = null;
		
		
	}
	
	public void defuseCounter(int progress) {
		
		if (progress > 0 && progress < 10) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░&7░░░░░░░░░");
			return;
		} else if (progress >= 10 && progress < 20) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░&7░░░░░░░░");
			return;
		} else if (progress >= 20 && progress < 30) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░&7░░░░░░░");
			return;
		} else if (progress >= 30 && progress < 40) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░░&7░░░░░░");
			return;
		} else if (progress >= 40 && progress < 50) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░░░&7░░░░░");
			return;
		} else if (progress >= 50 && progress < 60) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░░░░&7░░░░");
			return;
		} else if (progress >= 60 && progress < 70) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░░░░░&7░░░");
			return;
		} else if (progress >= 70 && progress < 80) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░░░░░░&7░░");
			return;
		} else if (progress >= 80 && progress < 90) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░░░░░░░&7░");
			return;
		} else if (progress >= 90 && progress < 100) {
			sendActionBar(defuser.asBukkitPlayer(), "&eDefusing.. &c░░░░░░░░░░");
			return;
		} else if (progress == 100) {
			sendActionBar(defuser.asBukkitPlayer(), "&cDefused!");
			return;
		}
	}
	
	public void deployCounter(int progress) {
		
		if (progress > 0 && progress < 5) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░&7░░░░░░░░░");
			return;
		} else if (progress >= 5 && progress < 10) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░&7░░░░░░░░");
			return;
		} else if (progress >= 10 && progress < 15) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░&7░░░░░░░");
			return;
		} else if (progress >= 15 && progress < 20) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░░&7░░░░░░");
			return;
		} else if (progress >= 20 && progress < 25) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░░░&7░░░░░");
			return;
		} else if (progress >= 25 && progress < 30) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░░░░&7░░░░");
			return;
		} else if (progress >= 30 && progress < 35) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░░░░░&7░░░");
			return;
		} else if (progress >= 35 && progress < 40) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░░░░░░&7░░");
			return;
		} else if (progress >= 40 && progress < 45) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░░░░░░░&7░");
			return;
		} else if (progress >= 45 && progress < 50) {
			sendActionBar(planter.asBukkitPlayer(), "&ePlanting.. &c░░░░░░░░░░");
			return;
		} else if (progress == 50) {
			sendActionBar(planter.asBukkitPlayer(), "&cPlanted!");
			return;
		}
	}
	
	public void sendActionBar(Player player, String actionbar) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',actionbar)));
	}
	

}
