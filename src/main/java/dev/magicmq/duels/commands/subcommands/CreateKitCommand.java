package dev.magicmq.duels.commands.subcommands;

import dev.magicmq.duels.commands.SubCommand;
import dev.magicmq.duels.commands.SubCommandMeta;
import dev.magicmq.duels.controllers.kits.KitsController;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommandMeta(
        command = "createkit",
        aliases = {"kitcreate"},
        permission = "duels.command.createkit",
        playerOnly = true,
        usage = "<kitname> <cost> <permission> <guislot>"
)
public class CreateKitCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length > 3) {
            try {
                KitsController.get().createNewKit(player, args[0], Double.parseDouble(args[1]), args[2], Integer.parseInt(args[3]));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "The kit cost and GUI slot must both be numbers!");
            }
        } else {
            return false;
        }
        return true;
    }
}
