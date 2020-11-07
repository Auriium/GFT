package com.elytraforce.gunfight.controllers;

import com.elytraforce.gunfight.config.PluginConfig;

import com.elytraforce.gunfight.controllers.game.DuelController;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.game.gamemodes.OneVOneBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.OneVOneGame;
import com.elytraforce.gunfight.controllers.game.gamemodes.ThreeVThreeBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.ThreeVThreeGame;
import com.elytraforce.gunfight.controllers.game.gamemodes.TwoVTwoBomb;
import com.elytraforce.gunfight.controllers.game.gamemodes.TwoVTwoGame;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.utils.ItemUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.github.bananapuncher714.nbteditor.NBTEditor;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class QueueController {

    private static QueueController instance;

    private HashMap<GameType.Type, Queue<DuelsPlayer>> queues;
    private HashMap<DuelsPlayer, Long> queueTimes;
    private Menu queueMenu;

    private QueueController() {
        queues = new HashMap<>();
        for (GameType.Type type : GameType.Type.values()) {
            queues.put(type, new LinkedList<>());
        }
        queueTimes = new HashMap<>();

        ConfigurationSection config = PluginConfig.getQueueGui();
        ConfigurationSection settings = config.getConfigurationSection("settings");
        queueMenu = ChestMenu.builder(4)
                .title(ChatColor.translateAlternateColorCodes('&', settings.getString("name")))
                .build();
        
        List<Integer> fillerSlots = Lists.newArrayList(settings.getString("filler-slots").split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        ItemStack fillerItem = ItemUtils.parseGUIItem(config.getConfigurationSection("filler"));
        for (int slot : fillerSlots) {

            Slot menuSlot = queueMenu.getSlot(slot);
            
            menuSlot.setItemTemplate(p -> {
        	    ItemStack item = fillerItem.clone();
        	    ItemMeta itemMeta = item.getItemMeta();
        	    item.setItemMeta(itemMeta);
        	    return item;
        	});
            
            menuSlot.setClickHandler((p, info) -> {
                ItemStack item = menuSlot.getItem(p);
                if (NBTEditor.contains(item, "action")) {
                    QueueController.get().processClick(PlayerController.get().getDuelsPlayer(p), NBTEditor.getString(item, "action"));
                }
            });
        }
        for (String key : config.getKeys(false)) {
            if (key.equals("settings") || key.equals("filler"))
                continue;
            
            Slot menuSlot = queueMenu.getSlot(config.getInt(key + ".slot"));
            menuSlot.setItemTemplate(p -> {
        	    ItemStack item = ItemUtils.parseGUIItem(config.getConfigurationSection(key));
        	    ItemMeta itemMeta = item.getItemMeta();
        	    item.setItemMeta(itemMeta);
        	    return item;
        	});
            
            menuSlot.setClickHandler((p, info) -> {
                // Additional functionality goes here
                ItemStack item = menuSlot.getItem(p);
                if (NBTEditor.contains(item, "action")) {
                    QueueController.get().processClick(PlayerController.get().getDuelsPlayer(p), NBTEditor.getString(item, "action"));
                }
            });
        }
    }

    public void openQueueInventory(Player player) {
        queueMenu.open(player);
    }

    public void processClick(DuelsPlayer player, String action) {
    	GameType.Type type = getQueuePlayerIsIn(player);
    	GameType actualType;
    	
    	switch(action) {
    		case "queue_1v1":
    			actualType = new OneVOneGame(null);
                
            	this.actionMethod(player, actualType, type);
                player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    			break;
    		case "queue_2v2":
    			actualType = new TwoVTwoGame(null);
                
            	this.actionMethod(player, actualType, type);
                player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    			break;
    		case "queue_3v3":
    			actualType = new ThreeVThreeGame(null);
                
            	this.actionMethod(player, actualType, type);
                player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    			break;
    		case "queue_1v1_bomb":
    			actualType = new OneVOneBomb(null);
                
            	this.actionMethod(player, actualType, type);
                player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    			break;
    		case "queue_2v2_bomb":
    			actualType = new TwoVTwoBomb(null);
                
            	this.actionMethod(player, actualType, type);
                player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    			break;
    		case "queue_3v3_bomb":
    			actualType = new ThreeVThreeBomb(null);
                this.actionMethod(player, actualType, type);
            	
            	player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    			break;
    		case "queue_cancel":
    			if (type != null) {
                    removePlayerFromQueue(player, type);
                } else {
                    player.sendMessage(PluginConfig.getMessage("not-in-queue"));
                }
                player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    			break;
    		default:
    			break;
    	}
        
        player.asBukkitPlayer().closeInventory();
    }
    
    public void actionMethod(DuelsPlayer player, GameType actualType, GameType.Type currentType) {
    	if (currentType != null) {
            if (currentType == actualType.getType()) {
                player.sendMessage(PluginConfig.getMessage("already-in-queue")
                        .replace("%queue%", actualType.getDisplayName()));
            } else {
                removePlayerFromQueue(player, currentType);
                player.sendMessage(PluginConfig.getMessage("queue-join")
                        .replace("%queue%", actualType.getDisplayName())
                        .replace("%players%", "" + (getNumberInQueue(actualType.getType()) + 1))
                        .replace("%maxplayers%", "" + (actualType.getMaxPlayers())));
                addPlayerToQueue(player, actualType.getType());
            }
        } else {
            player.sendMessage(PluginConfig.getMessage("queue-join")
            		.replace("%queue%", actualType.getDisplayName())
                    .replace("%players%", "" + (getNumberInQueue(actualType.getType()) + 1))
                    .replace("%maxplayers%", "" + (actualType.getMaxPlayers())));
            addPlayerToQueue(player, actualType.getType());
        }
    }

    public void addPlayerToQueue(DuelsPlayer player, GameType.Type type) {
    	
        Queue<DuelsPlayer> queue = queues.get(type);
        queue.add(player);
        queue.forEach(toSend -> toSend.sendMessage(PluginConfig.getMessage("player-joined-queue")
            .replace("%player%", player.asBukkitPlayer().getName())
            .replace("%players%", "" + queue.size())
            .replace("%maxplayers%", "" + type.getGameType().getMaxPlayers())));
        if (queue.size() >= type.getGameType().getMaxPlayers()) {
            List<DuelsPlayer> players = new ArrayList<>();
            for (int i = 0; i < type.getGameType().getMaxPlayers(); i++) {
                DuelsPlayer toAdd = queue.poll();
                queueTimes.remove(toAdd);
                toAdd.sendMessage(PluginConfig.getMessage("game-created"));
                players.add(toAdd);
                toAdd.asBukkitPlayer().playSound(toAdd.asBukkitPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, PluginConfig.getSoundVolume(), 1f);
            }
            Collections.shuffle(players);
            DuelController.get().queueGame(Sets.newHashSet(players), type);
        } else {
            queue.forEach(toSend -> toSend.asBukkitPlayer().playSound(toSend.asBukkitPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, PluginConfig.getSoundVolume(), 1f));
            queueTimes.put(player, Instant.now().getEpochSecond());
        }
    }

    public void removePlayerFromQueue(DuelsPlayer player, GameType.Type type) {
        queues.get(type).remove(player);
        queueTimes.remove(player);
    }

    public void removePlayerFromQueue(DuelsPlayer player) {
        for (Map.Entry<GameType.Type, Queue<DuelsPlayer>> entry : queues.entrySet()) {
            entry.getValue().remove(player);
        }
        queueTimes.remove(player);
    }

    public GameType.Type getQueuePlayerIsIn(DuelsPlayer player) {
        for (Map.Entry<GameType.Type, Queue<DuelsPlayer>> entry : queues.entrySet()) {
            if (entry.getValue().contains(player))
                return entry.getKey();
        }
        return null;
    }

    public int getNumberInQueue(GameType.Type type) {
        return queues.get(type).size();
    }

    public long getTimeSpentInQueue(DuelsPlayer player) {
        return Instant.now().getEpochSecond() - queueTimes.get(player);
    }

    public static QueueController get() {
        if (instance == null) {
            instance = new QueueController();
        }
        return instance;
    }

}
