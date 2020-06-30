package dev.magicmq.duels.commands;

import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (sender instanceof Player) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer((Player) sender);
            if (player.asBukkitPlayer().hasPermission("duels.command.quit")) {
                if (player.isInGame()) {
                    player.getCurrentGame().playerQuit(player);
                    player.sendMessage(PluginConfig.getMessage("quit-game"));
                } else {
                    sender.sendMessage(PluginConfig.getMessage("not-in-game"));
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Insufficient permissions!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
        }
        return true;
    }
}
