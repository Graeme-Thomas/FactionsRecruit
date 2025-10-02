package com.dirtygang.factionsrecruit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.Collections;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;

import com.dirtygang.factionsrecruit.SelectionEditorTemplate;

import me.clip.placeholderapi.libs.kyori.adventure.platform.viaversion.ViaFacet.Chat;

public class PlayerListener implements Listener {

    private final FactionsRecruit plugin;
    private final RecruitGUI recruitGUI;

    public PlayerListener(FactionsRecruit plugin, RecruitGUI recruitGUI) {
        this.plugin = plugin;
        this.recruitGUI = recruitGUI;
    }

    /**
     * Utility method to strip all color codes from inventory titles for comparison
     * This handles both traditional § color codes and modern gradient titles
     */
    private String stripColors(String text) {
        if (text == null) return null;
        return ChatColor.stripColor(text);
    }

    /**
     * Normalizes server-themed titles for comparison
     * Removes decorative arrows and converts small caps back to normal text
     */
    private String normalizeServerTitle(String text) {
        if (text == null) return null;

        String stripped = stripColors(text);

        // Remove server decorative arrows
        stripped = stripped.replace("►", "").replace("◄", "");

        // Remove extra spaces
        stripped = stripped.trim();

        // Convert small caps back to normal text
        return convertSmallCapsToNormal(stripped);
    }

