package dev.magicmq.duels.commands.subcommands;

import dev.magicmq.duels.commands.SubCommand;
import dev.magicmq.duels.commands.SubCommandMeta;
import dev.magicmq.duels.controllers.kits.KitsController;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommandMeta(
        command = "kits",
        permission = "duels.command.kits",
        playerOnly = true
)
public class KitsCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        KitsController.get().openKitsInventory((Player) sender);
        return true;
    }
}
