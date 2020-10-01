package com.elytraforce.gunfight.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elytraforce.gunfight.commands.SubCommand;
import com.elytraforce.gunfight.commands.SubCommandMeta;
import com.elytraforce.gunfight.controllers.kits.KitsController;

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
