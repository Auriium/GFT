package com.elytraforce.gunfight.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elytraforce.gunfight.commands.SubCommand;
import com.elytraforce.gunfight.commands.SubCommandMeta;
import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.QueueController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;

@SubCommandMeta(
        command = "queue",
        permission = "duels.command.queue",
        playerOnly = true
)
public class QueueCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        DuelsPlayer duelsPlayer = PlayerController.get().getDuelsPlayer((Player) sender);
        if (!duelsPlayer.isInGame()) {
            QueueController.get().openQueueInventory(duelsPlayer.asBukkitPlayer());
        } else {
            duelsPlayer.sendMessage(PluginConfig.getMessage("queue-in-game"));
        }
        return true;
    }
}
