package com.elytraforce.gunfight.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elytraforce.gunfight.commands.SubCommand;
import com.elytraforce.gunfight.commands.SubCommandMeta;
import com.elytraforce.gunfight.controllers.player.PlayerController;

@SubCommandMeta(
        command = "bomb",
        permission = "duels.command.bomb",
        playerOnly = true
)

public class SpawnBombCommand implements SubCommand{

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		PlayerController.get().getDuelsPlayer((Player) sender);
		
		
		
		return false;
	}
	

}
