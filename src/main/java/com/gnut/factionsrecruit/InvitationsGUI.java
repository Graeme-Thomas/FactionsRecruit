package com.gnut.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InvitationsGUI {

    private final FactionsRecruit plugin;

    public InvitationsGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openInvitationsUI(Player player) {
        // Create compact server-themed title
        String title = VisualUtils.createCompactServerTitle("Your Invitations");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Fill borders with PINK and WHITE glass panes
        ItemStack pinkGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.border-primary", Material.PINK_STAINED_GLASS_PANE), " ");
        ItemStack whiteGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " ");

        // Top row
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, (i % 2 == 0) ? pinkGlassPane : whiteGlassPane);
        }

        // Bottom row
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, (i % 2 == 0) ? pinkGlassPane : whiteGlassPane);
        }

        // Side columns
        for (int i = 9; i < 45; i += 9) {
            if (i % 2 == 0) {
                gui.setItem(i, pinkGlassPane);
                gui.setItem(i + 8, pinkGlassPane);
            } else {
                gui.setItem(i, whiteGlassPane);
                gui.setItem(i + 8, whiteGlassPane);
            }
        }

        // Get pending invitations for the player
        List<FactionInvitation> pendingInvitations = plugin.getDatabaseManager().getFactionInvitationsByPlayer(player.getUniqueId());

        // Filter only pending invitations that haven't expired
        List<FactionInvitation> validInvitations = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (FactionInvitation invitation : pendingInvitations) {
            if ("PENDING".equals(invitation.getStatus()) && invitation.getExpiresAt() > currentTime) {
                validInvitations.add(invitation);
            }
        }

        // If no invitations, show message
        if (validInvitations.isEmpty()) {
            ItemStack noInvitationsItem = createGuiItem(Material.BARRIER, "§cNo Pending Invitations");
            ItemMeta meta = noInvitationsItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7You have no pending faction invitations.");
            lore.add("§7Apply to factions or wait for invitations!");
            meta.setLore(lore);
            noInvitationsItem.setItemMeta(meta);
            gui.setItem(22, noInvitationsItem); // Center slot

            player.openInventory(gui);
            return;
        }

        // Display invitations (starting from slot 10)
        int slot = 10;
        for (FactionInvitation invitation : validInvitations) {
            if (slot >= 44) break; // Don't exceed available slots

            // Create invitation item
            ItemStack invitationItem = createGuiItem(Material.PAPER, "§6Faction Invitation");
            ItemMeta meta = invitationItem.getItemMeta();

            List<String> lore = new ArrayList<>();
            // Try to get faction name from the inviter (who is in the faction)
            Player inviter = Bukkit.getPlayer(invitation.getInvitedBy());
            String factionName = invitation.getFactionId(); // Default to ID

            if (inviter != null) {
                String inviterFactionName = plugin.getPapiIntegrationManager().getFactionName(inviter);
                if (inviterFactionName != null && !inviterFactionName.isEmpty() && !inviterFactionName.equalsIgnoreCase("none")) {
                    factionName = inviterFactionName;
                }
            } else {
                // Try with offline player
                String inviterFactionName = plugin.getPapiIntegrationManager().getFactionName(Bukkit.getOfflinePlayer(invitation.getInvitedBy()));
                if (inviterFactionName != null && !inviterFactionName.isEmpty() && !inviterFactionName.equalsIgnoreCase("none")) {
                    factionName = inviterFactionName;
                }
            }

            lore.add("§7From: §f" + factionName);

            String inviterName = inviter != null ? inviter.getName() : Bukkit.getOfflinePlayer(invitation.getInvitedBy()).getName();
            lore.add("§7Invited by: §f" + inviterName);

            lore.add("§7Received: §f" + new java.util.Date(invitation.getCreatedAt()).toString());
            lore.add("§7Expires: §f" + new java.util.Date(invitation.getExpiresAt()).toString());
            lore.add("");
            lore.add("§a§lLeft-click to ACCEPT");
            lore.add("§c§lRight-click to REJECT");
            lore.add("");
            lore.add("§8ID: " + invitation.getId()); // Store invitation ID for click handling

            meta.setLore(lore);
            invitationItem.setItemMeta(meta);

            gui.setItem(slot, invitationItem);
            slot++;
        }

        // Add help item
        ItemStack helpItem = createGuiItem(Material.BOOK, "§eHow to Use");
        ItemMeta helpMeta = helpItem.getItemMeta();
        List<String> helpLore = new ArrayList<>();
        helpLore.add("§7Left-click an invitation to §aaccept §7it");
        helpLore.add("§7Right-click an invitation to §creject §7it");
        helpLore.add("§7You can also use commands:");
        helpLore.add("§7/recruit accept <invitation_id>");
        helpLore.add("§7/recruit reject <invitation_id>");
        helpMeta.setLore(helpLore);
        helpItem.setItemMeta(helpMeta);
        gui.setItem(49, helpItem); // Bottom right area

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void handleInvitationClick(Player player, int invitationId, boolean accept) {
        FactionInvitation invitation = plugin.getDatabaseManager().getFactionInvitation(invitationId);

        if (invitation == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cInvitation not found!");
            player.closeInventory();
            return;
        }

        // Verify the invitation belongs to this player
        if (!invitation.getPlayerUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cThis invitation doesn't belong to you!");
            player.closeInventory();
            return;
        }

        // Check if invitation is still pending and not expired
        if (!"PENDING".equals(invitation.getStatus())) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cThis invitation has already been " + invitation.getStatus().toLowerCase() + "!");
            player.closeInventory();
            return;
        }

        if (invitation.getExpiresAt() <= System.currentTimeMillis()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cThis invitation has expired!");
            player.closeInventory();
            return;
        }

        // Check if player is already in a faction (if accepting)
        if (accept && plugin.getPapiIntegrationManager().isInFaction(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou are already in a faction! Leave your current faction before accepting invitations.");
            player.closeInventory();
            return;
        }

        // Process the invitation
        if (accept) {
            plugin.acceptInvitation(player, invitation);

            // Get faction name for message
            String factionName = invitation.getFactionId();
            Player inviter = Bukkit.getPlayer(invitation.getInvitedBy());
            if (inviter != null) {
                String inviterFactionName = plugin.getPapiIntegrationManager().getFactionName(inviter);
                if (inviterFactionName != null && !inviterFactionName.isEmpty() && !inviterFactionName.equalsIgnoreCase("none")) {
                    factionName = inviterFactionName;
                }
            }

            player.sendMessage(plugin.getConfigManager().getPrefix() + "§aYou have accepted the invitation to join " + factionName + "!");
        } else {
            plugin.rejectInvitation(player, invitation);

            // Get faction name for message
            String factionName = invitation.getFactionId();
            Player inviter = Bukkit.getPlayer(invitation.getInvitedBy());
            if (inviter != null) {
                String inviterFactionName = plugin.getPapiIntegrationManager().getFactionName(inviter);
                if (inviterFactionName != null && !inviterFactionName.isEmpty() && !inviterFactionName.equalsIgnoreCase("none")) {
                    factionName = inviterFactionName;
                }
            }

            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou have rejected the invitation from " + factionName + ".");
        }

        player.closeInventory();

        // Refresh the GUI to show updated invitations
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                openInvitationsUI(player);
            }
        }, 5L);
    }
}