    /**
     * Converts small caps Unicode characters back to normal uppercase
     */
    private String convertSmallCapsToNormal(String text) {
        if (text == null || text.isEmpty()) return text;

        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";
        String normal = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            int index = smallCaps.indexOf(c);
            if (index != -1) {
                result.append(normal.charAt(index));
            } else {
                result.append(Character.toUpperCase(c));
            }
        }
        return result.toString();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SelectionEditorTemplate) {
            return;
        }
        String inventoryTitle = event.getView().getTitle();
        String strippedTitle = stripColors(inventoryTitle);
        String normalizedTitle = normalizeServerTitle(inventoryTitle);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int clickedSlot = event.getSlot();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (plugin.getGuiManager().hasActiveGUI(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getGuiManager().getClickHandler(player.getUniqueId()).handleClick(player, clickedSlot, event.getClick());
            return;
        }

        // Main Browser UI - check for recruitment browser (handles gradient titles)
        if (normalizedTitle.contains("RECRUITMENT BROWSER") || strippedTitle.equals(stripColors(plugin.getConfigManager().getGuiTitle()))) {
            event.setCancelled(true);
            handleMainMenuClick(player, clickedSlot, clickedItem);
        } 
        // Resume Editor
        else if (normalizedTitle.equals("RESUME EDITOR")) {
            event.setCancelled(true);
            handleEditorUIClick(player, event.getInventory(), clickedItem, clickedSlot, true);
        }
        // Faction Requirements Editor
        else if (normalizedTitle.equals("REQUIREMENTS EDITOR")) {
            event.setCancelled(true);
            handleEditorUIClick(player, event.getInventory(), clickedItem, clickedSlot, false);
        }
        // Manage Resume UI - handle gradient titles
        else if (normalizedTitle.contains("PROFILE MANAGER")) {
            event.setCancelled(true);
            handleManageResumeUIClick(player, clickedSlot, clickedItem);
        }
        // Pending Applications UI (Faction Owner Mode)
        else if (normalizedTitle.contains("PENDING APPLICATIONS")) {
            event.setCancelled(true);
            handleFactionOwnerPendingApplicationsUIClick(player, clickedSlot, clickedItem);
        }
        // Pending Applications UI (Player Mode)
        else if (normalizedTitle.equals("YOUR APPLICATIONS")) {
            event.setCancelled(true);
            handlePlayerPendingApplicationsUIClick(player, clickedSlot, clickedItem, event.isRightClick());
        }
        // Invitations UI
        else if (normalizedTitle.equals("YOUR INVITATIONS")) {
            event.setCancelled(true);
            handleInvitationsUIClick(player, clickedSlot, clickedItem, event.isLeftClick());
        }
        // Filter Results UI
        else if (normalizedTitle.equals("FILTER RESULTS")) {
            event.setCancelled(true);
            plugin.getFilterGUI().handleFilterClick(player, clickedSlot);
        }
        // Faction Application Confirmation Screen
        else if (strippedTitle.equals("Confirm Faction Application Changes")) {
            event.setCancelled(true);
            FactionApplication pendingApplication = plugin.getPendingFactionApplication(player.getUniqueId());
            if (pendingApplication == null) {
                player.sendMessage(ChatColor.RED + "Error: No pending faction application found.");
                player.closeInventory();
                return;
            }

            if (clickedSlot == 14) { // Confirm & Save
                plugin.getDatabaseManager().saveFactionApplication(pendingApplication);
                player.sendMessage(plugin.getConfigManager().getPrefix() + "Your faction application has been saved.");
                plugin.removePendingFactionApplication(player.getUniqueId());
                player.closeInventory();
            } else if (clickedSlot == 12) { // Cancel Changes
                plugin.removePendingFactionApplication(player.getUniqueId());
                player.sendMessage(plugin.getConfigManager().getPrefix() + "Faction application save cancelled.");
                player.closeInventory();
            }
        }
        // Manage Application UI - handle gradient titles
        else if (normalizedTitle.contains("FACTION MANAGER")) {
            event.setCancelled(true);
            handleManageApplicationUIClick(player, clickedSlot, clickedItem);
        }
        // Help & Commands UI
        else if (normalizedTitle.contains("HELP") && normalizedTitle.contains("COMMANDS")) {
            event.setCancelled(true);
            if (clickedSlot == 22) { // Back button
                recruitGUI.openMainMenu(player, plugin.getFilter());
            }
        }
        // Player Info Display
        else if (normalizedTitle.endsWith(" PROFILE")) {
            event.setCancelled(true);
            if (clickedSlot == 22) { // Lectern - Invite button
                if (clickedItem != null && clickedItem.getType() == Material.LECTERN) {
                    String targetPlayerName = normalizedTitle.substring(0, normalizedTitle.length() - " PROFILE".length());
                    Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

                    if (targetPlayer != null) {
                        String inviteCommand = plugin.getPapiIntegrationManager().getFactionInviteCommand(player, targetPlayer, null);
                        if (inviteCommand != null) {
                            // Show confirmation dialog before sending invitation
                            new ConfirmationGUI(plugin, player,
                                VisualUtils.createCompactServerTitle("Confirm Invitation"),
                                "Send faction invitation to " + targetPlayer.getName() + "?",
                                () -> {
                                    // Confirm action
                                    Bukkit.dispatchCommand(player, inviteCommand);
                                    player.sendMessage(plugin.getConfigManager().getPrefix() + "Invitation sent to " + targetPlayer.getName() + ".");
                                },
                                () -> {
                                    // Cancel action - reopen the player profile
                                    plugin.getPlayerInfoGUI().openPlayerInfoDisplay(player, targetPlayer);
                                }
                            ).open();
                        } else {
                            player.sendMessage(ChatColor.RED + "Could not send invitation. Are you in a faction?");
                            player.closeInventory();
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Error: Target player not found.");
                        player.closeInventory();
                    }
                }
            }
        }
    }

    private void handleMainMenuClick(Player player, int slot, ItemStack item) {
        switch (slot) {
            case 4: // Player Profile
                PlayerResume resume = plugin.getDatabaseManager().getPlayerResume(player.getUniqueId());
                if (resume == null) {
                    resume = new PlayerResume(player.getUniqueId());
                }
                resume.setLooking(!resume.isLooking());
                plugin.getDatabaseManager().savePlayerResume(resume);
                recruitGUI.openMainMenu(player, plugin.getFilter());
                break;
            case 45: // Previous Page
                if (plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE) {
                    int currentPage = plugin.getPlayerListPage();
                    if (currentPage > 1) {
                        plugin.setPlayerListPage(currentPage - 1);
                        recruitGUI.openMainMenu(player, plugin.getFilter());
                    }
                } else {
                    int currentPage = plugin.getFactionListPage();
                    if (currentPage > 1) {
                        plugin.setFactionListPage(currentPage - 1);
                        recruitGUI.openMainMenu(player, plugin.getFilter());
                    }
                }
                break;
            case 53: // Next Page
                if (plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE) {
                    int currentPage = plugin.getPlayerListPage();
                    int totalPlayers = plugin.getDatabaseManager().getLookingPlayers(plugin.getFilter()).size();
                    int maxPages = (int) Math.ceil((double) totalPlayers / 34);
                    if (currentPage < maxPages) {
                        plugin.setPlayerListPage(currentPage + 1);
                        recruitGUI.openMainMenu(player, plugin.getFilter());
                    }
                } else {
                    int currentPage = plugin.getFactionListPage();
                    int totalFactions = plugin.getDatabaseManager().getRecruitingFactions(plugin.getFilter()).size();
                    int maxPages = (int) Math.ceil((double) totalFactions / 34);
                    if (currentPage < maxPages) {
                        plugin.setFactionListPage(currentPage + 1);
                        recruitGUI.openMainMenu(player, plugin.getFilter());
                    }
                }
                break;
            case 46: // Manage Resume
                plugin.getManageResumeGUI().openManageResumeUI(player);
                break;
            case 47: // Manage Applications
                if (plugin.getPapiIntegrationManager().isInFaction(player) && plugin.getPapiIntegrationManager().hasRole(player, "admin")) {
                    //plugin.getFactionApplicationEditorGUI().openFactionApplicationEditor(player);
                    plugin.getManageApplicationGUI().openManageApplicationUI(player);
                }
                break;
            case 48: // Help & Commands
                plugin.getHelpGUI().openHelpUI(player);
                break;
            case 49: // Close
                player.closeInventory();
                break;
            case 50: // Search
                new AnvilGUI.Builder()
                    .onClick((s, stateSnapshot) -> {
                        if(s != AnvilGUI.Slot.OUTPUT) {
                            return Collections.emptyList();
                        }
                        
                        String text = stateSnapshot.getText();
                        text = text.trim();
                        if (text != null && !text.isEmpty()) {
                            if (plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE) {
                                if (text.length()<=16){
                                    List<PlayerResume> players = plugin.getDatabaseManager().searchPlayers(text);
                                    plugin.setSearchResultPlayers(players);
                                } else {
                                   player.sendMessage(ChatColor.RED + "The username can't be longer than 16 chars.");                                    
                                }
                            } else {
                                if (text.length()<=10){
                                List<FactionApplication> factions = plugin.getDatabaseManager().searchFactions(text);
                                plugin.setSearchResultFactions(factions);
                                } else {
                                   player.sendMessage(ChatColor.RED + "The faction tag can't be longer than 10 chars.");
                                }
                            }
                            Bukkit.getScheduler().runTask(plugin, () -> plugin.getRecruitGUI().openMainMenu(stateSnapshot.getPlayer(), null));
                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        } else {
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Please enter a search query."));
                        }
                        
                    })
                    .text(" ")
                    .itemLeft(new ItemStack(Material.PAPER))
                    .title("Search")
                    .plugin(plugin)
                    .open(player);
                break;
            case 51: // Toggle View
                plugin.setMainMenuDisplayMode(plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE ? FactionsRecruit.MainMenuDisplayMode.FACTION_MODE : FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE);
                recruitGUI.openMainMenu(player, plugin.getFilter());
                break;
            case 52: // Filter Results
                plugin.getFilterGUI().openFilterUI(player);
                break;
            default:
                if (slot >= 10 && slot <= 43) {
                    if (plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE) {
                        // Handle player click
                        if (item.getType() == Material.PLAYER_HEAD) {
                            String playerName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                            Player targetPlayer = Bukkit.getPlayer(playerName);
                            if (targetPlayer != null) {
                                plugin.getPlayerInfoGUI().openPlayerInfoDisplay(player, targetPlayer);
                            }
                        }
                    } else {
                        if (item.getType() == Material.YELLOW_BANNER) {
                            List<String> lore = item.getItemMeta().getLore();
                            if (lore != null && lore.size() > 4) {
                                // Faction ID is on line 4 (0-indexed) - the hidden line with faction ID
                                String factionId = ChatColor.stripColor(lore.get(4)).trim();

                                // Debug logging
                                if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                                    plugin.getLogger().info("[DEBUG] Banner clicked - Extracted faction ID: '" + factionId + "'");
                                    plugin.getLogger().info("[DEBUG] Banner lore size: " + lore.size());
                                    for (int i = 0; i < lore.size(); i++) {
                                        plugin.getLogger().info("[DEBUG] Lore[" + i + "]: '" + ChatColor.stripColor(lore.get(i)) + "'");
                                    }
                                }

                                FactionApplication factionApplication = plugin.getDatabaseManager().getFactionApplication(factionId);

                                // Fallback: if not found by ID, try to find by matching against displayed faction name
                                if (factionApplication == null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                                    String displayedFactionName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                                    if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                                        plugin.getLogger().info("[DEBUG] Fallback: Searching for faction by name: '" + displayedFactionName + "'");
                                    }

                                    // Search through all recruiting factions to find one with matching name
                                    List<FactionApplication> allFactions = plugin.getDatabaseManager().getRecruitingFactions(null);
                                    for (FactionApplication app : allFactions) {
                                        if (app.getFactionName().equalsIgnoreCase(displayedFactionName)) {
                                            factionApplication = app;
                                            if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                                                plugin.getLogger().info("[DEBUG] Fallback: Found faction by name: " + app.getFactionName() + " (ID: " + app.getFactionId() + ")");
                                            }
                                            break;
                                        }
                                    }
                                }

                                final FactionApplication finalFactionApplication = factionApplication;
                                if (finalFactionApplication != null) {
                                    new ConfirmationGUI(plugin, player,
                                        VisualUtils.createCompactServerTitle("Apply to Faction"),
                                        "Apply to " + finalFactionApplication.getFactionName() + "?",
                                        () -> plugin.sendApplication(player, finalFactionApplication),
                                        () -> recruitGUI.openMainMenu(player, plugin.getFilter())
                                    ).open();
                                } else {
                                    player.sendMessage(ChatColor.RED + "Error: Faction application not found for ID: " + factionId);
                                    if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                                        plugin.getLogger().warning("[DEBUG] Could not find faction application for ID: '" + factionId + "'");
                                    }
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Error: Invalid faction banner data.");
                            }
                        }
                    }
                }
                break;
        }
    }

    private void handleSelectionClick(Player player, Inventory currentInventory, ItemStack clickedItem, int clickedSlot, boolean isRadio, int skillLimit) {
        // Handle Timezone selection
        if (clickedSlot >= 11 && clickedSlot <= 16) {
            if (isRadio) { // Radio - Single Choice
                for (int i = 11; i <= 16; i++) {
                    ItemStack item = currentInventory.getItem(i);
                    if (item != null && item.hasItemMeta()) {
                        item.setType(i == clickedSlot ? Material.GREEN_CANDLE : Material.GRAY_CANDLE);
                        recruitGUI.updateGuiItemEnchantment(item, i == clickedSlot);
                    }
                }
            } else { // Checkbox - Multiple Choice
                boolean currentlyEnchanted = clickedItem.getItemMeta().hasEnchant(Enchantment.UNBREAKING);
                clickedItem.setType(!currentlyEnchanted ? Material.GREEN_CANDLE : Material.GRAY_CANDLE);
                recruitGUI.updateGuiItemEnchantment(clickedItem, !currentlyEnchanted);
            }
            player.updateInventory();
            player.sendMessage(ChatColor.YELLOW + "Timezone toggled: " + clickedItem.getItemMeta().getDisplayName());
        }
        // Handle Experience selection
        else if (clickedSlot >= 20 && clickedSlot <= 26) {
            if (isRadio) { // Radio - Single Choice
                for (int i = 20; i <= 26; i++) {
                    ItemStack item = currentInventory.getItem(i);
                    if (item != null && item.hasItemMeta()) {
                        item.setType(i == clickedSlot ? Material.GREEN_CANDLE : Material.GRAY_CANDLE);
                        recruitGUI.updateGuiItemEnchantment(item, i == clickedSlot);
                    }
                }
            } else { // Checkbox - Multiple Choice
                boolean currentlyEnchanted = clickedItem.getItemMeta().hasEnchant(Enchantment.UNBREAKING);
                clickedItem.setType(!currentlyEnchanted ? Material.GREEN_CANDLE : Material.GRAY_CANDLE);
                recruitGUI.updateGuiItemEnchantment(clickedItem, !currentlyEnchanted);
            }
            player.updateInventory();
            player.sendMessage(ChatColor.YELLOW + "Experience toggled: " + clickedItem.getItemMeta().getDisplayName());
        }
        // Handle Available Days selection (Checkbox - Multiple Choice for both)
        else if (clickedSlot >= 29 && clickedSlot <= 35) {
            boolean currentlyEnchanted = clickedItem.getItemMeta().hasEnchant(Enchantment.UNBREAKING);
            clickedItem.setType(!currentlyEnchanted ? Material.GREEN_CANDLE : Material.RED_CANDLE);
            recruitGUI.updateGuiItemEnchantment(clickedItem, !currentlyEnchanted);
            player.updateInventory();
            player.sendMessage(ChatColor.YELLOW + "Day toggled: " + clickedItem.getItemMeta().getDisplayName());
        }
        // Handle Skills selection
        else if (clickedSlot >= 38 && clickedSlot <= 44) {
            boolean currentlyEnchanted = clickedItem.getItemMeta().hasEnchant(Enchantment.UNBREAKING);
            if (currentlyEnchanted) {
                recruitGUI.updateGuiItemEnchantment(clickedItem, false);
                player.updateInventory();
                player.sendMessage(ChatColor.YELLOW + "Skill un-toggled: " + clickedItem.getItemMeta().getDisplayName());
            } else {
                int selectedSkills = 0;
                for (int i = 38; i <= 44; i++) {
                    ItemStack skillItem = currentInventory.getItem(i);
                    if (skillItem != null && skillItem.hasItemMeta() && skillItem.getItemMeta().hasEnchant(Enchantment.UNBREAKING)) {
                        selectedSkills++;
                    }
                }

                if (selectedSkills < skillLimit) {
                    recruitGUI.updateGuiItemEnchantment(clickedItem, true);
                    player.updateInventory();
                    player.sendMessage(ChatColor.YELLOW + "Skill toggled: " + clickedItem.getItemMeta().getDisplayName());
                } else {
                    player.sendMessage(ChatColor.RED + "You can select a maximum of " + skillLimit + " skills.");
                }
            }
        }
    }

    private void handleFilterUIClick(Player player, Inventory currentInventory, ItemStack clickedItem, int clickedSlot) {
        handleSelectionClick(player, currentInventory, clickedItem, clickedSlot, false, 7); // All checkboxes, no skill limit

        if (clickedItem.getType() == Material.BARRIER && clickedItem.hasItemMeta() && clickedItem.getItemMeta().getDisplayName().equals( "§cReset Filters")) {
            plugin.getFilterGUI().openFilterUI(player); // Just reopen to reset
            player.sendMessage( "§eFilters have been reset.");
        } else if (clickedItem.getType() == Material.EMERALD && clickedItem.hasItemMeta() && clickedItem.getItemMeta().getDisplayName().equals( "§aApply Filters")) {
            FactionApplication filter = extractFilterData(currentInventory);
            plugin.setFilter(filter);
            player.sendMessage( "§aFilters applied!");
            recruitGUI.openMainMenu(player, filter);
        }
    }

    private void handleManageResumeUIClick(Player player, int slot, ItemStack item) {
        switch (slot) {
            case 11: // Lectern - Edit resume
                plugin.getResumeEditorGUI().openResumeEditor(player);
                break;
            case 15: // Emerald/Redstone - Toggle "looking for faction" status
                PlayerResume resume = plugin.getDatabaseManager().getPlayerResume(player.getUniqueId());
                if (resume == null) {
                    resume = new PlayerResume(player.getUniqueId());
                }
                resume.setLooking(!resume.isLooking());
                plugin.getDatabaseManager().savePlayerResume(resume);
                plugin.getManageResumeGUI().openManageResumeUI(player);
                break;
            case 22: // Bookshelf - View pending applications/invitations
                plugin.getPendingApplicationsGUI().openPendingApplicationsUI(player);
                break;
        }
    }
    private void handleManageApplicationUIClick(Player player, int slot, ItemStack item) {
        switch (slot) {
            case 11: // Lectern - Edit resume
                plugin.getFactionApplicationEditorGUI().openFactionApplicationEditor(player);
                break;
            case 15: // Emerald/Redstone - Toggle "looking for recruits" status
                String factionId = plugin.getPapiIntegrationManager().getFactionInternalId(player);
                FactionApplication factionApplication = plugin.getDatabaseManager().getFactionApplication(factionId);

                if (factionApplication == null) {
                    // Create a new FactionApplication if none exists
                    factionApplication = new FactionApplication(
                        factionId,
                        plugin.getPapiIntegrationManager().getFactionName(player),
                        player.getUniqueId(),
                        new ArrayList<>(), // Empty desiredTimezones
                        new ArrayList<>(), // Empty experienceLevels
                        new ArrayList<>(), // Empty requiredDays
                        new ArrayList<>(), // Empty desiredSkills
                        true, // Default to accepting
                        System.currentTimeMillis(),
                        System.currentTimeMillis()
                    );
                }

                factionApplication.setAccepting(!factionApplication.isAccepting());
                factionApplication.setUpdatedAt(System.currentTimeMillis());
                plugin.getDatabaseManager().saveFactionApplication(factionApplication);
                plugin.getManageApplicationGUI().openManageApplicationUI(player);
                player.sendMessage(plugin.getConfigManager().getPrefix() + (factionApplication.isAccepting() ? "Your faction is now accepting applications." : "Your faction is no longer accepting applications."));
                break;
            case 22: // Bookshelf - View pending applications
                plugin.getPendingApplicationsGUI().openPendingApplicationsUI(player);
                break;
        }
    }

    private void handleFactionOwnerPendingApplicationsUIClick(Player player, int slot, ItemStack item) {
        if (slot >= 10 && slot <= 43) { // Clicks on player heads
            if (item.getType() == Material.PLAYER_HEAD) {
                String playerName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                Player targetPlayer = Bukkit.getPlayer(playerName);
                if (targetPlayer != null) {
                    plugin.getPlayerInfoGUI().openPlayerInfoDisplay(player, targetPlayer);
                }
            }
        }
    }

    private void handlePlayerPendingApplicationsUIClick(Player player, int slot, ItemStack item, boolean isRightClick) {
        if (slot >= 12 && slot <= 14) { // Clicks on application slots
            if (item.getType() == Material.YELLOW_BANNER) {
                int applicationId = Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(3)));
                PlayerApplication application = plugin.getDatabaseManager().getPlayerApplication(applicationId);
                if (application != null) {
                    if (isRightClick) {
                        // Cancel application (only after 24 hours)
                        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
                        if (application.getApplicationDate() < twentyFourHoursAgo) {
                            application.setStatus("CANCELLED");
                            plugin.getDatabaseManager().savePlayerApplication(application);
                            player.sendMessage( plugin.getConfigManager().getApplicationCancelledMessage().replace("%faction%", plugin.getPapiIntegrationManager().getFactionName(Bukkit.getOfflinePlayer(application.getPlayerUuid()))));
                            plugin.getPendingApplicationsGUI().openPendingApplicationsUI(player);
                        } else {
                            player.sendMessage( ChatColor.RED + "You can only cancel applications after 24 hours.");
                        }
                    } else {
                        // Left-click: View application details (if applicable)
                        // For now, just send a message
                        player.sendMessage( ChatColor.YELLOW + "Viewing application details for " + plugin.getPapiIntegrationManager().getFactionName(Bukkit.getOfflinePlayer(application.getPlayerUuid())) + ".");
                    }
                }
            }
        } else if (slot == 22) { // Back Button
            player.closeInventory();
            plugin.getManageResumeGUI().openManageResumeUI(player);
        }
    }

    

    private void handleEditorUIClick(Player player, Inventory currentInventory, ItemStack clickedItem, int clickedSlot,
            boolean isResumeUI) {
        // Handle Toggle Resume Visibility button
        if (clickedSlot == 48 && (clickedItem.getType() == Material.RED_WOOL || clickedItem.getType() == Material.LIME_WOOL)) {
            PlayerResume resume = plugin.getDatabaseManager().getPlayerResume(player.getUniqueId());
            if (resume == null) {
                resume = new PlayerResume(player.getUniqueId());
            }
            resume.setHidden(!resume.isHidden()); // Toggle the hidden status
            plugin.getDatabaseManager().savePlayerResume(resume); // Save to database
            plugin.getResumeEditorGUI().openResumeEditor(player); // Re-open GUI to refresh
            player.sendMessage( ChatColor.GREEN + "Your resume visibility has been toggled to " + (resume.isHidden() ? "Hidden" : "Visible") + ".");
        }
        // Handle Save & Continue buttons
        else if (clickedItem.getType() == Material.EMERALD && clickedItem.hasItemMeta()) {
            if (ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).equals("Save & Continue")) {
                if (isResumeUI) {
                    if (checkCooldown(player, true)) {
                        PlayerResume resume = extractResumeData(player, currentInventory);
                        if (resume != null) {
                            new ConfirmationGUI(plugin, player, plugin.getConfigManager().getGuiString("resume-confirmation.title", "Confirm Resume Changes"), plugin.getConfigManager().getGuiString("resume-confirmation.message", "Review your new resume details."), resume,
                                () -> {
                                    // onConfirm
                                    plugin.getDatabaseManager().savePlayerResume(resume);
                                    player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getGuiString("resume-confirmation.save-success", "Your resume has been saved."));
                                    plugin.removePendingResume(player.getUniqueId());
                                    recruitGUI.openMainMenu(player, null); // Or close inventory, whatever is appropriate
                                },
                                () -> {
                                    // onCancel
                                    player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getGuiString("resume-confirmation.save-cancelled", "Resume save cancelled."));
                                    plugin.getResumeEditorGUI().openResumeEditor(player); // Re-open editor
                                }
                            ).open();
                        }
                    }
                } else {
                    if (checkCooldown(player, false)) {
                        FactionApplication application = extractFactionApplicationData(player, currentInventory);
                        new ConfirmationGUI(plugin, player, plugin.getConfigManager().getGuiString("faction-application-confirmation.title", "&6Confirm Faction Application Changes"), "",
                            () -> {
                                plugin.getDatabaseManager().saveFactionApplication(application);
                                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getGuiString("faction-application-editor.messages.requirements-saved", "&aYour faction requirements have been saved!"));
                                player.closeInventory();
                            },
                            () -> plugin.getFactionApplicationEditorGUI().openFactionApplicationEditor(player)
                        ).open();
                    }
                }
            }
        }
        // Handle selection clicks for timezone, experience, days, skills
        else { // Only call handleSelectionClick if it's not one of the specific buttons
            handleSelectionClick(player, currentInventory, clickedItem, clickedSlot, isResumeUI, 3);
        }
    }

    private PlayerResume extractResumeData(Player player, Inventory inventory) {
        String timezone = null;
        String experience = null;
        List<String> availableDays = new ArrayList<>();
        List<String> skills = new ArrayList<>();

        // Extract Timezone (Slot 11-16)
        for (int i = 11; i <= 16; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                timezone = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                break;
            }
        }

        // Extract Experience (Slot 20-26)
        for (int i = 20; i <= 26; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                experience = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                break;
            }
        }

        // Extract Available Days (Slot 29-35)
        for (int i = 29; i <= 35; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                availableDays.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Extract Skills (Slot 38-44)
        for (int i = 38; i <= 44; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getItemMeta() != null && item.getItemMeta().hasEnchant(Enchantment.UNBREAKING)) {
                skills.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Validation
        if (timezone == null) {
            player.sendMessage( ChatColor.RED + "Please select a timezone.");
            return null;
        }
        if (experience == null) {
            player.sendMessage( ChatColor.RED + "Please select an experience level.");
            return null;
        }
        if (availableDays.isEmpty()) {
            player.sendMessage( ChatColor.RED + "Please select at least one available day.");
            return null;
        }
        if (skills.isEmpty() || skills.size() > 3) {
            player.sendMessage( ChatColor.RED + "Please select between 1 and 3 skills.");
            return null;
        }

        long currentTime = System.currentTimeMillis();
        // Retrieve existing resume to preserve isHidden and isLooking status
        PlayerResume existingResume = plugin.getDatabaseManager().getPlayerResume(player.getUniqueId());
        boolean isHidden = (existingResume != null) ? existingResume.isHidden() : false; // Default to false if no existing resume
        boolean isLooking = (existingResume != null) ? existingResume.isLooking() : false; // Preserve existing isLooking status

        return new PlayerResume(player.getUniqueId(), timezone, experience, new HashSet<>(availableDays),
                new HashSet<>(skills), isLooking, 0, currentTime, currentTime, isHidden); // Pass isHidden
    }

    private FactionApplication extractFilterData(Inventory inventory) {
        List<String> timezones = new ArrayList<>();
        List<String> experience = new ArrayList<>();
        List<String> availableDays = new ArrayList<>();
        List<String> skills = new ArrayList<>();

        // Extract Timezones (Slot 11-16)
        for (int i = 11; i <= 16; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                timezones.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Extract Experience (Slot 20-26)
        for (int i = 20; i <= 26; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                experience.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Extract Available Days (Slot 29-35)
        for (int i = 29; i <= 35; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                availableDays.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Extract Skills (Slot 38-44)
        for (int i = 38; i <= 44; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getItemMeta() != null && item.getItemMeta().hasEnchant(Enchantment.UNBREAKING)) {
                skills.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        return new FactionApplication("", "", null, timezones, experience, availableDays, skills, false, 0, 0);
    }

        private FactionApplication extractFactionApplicationData(Player player, Inventory inventory) {
        List<String> desiredTimezones = new ArrayList<>();
        List<String> experienceLevels = new ArrayList<>();
        List<String> requiredDays = new ArrayList<>();
        List<String> desiredSkills = new ArrayList<>();

        // Extract Desired Timezones (Slot 11-16)
        for (int i = 11; i <= 16; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                desiredTimezones.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Extract Experience Levels (Slot 20-26)
        for (int i = 20; i <= 26; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                experienceLevels.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Extract Required Days (Slot 29-35)
        for (int i = 29; i <= 35; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.GREEN_CANDLE) {
                requiredDays.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        // Extract Desired Skills (Slot 38-44)
        for (int i = 38; i <= 44; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getItemMeta() != null && item.getItemMeta().hasEnchant(Enchantment.UNBREAKING)) {
                desiredSkills.add(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            }
        }

        String factionId = plugin.getPapiIntegrationManager().getFactionInternalId(player);
        String factionName = plugin.getPapiIntegrationManager().getFactionName(player);
        long currentTime = System.currentTimeMillis();

        return new FactionApplication(factionId, factionName, player.getUniqueId(), desiredTimezones, experienceLevels, requiredDays, desiredSkills, true, currentTime, currentTime);
    }

    private boolean checkCooldown(Player player, boolean isResumeCooldown) {
        long currentTime = System.currentTimeMillis();
        long cooldownHours = isResumeCooldown ? plugin.getConfigManager().getResumeEditCooldownHours() : plugin.getConfigManager().getApplicationCooldownHours();
        long cooldownMillis = cooldownHours * 60 * 60 * 1000;

        if (isResumeCooldown) {
            PlayerResume resume = plugin.getDatabaseManager().getPlayerResume(player.getUniqueId());
            if (resume != null && (currentTime - resume.getLastUpdated()) < cooldownMillis) {
                long remainingTime = cooldownMillis - (currentTime - resume.getLastUpdated());
                player.sendMessage( ChatColor.RED + "You must wait " + formatTime(remainingTime) + " before editing your resume again.");
                return false;
            }
        } else {
            FactionApplication factionApp = plugin.getDatabaseManager().getFactionApplication(plugin.getPapiIntegrationManager().getFactionInternalId(player));
            if (factionApp != null && (currentTime - factionApp.getUpdatedAt()) < cooldownMillis) {
                long remainingTime = cooldownMillis - (currentTime - factionApp.getUpdatedAt());
                player.sendMessage( ChatColor.RED + "You must wait " + formatTime(remainingTime) + " before editing your faction application again.");
                return false;
            }
        }
        return true;
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = minutes / 1440;

        if (days > 0) {
            return days + " days " + (hours % 24) + " hours";
        } else if (hours > 0) {
            return hours + " hours " + (minutes % 60) + " minutes";
        } else if (minutes > 0) {
            return minutes + " minutes " + (seconds % 60) + " seconds";
        } else {
            return seconds + " seconds";
        }
    }

    private void handleInvitationsUIClick(Player player, int clickedSlot, ItemStack clickedItem, boolean isLeftClick) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Check if the clicked item is an invitation
        if (clickedItem.getType() == Material.PAPER && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore()) {
            List<String> lore = clickedItem.getItemMeta().getLore();

            // Find invitation ID in the lore (stored as "§8ID: <id>")
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    try {
                        int invitationId = Integer.parseInt(line.replace("§8ID: ", ""));

                        // Handle click - left click to accept, right click to reject
                        plugin.getInvitationsGUI().handleInvitationClick(player, invitationId, isLeftClick);
                        return;
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cError: Invalid invitation ID!");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up any active GUI handlers when player disconnects
        // This prevents memory leaks and ensures clean state
        plugin.getGuiManager().removeActiveGUI(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Clean up GUI handlers when inventory is closed (ESC/E key)
        // This fixes the issue where closing confirmation GUIs with ESC/E
        // leaves the click handler active, causing "Please click green or red buttons" message
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();

            // Check if this was a confirmation GUI or other managed GUI
            if (plugin.getGuiManager().hasActiveGUI(player.getUniqueId())) {
                String inventoryTitle = event.getView().getTitle();
                String normalizedTitle = normalizeServerTitle(inventoryTitle);

                // Only clean up for confirmation dialogs and other managed GUIs
                // Don't interfere with SelectionEditorTemplate or other special inventories
                if (normalizedTitle != null && (
                    normalizedTitle.contains("CONFIRM") ||
                    normalizedTitle.contains("CONFIRMATION") ||
                    event.getInventory().getSize() == 27 // Confirmation GUIs are 27 slots
                )) {
                    plugin.getGuiManager().removeActiveGUI(player.getUniqueId());
                }
            }
        }
    }
}