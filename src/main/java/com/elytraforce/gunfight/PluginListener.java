package com.elytraforce.gunfight;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.QueueController;
import com.elytraforce.gunfight.controllers.game.BombObject;
import com.elytraforce.gunfight.controllers.game.Duel;
import com.elytraforce.gunfight.controllers.game.DuelController;
import com.elytraforce.gunfight.controllers.game.gamemodes.ThreeVThreeBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.TwoVTwoBomb;
import com.elytraforce.gunfight.controllers.kits.KitsController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PluginListener implements Listener {

    private HashMap<Player, Location> deathLocations;

    public PluginListener() {
        this.deathLocations = new HashMap<>();
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (PlayerController.get().getDuelsPlayer(event.getPlayer()) == null) {
            PlayerController.get().playerJoined(event.getPlayer());
            event.getPlayer().teleport(PluginConfig.getSpawnLocation());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (PlayerController.get().getDuelsPlayer(event.getPlayer()) != null) {
            PlayerController.get().playerQuit(PlayerController.get().getDuelsPlayer(event.getPlayer()));
        }
    }

    /*@EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        DuelGenerateWaiting waiting = DuelController.get().getDuelWaiting(event.getWorld());
        if (waiting != null) {
            DuelController.get().worldLoadedCallback(waiting);
        }
    }*/

    //deprecate this
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            if (event.getCurrentItem() != null) {
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    if (NBTEditor.contains(event.getCurrentItem(), "action")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
        if (player.isInGame()) {
            if (!player.getCurrentGame().hasStarted())
                if (event.getFrom().distance(event.getTo()) != 0)
                    event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDualWield(PlayerSwapHandItemsEvent event) {
        if (event.getMainHandItem() != null) {
            if (event.getMainHandItem().getType() != Material.AIR) {
                if (NBTEditor.contains(event.getMainHandItem(), "action"))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer((Player) event.getEntity());
            if (player.isInGame()) {
                if (!player.getCurrentGame().hasStarted()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getEntity());
        if (player.isInGame() && !player.isDead()) {
            if (player.getCurrentGame().hasStarted()) {
                player.getCurrentGame().playerDied(player);
                deathLocations.put(event.getEntity(), event.getEntity().getLocation());
                new BukkitRunnable() {
                    public void run() {
                    	event.getEntity().spigot().respawn();
                    	
                    	
                    }
                }.runTaskLater(Main.get(), (long)1L);
                
                event.getDrops().clear();
                event.setDroppedExp(0);
                event.setDeathMessage(null);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (deathLocations.containsKey(event.getPlayer())) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
            player.getCurrentGame().playerRespawned(player);
            event.setRespawnLocation(deathLocations.remove(event.getPlayer()));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIgnite(EntityCombustEvent event) {
        if (event.getEntity() instanceof Player) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer((Player) event.getEntity());
            if (player.isInGame()) {
                if (!player.getCurrentGame().hasStarted() || player.getCurrentGame().hasEnded()) {
                    event.setCancelled(true);
                    return;
                }
                if (player.isDead()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
        if (event.getMessage().contains("quit"))
            return;
        if (player.isInGame() && !event.getPlayer().hasPermission("duels.admin")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(PluginConfig.getMessage("command-in-game"));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null) {
                if (event.getMaterial() != Material.AIR) {
                    ItemStack item = event.getItem();
                    if (NBTEditor.contains(item, "action")) {
                        event.setCancelled(true);
                        String action = NBTEditor.getString(item, "action");
                        if (action.equals("queue")) {
                            QueueController.get().openQueueInventory(event.getPlayer());
                            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
                        } else if (action.equals("kit")) {
                            KitsController.get().openKitsInventory(event.getPlayer());
                            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
                        } else if (action.equals("lobby")) {
                            Main.get().connectToLobby(event.getPlayer());
                        } else if (action.equals("quit")) {
                            DuelController.get().quitGame(player);
                        } else if (action.equals("spectate")) {
                        	DuelController.get().displayGUI(player);
                        }
                    }
                }
            }
        }
        
        
        
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer((Player) event.getEntity().getShooter());
            if (player.isInGame()) {
                player.setShotsFired(player.getShotsFired() + 1);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() != null) {
            if (event.getHitEntity() instanceof Player) {
                if (event.getEntity().getShooter() instanceof Player) {
                    DuelsPlayer player = PlayerController.get().getDuelsPlayer((Player) event.getEntity().getShooter());
                    if (player.isInGame()) {
                        player.setShotsHit(player.getShotsHit() + 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
        if (player.isInGame()) {
        	//sorry mq, but all that work with the block allowed check is for nothing
        	//now that i can directly add materials to the source ;))
        	
        	List<Material> listOfShit = new ArrayList<>();
        	//this is so i can see what is being put into the list and add more stuff ;)
        	listOfShit.add(Material.GLASS);
        	listOfShit.add(Material.GLASS_PANE);
        	listOfShit.add(Material.WHITE_STAINED_GLASS);
        	listOfShit.add(Material.WHITE_STAINED_GLASS_PANE);
        	listOfShit.add(Material.GRAY_STAINED_GLASS);
        	listOfShit.add(Material.GRAY_STAINED_GLASS_PANE);
        	listOfShit.add(Material.BLACK_STAINED_GLASS);
        	listOfShit.add(Material.BLACK_STAINED_GLASS_PANE);
        	listOfShit.add(Material.LIGHT_GRAY_STAINED_GLASS);
        	listOfShit.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            if (!listOfShit.contains(event.getBlock().getType())) {
                event.setCancelled(true);
            }
        }
    }

    //this isnt needed actually but i'm glad you added it :0
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
        if (player.isInGame()) {
        	List<Material> listOfShit = new ArrayList<>();
        	//this is so i can see what is being put into the list and add more stuff ;)
        	listOfShit.add(Material.GLASS);
        	listOfShit.add(Material.GLASS_PANE);
        	listOfShit.add(Material.WHITE_STAINED_GLASS);
        	listOfShit.add(Material.WHITE_STAINED_GLASS_PANE);
        	listOfShit.add(Material.GRAY_STAINED_GLASS);
        	listOfShit.add(Material.GRAY_STAINED_GLASS_PANE);
        	listOfShit.add(Material.BLACK_STAINED_GLASS);
        	listOfShit.add(Material.BLACK_STAINED_GLASS_PANE);
        	listOfShit.add(Material.LIGHT_GRAY_STAINED_GLASS);
        	listOfShit.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            if (!listOfShit.contains(event.getBlock().getType())) {
                event.setCancelled(true);
            }
        }
    }
    
    public void sendActionBar(Player player, String actionbar) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',actionbar)));
	}
}