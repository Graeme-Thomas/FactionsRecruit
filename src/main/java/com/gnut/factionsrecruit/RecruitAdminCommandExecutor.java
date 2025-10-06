package com.gnut.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RecruitAdminCommandExecutor implements CommandExecutor {

    private final FactionsRecruit plugin;

    public RecruitAdminCommandExecutor(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("factionsrecruit.admin")) {
            sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }

        if (args.length == 0) {
            sendAdminHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage( "Configuration reloaded.");
                break;
            case "cleanup":
                new CleanupTask(plugin).run();
                sender.sendMessage( "Cleanup task executed.");
                break;
            case "stats":
                // Display stats
                break;
            case "reset":
                if (args.length > 1) {
                    // Reset player data
                } else {
                    sender.sendMessage("§cUsage: /recruitadmin reset <player>");
                }
                break;
            case "faction":
                if (args.length > 2 && args[1].equalsIgnoreCase("status")) {
                    // Show faction status
                } else {
                    sender.sendMessage("§cUsage: /recruitadmin faction <faction> status");
                }
                break;
            default:
                sendAdminHelp(sender);
                break;
        }

        return true;
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage("§6FactionsRecruit Admin Commands:");
        sender.sendMessage("§e/recruitadmin reload - Reloads the config.");
        sender.sendMessage("§e/recruitadmin cleanup - Runs the cleanup task.");
        sender.sendMessage("§e/recruitadmin stats - Shows plugin stats.");
        sender.sendMessage("§e/recruitadmin reset <player> - Resets a player\'s data.");
        sender.sendMessage("§e/recruitadmin faction <faction> status - Shows a faction\'s recruitment status.");
    }
}

