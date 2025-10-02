package com.dirtygang.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecruitCommandExecutor implements CommandExecutor {

    private final FactionsRecruit plugin;
    private final RecruitGUI recruitGUI;

    public RecruitCommandExecutor(FactionsRecruit plugin, RecruitGUI recruitGUI) {
        this.plugin = plugin;
        this.recruitGUI = recruitGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            recruitGUI.openMainMenu(player, null);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "resume":
                plugin.getManageResumeGUI().openManageResumeUI(player);
                break;
            case "apply":
                if (args.length > 1) {
                    // Handle application to a specific faction
                } else {
                    player.sendMessage("§cUsage: /recruit apply <faction>");
                }
                break;
            case "browse":
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("players")) {
                        plugin.setMainMenuDisplayMode(FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE);
                        recruitGUI.openMainMenu(player, null);
                    } else if (args[1].equalsIgnoreCase("factions")) {
                        plugin.setMainMenuDisplayMode(FactionsRecruit.MainMenuDisplayMode.FACTION_MODE);
                        recruitGUI.openMainMenu(player, null);
                    }
                } else {
                    recruitGUI.openMainMenu(player, null);
                }
                break;
            case "applications":
                plugin.getPendingApplicationsGUI().openPendingApplicationsUI(player);
                break;
            case "invitations":
                plugin.getInvitationsGUI().openInvitationsUI(player);
                break;
            case "accept":
                if (args.length > 1) {
                    try {
                        int invitationId = Integer.parseInt(args[1]);
                        plugin.getInvitationsGUI().handleInvitationClick(player, invitationId, true);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cUsage: /recruit accept <invitation_id>");
                    }
                } else {
                    player.sendMessage("§cUsage: /recruit accept <invitation_id>");
                }
                break;
            case "reject":
                if (args.length > 1) {
                    try {
                        int invitationId = Integer.parseInt(args[1]);
                        plugin.getInvitationsGUI().handleInvitationClick(player, invitationId, false);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cUsage: /recruit reject <invitation_id>");
                    }
                } else {
                    player.sendMessage("§cUsage: /recruit reject <invitation_id>");
                }
                break;
            default:
                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer != null) {
                    plugin.getPlayerInfoGUI().openPlayerInfoDisplay(player, targetPlayer);
                } else {
                    player.sendMessage("§cPlayer not found.");
                }
                break;
        }

        return true;
    }
}