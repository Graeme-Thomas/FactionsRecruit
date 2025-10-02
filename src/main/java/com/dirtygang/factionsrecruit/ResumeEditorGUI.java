package com.dirtygang.factionsrecruit;

import com.dirtygang.factionsrecruit.SelectionEditorTemplate.Category;
import com.dirtygang.factionsrecruit.SelectionEditorTemplate.SelectionEditorConfig;
import com.dirtygang.factionsrecruit.SelectionEditorTemplate.SelectionMode;
import com.dirtygang.factionsrecruit.SelectionEditorTemplate.ValidationResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.dirtygang.factionsrecruit.util.ItemBuilder; // Import ItemBuilder
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ResumeEditorGUI {

    private final FactionsRecruit plugin;
    private final DatabaseManager databaseManager;

    public ResumeEditorGUI(FactionsRecruit plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void openResumeEditor(Player player) {
        PlayerResume existingResume = databaseManager.getPlayerResume(player.getUniqueId());
        if (existingResume == null) {
            existingResume = new PlayerResume(player.getUniqueId());
        }
        final PlayerResume resumeToUpdate = existingResume;

        // Enhanced Toggle Visibility Button
        Material visibilityMaterial = resumeToUpdate.isHidden() ?
            plugin.getConfigManager().getMaterial("resume-editor.materials.visibility-toggle-off", Material.REDSTONE_BLOCK) :
            plugin.getConfigManager().getMaterial("resume-editor.materials.visibility-toggle-on", Material.EMERALD_BLOCK);

        boolean isVisible = !resumeToUpdate.isHidden();
        List<String> visibilityLore = Arrays.asList(
            VisualUtils.createServerStatusIndicator("Resume Visibility: " + (isVisible ? "Public" : "Hidden"), isVisible),
            "",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                (isVisible ? "Factions can see and invite you" : "Your resume is hidden from factions"),
            "",
            VisualUtils.createActionLore("Click to toggle", isVisible ? VisualUtils.ColorPalette.ERROR : VisualUtils.ColorPalette.SUCCESS)
        );

        ItemStack visibilityToggleButton = VisualUtils.createServerItem(
            visibilityMaterial,
            "Toggle Resume Visibility",
            visibilityLore,
            isVisible
        );

        // Create server-themed central display item (player head)
        List<String> tips = Arrays.asList(
            "Customize your recruitment profile",
            "to attract the right faction"
        );
        List<String> features = Arrays.asList(
            VisualUtils.Symbols.STAR + " Showcase your experience and skills",
            VisualUtils.Symbols.HOURGLASS + " Set your availability preferences"
        );

        List<String> playerHeadLore = VisualUtils.createServerLoreSection("Tips", tips);
        playerHeadLore.addAll(VisualUtils.createServerLoreSection("Features", features));
        playerHeadLore.add(VisualUtils.createActionLore("Configure sections below", VisualUtils.ColorPalette.FACTION_ACCENT));

        ItemStack playerHead = VisualUtils.createServerItem(
            Material.PLAYER_HEAD,
            player.getName() + "'s Resume",
            playerHeadLore,
            true
        );
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            playerHead.setItemMeta(skullMeta);
        }

        // Enhanced Save Button
        Material saveButtonMaterial = plugin.getConfigManager().getMaterial("resume-editor.materials.save-button", Material.LIME_WOOL);

        List<String> saveLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Save your resume and proceed",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "to the confirmation screen",
            "",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.INFO) + VisualUtils.Symbols.ARROW_RIGHT + " Review your changes",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.INFO) + VisualUtils.Symbols.ARROW_RIGHT + " Confirm to save to database",
            "",
            VisualUtils.createActionLore("Click to save & continue", VisualUtils.ColorPalette.SUCCESS)
        );

        ItemStack saveButton = VisualUtils.createServerItem(
            saveButtonMaterial,
            "Save & Continue",
            saveLore,
            true
        );

        // Configure the template with compact server-themed title
        String configTitle = VisualUtils.createCompactServerTitle("Resume Editor");
        SelectionEditorConfig config = new SelectionEditorConfig(
                configTitle,
                playerHead,
                plugin.getConfigManager()
        )
                        .withSelectionMode(Category.TIMEZONE, SelectionMode.RADIO)
                        .withSelectionMode(Category.EXPERIENCE, SelectionMode.RADIO)
                        .withSelectionMode(Category.DAYS, SelectionMode.CHECKBOX)
                        .withSelectionMode(Category.SKILLS, SelectionMode.CHECKBOX);
        
                final SelectionEditorTemplate editor = new SelectionEditorTemplate(player, config, plugin.getConfigManager());
        
                config.withActionButton(49, saveButton, () -> {
                    Map<Category, Set<String>> selections = editor.getPlayerSelections();

                    // Validate and convert selections - prevent database truncation
                    String selectedTimezone = selections.get(Category.TIMEZONE).stream().findFirst().orElse(null);
                    String selectedExperience = selections.get(Category.EXPERIENCE).stream().findFirst().orElse(null);

                    // Convert and validate timezone
                    String timezoneDbKey = null;
                    if (selectedTimezone != null) {
                        timezoneDbKey = plugin.getConfigManager().getDatabaseKey(selectedTimezone);
                        if (timezoneDbKey == null || !plugin.getConfigManager().isValidDatabaseKey(timezoneDbKey, "timezone")) {
                            player.sendMessage(plugin.getConfigManager().getPrefix() +
                                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                                VisualUtils.Symbols.CROSS + " Invalid timezone selection: " + selectedTimezone);
                            plugin.getLogger().severe("Database key conversion failed for timezone: " + selectedTimezone + " -> " + timezoneDbKey);
                            return;
                        }
                    }

                    // Convert and validate experience
                    String experienceDbKey = null;
                    if (selectedExperience != null) {
                        experienceDbKey = plugin.getConfigManager().getDatabaseKey(selectedExperience);
                        if (experienceDbKey == null || !plugin.getConfigManager().isValidDatabaseKey(experienceDbKey, "experience")) {
                            player.sendMessage(plugin.getConfigManager().getPrefix() +
                                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                                VisualUtils.Symbols.CROSS + " Invalid experience selection: " + selectedExperience);
                            plugin.getLogger().severe("Database key conversion failed for experience: " + selectedExperience + " -> " + experienceDbKey);
                            return;
                        }
                    }

                    resumeToUpdate.setTimezone(timezoneDbKey);
                    resumeToUpdate.setExperience(experienceDbKey);

                    // Convert and validate available days
                    Set<String> databaseDays = new HashSet<>();
                    for (String day : selections.get(Category.DAYS)) {
                        String dayDbKey = plugin.getConfigManager().getDatabaseKey(day);
                        if (dayDbKey == null || !plugin.getConfigManager().isValidDatabaseKey(dayDbKey, "days")) {
                            player.sendMessage(plugin.getConfigManager().getPrefix() +
                                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                                VisualUtils.Symbols.CROSS + " Invalid day selection: " + day);
                            plugin.getLogger().severe("Database key conversion failed for day: " + day + " -> " + dayDbKey);
                            return;
                        }
                        databaseDays.add(dayDbKey);
                    }

                    // Convert and validate skills
                    Set<String> databaseSkills = new HashSet<>();
                    for (String skill : selections.get(Category.SKILLS)) {
                        String skillDbKey = plugin.getConfigManager().getDatabaseKey(skill);
                        if (skillDbKey == null || !plugin.getConfigManager().isValidDatabaseKey(skillDbKey, "skills")) {
                            player.sendMessage(plugin.getConfigManager().getPrefix() +
                                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                                VisualUtils.Symbols.CROSS + " Invalid skill selection: " + skill);
                            plugin.getLogger().severe("Database key conversion failed for skill: " + skill + " -> " + skillDbKey);
                            return;
                        }
                        databaseSkills.add(skillDbKey);
                    }

                    resumeToUpdate.setAvailableDays(databaseDays);
                    resumeToUpdate.setSkills(databaseSkills);
                    resumeToUpdate.setLastUpdated(System.currentTimeMillis());
        
                    // Open the confirmation screen
                    plugin.addPendingResume(player.getUniqueId(), resumeToUpdate); // Store the resume to be confirmed
                    new ConfirmationGUI(plugin, player, plugin.getConfigManager().getGuiString("resume-confirmation.title", "Confirm Resume Changes"), plugin.getConfigManager().getGuiString("resume-confirmation.message", "Review your new resume details."), resumeToUpdate,
                        () -> {
                            // onConfirm
                            plugin.getDatabaseManager().savePlayerResume(resumeToUpdate);
                            player.sendMessage(plugin.getConfigManager().getPrefix() +
                                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.SUCCESS) +
                                VisualUtils.Symbols.CHECK + " Your resume has been saved successfully!");
                            plugin.removePendingResume(player.getUniqueId());
                            plugin.getRecruitGUI().openMainMenu(player, null);
                        },
                        () -> {
                            // onCancel
                            player.sendMessage(plugin.getConfigManager().getPrefix() +
                                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.WARNING) +
                                VisualUtils.Symbols.CROSS + " Resume save cancelled.");
                            plugin.getResumeEditorGUI().openResumeEditor(player);
                        }
                    ).open();
                });
        
                config.withActionButton(48, visibilityToggleButton, () -> { // Add the new button here
                    // Get fresh resume data from database to avoid corrupted display names
                    PlayerResume freshResume = databaseManager.getPlayerResume(player.getUniqueId());
                    if (freshResume != null) {
                        freshResume.setHidden(!freshResume.isHidden());
                        databaseManager.savePlayerResume(freshResume);
                    }
                    openResumeEditor(player);
                });
        
                // Initialize selections in the template from the existing resume - convert database keys to display names
                Map<Category, Set<String>> initialSelections = new EnumMap<>(Category.class);

                // Convert single values (timezone, experience) from database keys to display names
                initialSelections.put(Category.TIMEZONE, existingResume.getTimezone() != null ?
                    new HashSet<>(Arrays.asList(plugin.getConfigManager().getDisplayName(existingResume.getTimezone()))) : new HashSet<>());
                initialSelections.put(Category.EXPERIENCE, existingResume.getExperience() != null ?
                    new HashSet<>(Arrays.asList(plugin.getConfigManager().getDisplayName(existingResume.getExperience()))) : new HashSet<>());

                // Convert sets (days, skills) from database keys to display names
                Set<String> displayDays = new HashSet<>();
                for (String dbDay : existingResume.getAvailableDays()) {
                    displayDays.add(plugin.getConfigManager().getDisplayName(dbDay));
                }

                Set<String> displaySkills = new HashSet<>();
                for (String dbSkill : existingResume.getSkills()) {
                    displaySkills.add(plugin.getConfigManager().getDisplayName(dbSkill));
                }

                initialSelections.put(Category.DAYS, displayDays);
                initialSelections.put(Category.SKILLS, displaySkills);
        
                // Create and open the GUI
                editor.setPlayerSelections(initialSelections);
                editor.drawGUI(); // Redraw to reflect initial selections
                player.openInventory(editor.getInventory());
        
                // Register the GUI as an event listener
                Bukkit.getPluginManager().registerEvents(editor, plugin);        
    }
}