package dev.magicmq.duels.commands;

import dev.magicmq.duels.Duels;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.QueueController;
import dev.magicmq.duels.controllers.game.DuelController;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("duels.command.reload")) {
                    try {
                        Duels.get().reload();
                        sender.sendMessage(ChatColor.GREEN + "Reload was successful.");
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "There was an error when reloading! See the console for details. It is recommended to restart to prevent unintended bugs.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Insufficient permissions!");
                }
            } else if (args[0].equalsIgnoreCase("queue")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("duels.command.queue")) {
                        DuelsPlayer duelsPlayer = PlayerController.get().getDuelsPlayer(player);
                        if (!duelsPlayer.isInGame()) {
                            QueueController.get().openQueueInventory(player);
                        } else {
                            player.sendMessage(PluginConfig.getMessage("queue-in-game"));
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Insufficient permissions!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "This is a player only command.");
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must specify an argument!");
        }
        return true;
    }
}