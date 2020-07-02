package dev.magicmq.duels.commands.subcommands;

import dev.magicmq.duels.commands.SubCommand;
import dev.magicmq.duels.commands.SubCommandMeta;
import dev.magicmq.duels.controllers.kits.Kit;
import dev.magicmq.duels.controllers.kits.KitsController;
import org.bukkit.command.CommandSender;

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
            KitsController.get().deleteKit(sender, args[1]);
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
