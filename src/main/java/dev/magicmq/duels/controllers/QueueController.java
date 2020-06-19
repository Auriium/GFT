package dev.magicmq.duels.controllers;

import com.google.common.collect.Lists;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.game.DuelController;
import dev.magicmq.duels.controllers.game.DuelType;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import dev.magicmq.duels.utils.ItemUtils;

import java.util.*;
import java.util.stream.Collectors;

public class QueueController {

    private static QueueController instance;

    private HashMap<DuelType, Queue<DuelsPlayer>> queues;

    private Inventory queueInventory;

    private QueueController() {
        queues = new HashMap<>();
        for (DuelType type : DuelType.values()) {
            queues.put(type, new LinkedList<>());
        }

        ConfigurationSection config = PluginConfig.getQueueGui();
        ConfigurationSection settings = config.getConfigurationSection("settings");
        queueInventory = Bukkit.createInventory(null, settings.getInt("slots"), ChatColor.translateAlternateColorCodes('&', settings.getString("name")));
        List<Integer> fillerSlots = Lists.newArrayList(settings.getString("filler-slots").split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        ItemStack fillerItem = ItemUtils.parseGUIItem(config.getConfigurationSection("filler"));
        for (int slot : fillerSlots) {
            queueInventory.setItem(slot, fillerItem.clone());
        }
        for (String key : config.getKeys(false)) {
            if (key.equals("settings") || key.equals("filler"))
                continue;
            queueInventory.setItem(config.getInt(key + ".slot"), ItemUtils.parseGUIItem(config.getConfigurationSection(key)));
        }
    }

    public void openQueueInventory(Player player) {
        player.openInventory(queueInventory);
    }

    public boolean isQueueInventory(Inventory inventory) {
        return inventory.getName().equals(queueInventory.getName());
    }

    public void processClick(Player player, String action) {
        DuelsPlayer duelsPlayer = PlayerController.get().getDuelsPlayer(player);
        DuelType type = getQueuePlayerIsIn(duelsPlayer);
        if (action.equals("close")) {
            player.closeInventory();
        } else if (action.equals("queue_1v1")) {
            if (type != null) {
                if (type == DuelType.ONE_V_ONE) {
                    player.sendMessage(PluginConfig.getMessage("already-in-queue"));
                } else {
                    removePlayerFromQueue(duelsPlayer, type);
                    addPlayerToQueue(duelsPlayer, DuelType.ONE_V_ONE);
                }
            } else {
                addPlayerToQueue(duelsPlayer, DuelType.ONE_V_ONE);
            }
            player.closeInventory();
        } else if (action.equals("queue_2v2")) {
            if (type != null) {
                if (type == DuelType.TWO_V_TWO) {
                    player.sendMessage(PluginConfig.getMessage("already-in-queue"));
                } else {
                    removePlayerFromQueue(duelsPlayer, type);
                    addPlayerToQueue(duelsPlayer, DuelType.TWO_V_TWO);
                }
            } else {
                addPlayerToQueue(duelsPlayer, DuelType.TWO_V_TWO);
            }
            player.closeInventory();
        } else if (action.equals("queue_3v3")) {
            if (type != null) {
                if (type == DuelType.THREE_V_THREE) {
                    player.sendMessage(PluginConfig.getMessage("already-in-queue"));
                } else {
                    removePlayerFromQueue(duelsPlayer, type);
                    addPlayerToQueue(duelsPlayer, DuelType.THREE_V_THREE);
                }
            } else {
                addPlayerToQueue(duelsPlayer, DuelType.THREE_V_THREE);
            }
            player.closeInventory();
        }
    }

    public boolean addPlayerToQueue(DuelsPlayer player, DuelType type) {
        Queue<DuelsPlayer> queue = queues.get(type);
        queue.add(player);
        if (queue.size() >= type.getMaxPlayers()) {
            HashSet<DuelsPlayer> players = new HashSet<>();
            for (int i = 0; i < type.getMaxPlayers(); i++) {
                DuelsPlayer toAdd = queue.poll();
                toAdd.asBukkitPlayer().sendMessage(PluginConfig.getMessage("game-created"));
                players.add(toAdd);
            }
            DuelController.get().beginGame(players, type);
        }
        return true;
    }

    public void removePlayerFromQueue(DuelsPlayer player, DuelType type) {
        queues.get(type).remove(player);
    }

    public void removePlayerFromQueue(DuelsPlayer player) {
        for (Map.Entry<DuelType, Queue<DuelsPlayer>> entry : queues.entrySet()) {
            entry.getValue().remove(player);
        }
    }

    public DuelType getQueuePlayerIsIn(DuelsPlayer player) {
        for (Map.Entry<DuelType, Queue<DuelsPlayer>> entry : queues.entrySet()) {
            if (entry.getValue().contains(player))
                return entry.getKey();
        }
        return null;
    }

    public static QueueController get() {
        if (instance == null) {
            instance = new QueueController();
        }
        return instance;
    }

}
