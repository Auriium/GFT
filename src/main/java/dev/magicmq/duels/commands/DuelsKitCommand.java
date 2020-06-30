package dev.magicmq.duels.commands;

import dev.magicmq.duels.controllers.kits.KitsController;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelsKitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("duels.admin")) {
                if (args.length > 0) {
                    if (args.length > 1) {
                        if (args[0].equalsIgnoreCase("create")) {
                            if (args.length > 3) {
                                try {
                                    KitsController.get().createNewKit(player, args[1], Double.parseDouble(args[2]), args[3]);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(ChatColor.RED + "The kit cost must be a number!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Usage: /duelskit create <kitname> <cost> <permission>");
                            }
                        } else if (args[0].equalsIgnoreCase("delete")) {
                            KitsController.get().deleteKit(player, args[1]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /duelskit <create/delete> [kitname] [cost] [permission]");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You must specify a kit name! Usage: /duelskit <create/delete> [kitname]");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /duelskit <create/delete> [kitname]");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Insufficient permissions!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
        }
        return true;
    }

}
