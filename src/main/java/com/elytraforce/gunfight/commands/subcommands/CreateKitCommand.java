package com.elytraforce.gunfight.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elytraforce.gunfight.commands.SubCommand;
import com.elytraforce.gunfight.commands.SubCommandMeta;
import com.elytraforce.gunfight.controllers.kits.KitsController;

@SubCommandMeta(
        command = "createkit",
        aliases = {"kitcreate"},
        permission = "duels.command.createkit",
        playerOnly = true,
        usage = "<kitname> <cost> <permission> <guislot> <isHidden>"
)
public class CreateKitCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length > 3) {
            try {
                KitsController.get().createNewKit(player, args[0], Double.parseDouble(args[1]), args[2], Integer.parseInt(args[3]), Boolean.valueOf(args[4]));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "The kit cost and GUI slot must both be numbers!");
            }
        } else {
            return false;
        }
        return true;
    }
}
