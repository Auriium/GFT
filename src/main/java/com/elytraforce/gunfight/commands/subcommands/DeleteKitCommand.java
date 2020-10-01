package com.elytraforce.gunfight.commands.subcommands;

import org.bukkit.command.CommandSender;

import com.elytraforce.gunfight.commands.SubCommand;
import com.elytraforce.gunfight.commands.SubCommandMeta;
import com.elytraforce.gunfight.controllers.kits.Kit;
import com.elytraforce.gunfight.controllers.kits.KitsController;

import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "deletekit",
        aliases = {"kitdelete"},
        permission = "duels.command.deletekit",
        usage = "<kitname>"
)
public class DeleteKitCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            KitsController.get().deleteKit(sender, args[0]);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> toReturn = new ArrayList<>();
        for (Kit kit : KitsController.get().getKits()) {
            toReturn.add(kit.getName());
        }
        return toReturn;
    }

}
