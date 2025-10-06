package com.gnut.factionsrecruit.interfaces;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.interfaces.SelectionEditorTemplate.Category;
import com.gnut.factionsrecruit.interfaces.SelectionEditorTemplate.SelectionEditorConfig;
import com.gnut.factionsrecruit.interfaces.SelectionEditorTemplate.SelectionMode;
import com.gnut.factionsrecruit.manager.DatabaseManager;
import com.gnut.factionsrecruit.manager.PAPIIntegrationManager;
import com.gnut.factionsrecruit.model.FactionApplication;
import com.gnut.factionsrecruit.util.VisualUtils;

import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FactionApplicationEditorGUI {

    private final FactionsRecruit plugin;
    private final DatabaseManager databaseManager;
    private final PAPIIntegrationManager papiIntegrationManager;

    public FactionApplicationEditorGUI(FactionsRecruit plugin, DatabaseManager databaseManager, PAPIIntegrationManager papiIntegrationManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.papiIntegrationManager = papiIntegrationManager;
    }

    public void openFactionApplicationEditor(Player player) {
        String factionId = papiIntegrationManager.getFactionInternalId(player);
        if (factionId == null || factionId.equalsIgnoreCase("none")) {
            player.sendMessage(plugin.getConfigManager().getGuiString("faction-application-editor.messages.not-in-faction", "&cYou must be in a faction to manage application requirements."));
            player.closeInventory();
            return;
        }

        // Check if player is faction leader
        String leaderName = papiIntegrationManager.getFactionLeader(player);
        UUID leaderUuid = null;
        if (leaderName != null && !leaderName.equalsIgnoreCase("none")) {
            OfflinePlayer offlineLeader = Bukkit.getOfflinePlayer(leaderName);
            if (offlineLeader != null) {
                leaderUuid = offlineLeader.getUniqueId();
            }
        }

        if (leaderUuid == null || !leaderUuid.equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getGuiString("faction-application-editor.messages.not-leader", "&cOnly faction leaders can manage application requirements."));
            player.closeInventory();
            return;
        }

        FactionApplication existingApplication = databaseManager.getFactionApplication(factionId);
        if (existingApplication == null) {
            String factionName = papiIntegrationManager.getFactionName(player);
            existingApplication = new FactionApplication(factionId, factionName, player.getUniqueId(), Arrays.asList(), Arrays.asList(), Arrays.asList(), Arrays.asList(), true, System.currentTimeMillis(), System.currentTimeMillis());
        }
        final FactionApplication applicationToUpdate = existingApplication;

        // Create server-themed central display item
        Material centralDisplayMaterial = plugin.getConfigManager().getMaterial("faction-application-editor.materials.central-display", Material.YELLOW_BANNER);

        List<String> instructions = Arrays.asList(
            "Define your faction's recruitment standards",
            "Players will see these requirements when applying"
        );
        List<String> factionInfo = Arrays.asList(
            VisualUtils.Symbols.CROWN + " Faction: " + applicationToUpdate.getFactionName()
        );

        List<String> centralLore = VisualUtils.createServerLoreSection("Instructions", instructions);
        centralLore.addAll(VisualUtils.createServerLoreSection("Faction Info", factionInfo));
        centralLore.add(VisualUtils.createActionLore("Configure requirements below", VisualUtils.ColorPalette.FACTION_ACCENT));

        String titleText = applicationToUpdate.getFactionName() + " Requirements";
        ItemStack centralDisplayItem = VisualUtils.createServerItem(
            centralDisplayMaterial,
            titleText,
            centralLore,
            true
        );

        // Create enhanced save button
        Material saveButtonMaterial = plugin.getConfigManager().getMaterial("faction-application-editor.materials.save-button", Material.GREEN_WOOL);

        List<String> saveLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Save your faction's recruitment",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "requirements to the database",
            "",
            VisualUtils.createServerStatusIndicator("Requirements will be updated", true),
            "",
            VisualUtils.createActionLore("Click to save", VisualUtils.ColorPalette.SUCCESS)
        );

        ItemStack saveButton = VisualUtils.createServerItem(
            saveButtonMaterial,
            "Save Requirements",
            saveLore,
            true
        );

        // Create enhanced continue button
        Material continueButtonMaterial = plugin.getConfigManager().getMaterial("faction-application-editor.materials.save-and-view-button", Material.BLUE_WOOL);

        List<String> continueLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Save requirements and proceed",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "to review pending applications",
            "",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.INFO) + VisualUtils.Symbols.ARROW_RIGHT + " Requirements will be saved",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.INFO) + VisualUtils.Symbols.ARROW_RIGHT + " Applications screen will open",
            "",
            VisualUtils.createActionLore("Click to save & continue", VisualUtils.ColorPalette.INFO)
        );

        ItemStack continueButton = VisualUtils.createServerItem(
            continueButtonMaterial,
            "Save & View Applications",
            continueLore,
            true
        );

        // Configure the template with compact server-themed title
        String configTitle = VisualUtils.createCompactServerTitle("Requirements Editor");
        SelectionEditorConfig config = new SelectionEditorConfig(
                configTitle,
                centralDisplayItem,
                plugin.getConfigManager()
        )
                .withSelectionMode(Category.TIMEZONE, SelectionMode.CHECKBOX)
                .withSelectionMode(Category.EXPERIENCE, SelectionMode.CHECKBOX)
                .withSelectionMode(Category.DAYS, SelectionMode.CHECKBOX)
                .withSelectionMode(Category.SKILLS, SelectionMode.CHECKBOX);

        final SelectionEditorTemplate editor = new SelectionEditorTemplate(player, config, plugin.getConfigManager());

        config.withActionButton(49, saveButton, () -> {
            Map<Category, Set<String>> selections = editor.getPlayerSelections();

            // Convert and validate display names to database keys before saving
            List<String> timezoneKeys = new ArrayList<>();
            for (String timezone : selections.get(Category.TIMEZONE)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(timezone);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "timezone")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid timezone selection: " + timezone);
                    plugin.getLogger().severe("Database key conversion failed for timezone: " + timezone + " -> " + dbKey);
                    return;
                }
                timezoneKeys.add(dbKey);
            }

            List<String> experienceKeys = new ArrayList<>();
            for (String experience : selections.get(Category.EXPERIENCE)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(experience);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "experience")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid experience selection: " + experience);
                    plugin.getLogger().severe("Database key conversion failed for experience: " + experience + " -> " + dbKey);
                    return;
                }
                experienceKeys.add(dbKey);
            }

            List<String> dayKeys = new ArrayList<>();
            for (String day : selections.get(Category.DAYS)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(day);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "days")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid day selection: " + day);
                    plugin.getLogger().severe("Database key conversion failed for day: " + day + " -> " + dbKey);
                    return;
                }
                dayKeys.add(dbKey);
            }

            List<String> skillKeys = new ArrayList<>();
            for (String skill : selections.get(Category.SKILLS)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(skill);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "skills")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid skill selection: " + skill);
                    plugin.getLogger().severe("Database key conversion failed for skill: " + skill + " -> " + dbKey);
                    return;
                }
                skillKeys.add(dbKey);
            }

            applicationToUpdate.setDesiredTimezones(timezoneKeys);
            applicationToUpdate.setExperienceLevels(experienceKeys);
            applicationToUpdate.setRequiredDays(dayKeys);
            applicationToUpdate.setDesiredSkills(skillKeys);
            applicationToUpdate.setUpdatedAt(System.currentTimeMillis());

            // Save to database
            databaseManager.saveFactionApplication(applicationToUpdate);
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.SUCCESS) +
                VisualUtils.Symbols.CHECK + " Your faction requirements have been saved!");
            player.closeInventory();
        });

        config.withActionButton(50, continueButton, () -> {
            Map<Category, Set<String>> selections = editor.getPlayerSelections();

            // Convert and validate display names to database keys before saving
            List<String> timezoneKeys = new ArrayList<>();
            for (String timezone : selections.get(Category.TIMEZONE)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(timezone);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "timezone")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid timezone selection: " + timezone);
                    plugin.getLogger().severe("Database key conversion failed for timezone: " + timezone + " -> " + dbKey);
                    return;
                }
                timezoneKeys.add(dbKey);
            }

            List<String> experienceKeys = new ArrayList<>();
            for (String experience : selections.get(Category.EXPERIENCE)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(experience);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "experience")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid experience selection: " + experience);
                    plugin.getLogger().severe("Database key conversion failed for experience: " + experience + " -> " + dbKey);
                    return;
                }
                experienceKeys.add(dbKey);
            }

            List<String> dayKeys = new ArrayList<>();
            for (String day : selections.get(Category.DAYS)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(day);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "days")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid day selection: " + day);
                    plugin.getLogger().severe("Database key conversion failed for day: " + day + " -> " + dbKey);
                    return;
                }
                dayKeys.add(dbKey);
            }

            List<String> skillKeys = new ArrayList<>();
            for (String skill : selections.get(Category.SKILLS)) {
                String dbKey = plugin.getConfigManager().getDatabaseKey(skill);
                if (dbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dbKey, "skills")) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                        VisualUtils.Symbols.CROSS + " Invalid skill selection: " + skill);
                    plugin.getLogger().severe("Database key conversion failed for skill: " + skill + " -> " + dbKey);
                    return;
                }
                skillKeys.add(dbKey);
            }

            applicationToUpdate.setDesiredTimezones(timezoneKeys);
            applicationToUpdate.setExperienceLevels(experienceKeys);
            applicationToUpdate.setRequiredDays(dayKeys);
            applicationToUpdate.setDesiredSkills(skillKeys);
            applicationToUpdate.setUpdatedAt(System.currentTimeMillis());

            // Save to database
            databaseManager.saveFactionApplication(applicationToUpdate);
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.SUCCESS) +
                VisualUtils.Symbols.CHECK + " Requirements saved! Opening applications...");
            plugin.getFactionApplicationReviewGUI().openFactionApplicationReviewUI(player);
        });

        // Initialize selections in the template from the existing application
        // Convert database keys to display names for UI display
        Map<Category, Set<String>> initialSelections = new EnumMap<>(Category.class);
        initialSelections.put(Category.TIMEZONE, new HashSet<>(plugin.getConfigManager().getDisplayNames(applicationToUpdate.getDesiredTimezones())));
        initialSelections.put(Category.EXPERIENCE, new HashSet<>(plugin.getConfigManager().getDisplayNames(applicationToUpdate.getExperienceLevels())));
        initialSelections.put(Category.DAYS, new HashSet<>(plugin.getConfigManager().getDisplayNames(applicationToUpdate.getRequiredDays())));
        initialSelections.put(Category.SKILLS, new HashSet<>(plugin.getConfigManager().getDisplayNames(applicationToUpdate.getDesiredSkills())));

        // Create and open the GUI
        editor.setPlayerSelections(initialSelections);
        editor.drawGUI();
        player.openInventory(editor.getInventory());

        // Register the GUI as an event listener
        Bukkit.getPluginManager().registerEvents(editor, plugin);
    }
}
