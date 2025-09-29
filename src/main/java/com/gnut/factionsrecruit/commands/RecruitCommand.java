package com.gnut.factionsrecruit.commands;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.interfaces.LandingUI;
import com.gnut.factionsrecruit.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Main command handler for /recruit
 *
 * Commands:
 * - /recruit - Opens the recruitment GUI (LandingUI)
 * - /recruit reload - Reloads the plugin configuration (admin)
 * - /recruit help - Shows help information
 * - /recruit stats - Shows plugin statistics (admin)
 */
public class RecruitCommand implements CommandExecutor, TabCompleter {

    private final FactionsRecruit plugin;

    // Subcommands for tab completion
    private static final List<String> SUBCOMMANDS = Arrays.asList("help", "reload", "stats");

    public RecruitCommand(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle subcommands
        if (args.length > 0) {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "help":
                    sendHelpMessage(sender);
                    return true;

                case "reload":
                    return handleReload(sender);

                case "stats":
                    return handleStats(sender);

                default:
                    MessageUtil.sendError(sender, "Unknown subcommand: " + subcommand);
                    MessageUtil.sendMessage(sender, "Use /recruit help for available commands");
                    return true;
            }
        }

        // No arguments - open GUI (only for players)
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "This command can only be used by players");
            MessageUtil.sendMessage(sender, "Use /recruit help for available commands");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("factionsrecruit.use")) {
            MessageUtil.sendError(player, "You don't have permission to use this command");
            return true;
        }

        // Open the LandingUI
        try {
            // TODO: Implement LandingUI opening
            // LandingUI.open(player);
            MessageUtil.sendInfo(player, "Opening recruitment interface...");
            MessageUtil.sendWarning(player, "GUI system is not yet implemented");
        } catch (Exception e) {
            MessageUtil.sendError(player, "Failed to open recruitment interface");
            plugin.getLogger().warning("Error opening LandingUI for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Handles the /recruit reload subcommand
     *
     * @param sender The command sender
     * @return true if successful
     */
    private boolean handleReload(CommandSender sender) {
        // Check permission
        if (!sender.hasPermission("factionsrecruit.admin.reload")) {
            MessageUtil.sendError(sender, "You don't have permission to reload the configuration");
            return true;
        }

        try {
            plugin.reloadPluginConfig();
            MessageUtil.sendSuccess(sender, "Configuration reloaded successfully");
        } catch (Exception e) {
            MessageUtil.sendError(sender, "Failed to reload configuration");
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Handles the /recruit stats subcommand
     *
     * @param sender The command sender
     * @return true if successful
     */
    private boolean handleStats(CommandSender sender) {
        // Check permission
        if (!sender.hasPermission("factionsrecruit.admin.stats")) {
            MessageUtil.sendError(sender, "You don't have permission to view statistics");
            return true;
        }

        // Fetch statistics asynchronously using CompletableFuture
        CompletableFuture.allOf(
            plugin.getDatabaseManager().getTotalPlayers(),
            plugin.getDatabaseManager().getRecruitingFactions(),
            plugin.getDatabaseManager().getPendingApplications(),
            plugin.getDatabaseManager().getPendingInvitations()
        ).thenRun(() -> {
            // Get results
            int totalPlayers = plugin.getDatabaseManager().getTotalPlayers().join();
            int recruitingFactions = plugin.getDatabaseManager().getRecruitingFactions().join();
            int pendingApplications = plugin.getDatabaseManager().getPendingApplications().join();
            int pendingInvitations = plugin.getDatabaseManager().getPendingInvitations().join();

            // Send statistics to sender (on main thread)
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MessageUtil.sendMessage(sender, "Plugin Statistics:");
                MessageUtil.sendInfo(sender, "Total Player Profiles: " + totalPlayers);
                MessageUtil.sendInfo(sender, "Recruiting Factions: " + recruitingFactions);
                MessageUtil.sendInfo(sender, "Pending Applications: " + pendingApplications);
                MessageUtil.sendInfo(sender, "Pending Invitations: " + pendingInvitations);
            });
        }).exceptionally(e -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MessageUtil.sendError(sender, "Failed to fetch statistics");
                plugin.getLogger().warning("Error fetching statistics: " + e.getMessage());
            });
            return null;
        });

        return true;
    }

    /**
     * Sends help message to the sender
     *
     * @param sender The command sender
     */
    private void sendHelpMessage(CommandSender sender) {
        MessageUtil.sendMessage(sender, "Available Commands:");
        sender.sendMessage("");

        if (sender.hasPermission("factionsrecruit.use")) {
            MessageUtil.sendInfo(sender, "/recruit - Open recruitment interface");
        }

        MessageUtil.sendInfo(sender, "/recruit help - Show this help message");

        if (sender.hasPermission("factionsrecruit.admin.reload")) {
            MessageUtil.sendInfo(sender, "/recruit reload - Reload configuration");
        }

        if (sender.hasPermission("factionsrecruit.admin.stats")) {
            MessageUtil.sendInfo(sender, "/recruit stats - View plugin statistics");
        }

        sender.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - suggest subcommands based on permissions
            String input = args[0].toLowerCase();

            for (String subcommand : SUBCOMMANDS) {
                // Check permissions for each subcommand
                boolean hasPermission = switch (subcommand) {
                    case "reload" -> sender.hasPermission("factionsrecruit.admin.reload");
                    case "stats" -> sender.hasPermission("factionsrecruit.admin.stats");
                    default -> true; // help is always available
                };

                if (hasPermission && subcommand.startsWith(input)) {
                    completions.add(subcommand);
                }
            }
        }

        return completions;
    }
}