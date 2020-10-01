package com.elytraforce.gunfight.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;

public class QuitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (sender instanceof Player) {
            DuelsPlayer player = PlayerController.get().getDuelsPlayer((Player) sender);

                if (player.isInGame()) {
                    player.getCurrentGame().playerQuit(player);
                    player.sendMessage(PluginConfig.getMessage("quit-game"));
                } else if (player.isSpectating()) {
                	player.getSpectatingGame().removeSpectator(player);
                    player.sendMessage(PluginConfig.getMessage("quit-game"));
                } else {
                    sender.sendMessage(PluginConfig.getMessage("not-in-game"));
                }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
        }
        return true;
    }
}
