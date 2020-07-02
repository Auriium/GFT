package dev.magicmq.duels.commands;

import dev.magicmq.duels.commands.subcommands.CreateKitCommand;
import dev.magicmq.duels.commands.subcommands.DeleteKitCommand;
import dev.magicmq.duels.commands.subcommands.KitsCommand;
import dev.magicmq.duels.commands.subcommands.QueueCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelsCommand implements TabExecutor {

    private List<SubCommand> subCommands;

    public DuelsCommand() {
        subCommands = new ArrayList<>();
        subCommands.add(new KitsCommand());
        subCommands.add(new QueueCommand());
        subCommands.add(new CreateKitCommand());
        subCommands.add(new DeleteKitCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify an argument!");
            return true;
        }

        SubCommand subCommand = getSubCmdFromArgument(args[0]);
        SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);

        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "Unrecognized argument " + args[0]);
            return true;
        }

        if (!subCommandMeta.permission().equals("") && !sender.hasPermission(subCommandMeta.permission())) {
            sender.sendMessage(ChatColor.RED + "Insufficient permissions!");
            return true;
        }

        if (subCommandMeta.playerOnly()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
                return true;
            }
        }

        String[] adjustedArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!subCommand.onCommand(sender, adjustedArgs)) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + args[0] + " " + subCommandMeta.usage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> toReturn = new ArrayList<>();

        if (args.length == 1) {
            for (SubCommand subCommand : subCommands) {
                SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
                if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().equals("")) {
                    if (subCommandMeta.playerOnly()) {
                        if (sender instanceof Player) {
                            toReturn.add(subCommandMeta.command());
                        }
                    } else {
                        toReturn.add(subCommandMeta.command());
                    }
                }
            }
        } else if (args.length > 1) {
            SubCommand subCommand = getSubCmdFromArgument(args[0]);
            if (subCommand != null) {
                SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
                if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().equals("")) {
                    if (subCommandMeta.playerOnly()) {
                        if (sender instanceof Player) {
                            String[] adjustedArgs = Arrays.copyOfRange(args, 1, args.length);
                            toReturn = subCommand.onTabComplete(sender, adjustedArgs);
                        }
                    } else {
                        String[] adjustedArgs = Arrays.copyOfRange(args, 1, args.length);
                        toReturn = subCommand.onTabComplete(sender, adjustedArgs);
                    }
                }
            }
        }

        return toReturn;
    }

    private SubCommand getSubCmdFromArgument(String arg) {
        arg = arg.toLowerCase();
        for (SubCommand subCommand : subCommands) {
            SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
            if (subCommandMeta.command().equals(arg))
                return subCommand;
            else if (Arrays.asList(subCommandMeta.aliases()).contains(arg))
                return subCommand;
        }
        return null;
    }
}