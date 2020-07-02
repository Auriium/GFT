package dev.magicmq.duels.commands.subcommands;

import dev.magicmq.duels.commands.SubCommand;
import dev.magicmq.duels.commands.SubCommandMeta;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.QueueController;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
