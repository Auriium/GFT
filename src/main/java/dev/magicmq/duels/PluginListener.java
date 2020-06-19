package dev.magicmq.duels;

import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.kits.KitsController;
import dev.magicmq.duels.controllers.QueueController;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PluginListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (PlayerController.get().getDuelsPlayer(event.getPlayer()) == null) {
            PlayerController.get().playerJoined(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (PlayerController.get().getDuelsPlayer(event.getPlayer()) != null) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
            if (QueueController.get().getQueuePlayerIsIn(player) != null)
                QueueController.get().removePlayerFromQueue(player);
            PlayerController.get().playerQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            if (event.getCurrentItem() != null) {
                if (QueueController.get().isQueueInventory(event.getClickedInventory())) {
                    event.setCancelled(true);
                    ItemStack item = event.getCurrentItem();
                    if (NBTEditor.contains(item, "action")) {
                        QueueController.get().processClick((Player) event.getWhoClicked(), NBTEditor.getString(item, "action"));
                    }
                } else if (KitsController.get().isKitsInventory(event.getClickedInventory())) {
                    event.setCancelled(true);
                    ItemStack item = event.getCurrentItem();
                    if (NBTEditor.contains(item, "action")) {
                        KitsController.get().processClick(PlayerController.get().getDuelsPlayer((Player) event.getWhoClicked()), NBTEditor.getString(item, "action"), event.getClick());
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
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer((Player) event.getEntity());
            if (player.asBukkitPlayer().getHealth() - event.getFinalDamage() <= 0) {
                if (player.isInGame()) {
                    player.getCurrentGame().playerDied(player);
                    event.setCancelled(true);
                    player.asBukkitPlayer().setHealth(player.asBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
                    player.asBukkitPlayer().setFoodLevel(18);
                }
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        DuelsPlayer player = PlayerController.get().getDuelsPlayer(event.getPlayer());
        if (player.isInGame()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(PluginConfig.getMessage("command-in-game"));
        }
    }
}