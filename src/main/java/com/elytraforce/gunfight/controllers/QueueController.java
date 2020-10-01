package com.elytraforce.gunfight.controllers;

import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.game.DuelController;
import com.elytraforce.gunfight.controllers.game.DuelType;
import com.elytraforce.gunfight.controllers.kits.KitsController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.utils.ItemUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.github.bananapuncher714.nbteditor.NBTEditor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
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

    private HashMap<DuelType, Queue<DuelsPlayer>> queues;
    private HashMap<DuelsPlayer, Long> queueTimes;
    private Menu queueMenu;

    private QueueController() {
        queues = new HashMap<>();
        for (DuelType type : DuelType.values()) {
            queues.put(type, new LinkedList<>());
        }
        queueTimes = new HashMap<>();

        ConfigurationSection config = PluginConfig.getQueueGui();
        ConfigurationSection settings = config.getConfigurationSection("settings");
        queueMenu = ChestMenu.builder(3)
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
        DuelType type = getQueuePlayerIsIn(player);
        if (action.equals("queue_1v1")) {
            if (type != null) {
                if (type == DuelType.ONE_V_ONE) {
                    player.sendMessage(PluginConfig.getMessage("already-in-queue")
                            .replace("%queue%", type.getDisplayName()));
                } else {
                    removePlayerFromQueue(player, type);
                    player.sendMessage(PluginConfig.getMessage("queue-leave")
                            .replace("%queue%", type.getDisplayName()));
                    player.sendMessage(PluginConfig.getMessage("queue-join")
                            .replace("%queue%", DuelType.ONE_V_ONE.getDisplayName())
                            .replace("%players%", "" + (getNumberInQueue(DuelType.ONE_V_ONE) + 1))
                            .replace("%maxplayers%", "" + (DuelType.ONE_V_ONE.getMaxPlayers())));
                    addPlayerToQueue(player, DuelType.ONE_V_ONE);
                }
            } else {
                player.sendMessage(PluginConfig.getMessage("queue-join")
                        .replace("%queue%", DuelType.ONE_V_ONE.getDisplayName())
                        .replace("%players%", "" + (getNumberInQueue(DuelType.ONE_V_ONE) + 1))
                        .replace("%maxplayers%", "" + (DuelType.ONE_V_ONE.getMaxPlayers())));
                addPlayerToQueue(player, DuelType.ONE_V_ONE);
            }
            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
        } else if (action.equals("queue_2v2")) {
            if (type != null) {
                if (type == DuelType.TWO_V_TWO) {
                    player.sendMessage(PluginConfig.getMessage("already-in-queue")
                            .replace("%queue%", type.getDisplayName()));
                } else {
                    removePlayerFromQueue(player, type);
                    player.sendMessage(PluginConfig.getMessage("queue-leave")
                            .replace("%queue%", type.getDisplayName()));
                    player.sendMessage(PluginConfig.getMessage("queue-join")
                            .replace("%queue%", DuelType.TWO_V_TWO.getDisplayName())
                            .replace("%players%", "" + (getNumberInQueue(DuelType.TWO_V_TWO) + 1))
                            .replace("%maxplayers%", "" + (DuelType.TWO_V_TWO.getMaxPlayers())));
                    addPlayerToQueue(player, DuelType.TWO_V_TWO);
                }
            } else {
                player.sendMessage(PluginConfig.getMessage("queue-join")
                        .replace("%queue%", DuelType.TWO_V_TWO.getDisplayName())
                        .replace("%players%", "" + (getNumberInQueue(DuelType.TWO_V_TWO) + 1))
                        .replace("%maxplayers%", "" + (DuelType.TWO_V_TWO.getMaxPlayers())));
                addPlayerToQueue(player, DuelType.TWO_V_TWO);
            }
            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
        } else if (action.equals("queue_3v3")) {
            if (type != null) {
                if (type == DuelType.THREE_V_THREE) {
                    player.sendMessage(PluginConfig.getMessage("already-in-queue")
                            .replace("%queue%", type.getDisplayName()));
                } else {
                    removePlayerFromQueue(player, type);
                    player.sendMessage(PluginConfig.getMessage("queue-leave")
                            .replace("%queue%", type.getDisplayName()));
                    player.sendMessage(PluginConfig.getMessage("queue-join")
                            .replace("%queue%", DuelType.THREE_V_THREE.getDisplayName())
                            .replace("%players%", "" + (getNumberInQueue(DuelType.THREE_V_THREE) + 1))
                            .replace("%maxplayers%", "" + (DuelType.THREE_V_THREE.getMaxPlayers())));
                    addPlayerToQueue(player, DuelType.THREE_V_THREE);
                }
            } else {
                player.sendMessage(PluginConfig.getMessage("queue-join")
                        .replace("%queue%", DuelType.THREE_V_THREE.getDisplayName())
                        .replace("%players%", "" + (getNumberInQueue(DuelType.THREE_V_THREE) + 1))
                        .replace("%maxplayers%", "" + (DuelType.THREE_V_THREE.getMaxPlayers())));
                addPlayerToQueue(player, DuelType.THREE_V_THREE);
            }
            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
        } else if (action.equals("queue_leave")) {
            if (type != null) {
                player.sendMessage(PluginConfig.getMessage("queue-leave")
                        .replace("%queue%", type.getDisplayName()));
                removePlayerFromQueue(player, type);
            } else {
                player.sendMessage(PluginConfig.getMessage("not-in-queue"));
            }
            player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
        }
        player.asBukkitPlayer().closeInventory();
    }

    public void addPlayerToQueue(DuelsPlayer player, DuelType type) {
        Queue<DuelsPlayer> queue = queues.get(type);
        queue.add(player);
        queue.forEach(toSend -> toSend.sendMessage(PluginConfig.getMessage("player-joined-queue")
            .replace("%player%", player.asBukkitPlayer().getName())
            .replace("%players%", "" + queue.size())
            .replace("%maxplayers%", "" + type.getMaxPlayers())));
        if (queue.size() >= type.getMaxPlayers()) {
            List<DuelsPlayer> players = new ArrayList<>();
            for (int i = 0; i < type.getMaxPlayers(); i++) {
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

    public void removePlayerFromQueue(DuelsPlayer player, DuelType type) {
        queues.get(type).remove(player);
        queueTimes.remove(player);
    }

    public void removePlayerFromQueue(DuelsPlayer player) {
        for (Map.Entry<DuelType, Queue<DuelsPlayer>> entry : queues.entrySet()) {
            entry.getValue().remove(player);
        }
        queueTimes.remove(player);
    }

    public DuelType getQueuePlayerIsIn(DuelsPlayer player) {
        for (Map.Entry<DuelType, Queue<DuelsPlayer>> entry : queues.entrySet()) {
            if (entry.getValue().contains(player))
                return entry.getKey();
        }
        return null;
    }

    public int getNumberInQueue(DuelType type) {
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
