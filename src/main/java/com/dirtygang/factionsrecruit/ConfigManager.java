package com.dirtygang.factionsrecruit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration guiConfig;
    private File guiConfigFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        loadGuiConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig(); // Creates config.yml if it doesn't exist
        config = plugin.getConfig();
    }

    public void loadGuiConfig() {
        guiConfigFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiConfigFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiConfigFile);
    }

    public String getGuiString(String path, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&', guiConfig.getString(path, defaultValue));
    }

    public List<String> getGuiStringList(String path, List<String> defaultValue) {
        List<String> rawList = guiConfig.getStringList(path);
        return rawList.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }

    public Material getMaterial(String path, Material defaultValue) {
        String materialName = guiConfig.getString(path);
        if (materialName == null || materialName.isEmpty()) {
            plugin.getLogger().warning("No material specified in gui.yml at path '" + path + "'. Using default value.");

            return defaultValue;
        }
        try {
            
            return Material.valueOf(materialName.toUpperCase());

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material name '" + materialName + "' in gui.yml at path '" + path + "'. Using default value.");
            return defaultValue;
        }
    }

    public boolean isDebugMode() {
        return config.getBoolean("debug_mode", false);
    }

    public boolean isDebugLoggingEnabled() {
        return config.getBoolean("testing.log-gui-interactions", false);
    }

    public String getGuiTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui_title", "&6Faction Recruitment Browser"));
    }

    public String getPlayerNotFoundMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.player_not_found", "&cPlayer %player% not found or is offline."));
    }

    public String getRecruitmentRequestSentMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.recruitment_request_sent", "&aRecruitment request sent to %player%."));
    }

    public String getAlreadySentRequestMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.already_sent_request", "&cYou have already sent a recruitment request to %player%."));
    }

    public String getRecruitmentRequestMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.recruitment_request", "&e%player% has sent you a recruitment request for faction %faction%."));
    }

    public String getNoPendingRequestMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_pending_request", "&cYou have no pending recruitment requests."));
    }

    public String getNoPermissionMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_permission", "&cYou do not have permission to use this command."));
    }

    public String getInvitationSentMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation_sent", "&aYou have sent a recruitment invitation to &e{target}&a."));
    }

    

    public String getNoPendingInvitationMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_pending_invitation", "&cYou have no pending recruitment invitations."));
    }

    public String getRecruitmentRequestExpiredRecruitMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.recruitment_request_expired_recruit", "&cThe recruitment request from %recruiter% has expired."));
    }

    public String getRecruitmentRequestExpiredRecruiterMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.recruitment_request_expired_recruiter", "&cYour recruitment request to %recruit% has expired."));
    }

    public int getRecruitmentRequestTimeoutSeconds() {
        return config.getInt("recruitment_request_timeout_seconds", 60);
    }

    public String getDatabaseConnectionString() {
        return plugin.getConfig().getString("database.connection-string");
    }

    // New Settings from SPEC.md
    public int getFactionMemberLimit() {
        return config.getInt("faction-member-limit", 30);
    }

    public int getApplicationSlotsPerPlayer() {
        return config.getInt("application-slots-per-player", 3);
    }

    public int getApplicationExpiryDays() {
        return config.getInt("application-expiry-days", 3);
    }

    public int getInvitationExpiryDays() {
        return config.getInt("invitation-expiry-days", 3);
    }

    public int getResumeDisplayDays() {
        return config.getInt("resume-display-days", 3);
    }

    public int getApplicationCooldownHours() {
        return config.getInt("application-cooldown-hours", 24);
    }

    public int getResumeEditCooldownHours() {
        return config.getInt("resume-edit-cooldown-hours", 6);
    }

    // New Notification Settings from SPEC.md
    public boolean isLoginCheckEnabled() {
        return config.getBoolean("notifications.login-check", true);
    }

    public String getSoundSuccess() {
        return config.getString("notifications.sounds.success", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public String getSoundError() {
        return config.getString("notifications.sounds.error", "BLOCK_NOTE_BLOCK_BASS");
    }

    public String getSoundClick() {
        return config.getString("notifications.sounds.click", "BLOCK_NOTE_BLOCK_PLING");
    }

    public String getSoundNavigation() {
        return config.getString("notifications.sounds.navigation", "UI_BUTTON_CLICK");
    }

    // New Messages from SPEC.md
    public int getMaxOutgoingApplications() {
        return config.getInt("application-slots-per-player", 3);
    }

    public String getApplicationSentMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.application-sent", "&aApplication sent to %faction%!"));
    }

    public String getApplicationExpiredMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.application-expired", "&cYou have expired applications. Use /recruit to check status."));
    }

    public String getApplicationCancelledMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.application-cancelled", "&eApplication to %faction% cancelled"));
    }

    public String getInvitationReceivedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation_received", "&6You have received an invitation from %faction%!"));
    }

    public String getInvitationExpiredMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation-expired", "&cInvitation from %faction% has expired"));
    }

    public String getResumeExpiredMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.resume-expired", "&cYour resume is no longer visible to faction owners"));
    }

    public String getSlotsAvailableMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.slots-available", "&aYou have %slots% application slots available"));
    }

    public String getExpiredApplicationsMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.expired-applications", "&cYou have expired applications. Use /recruit to check status."));
    }

    public String getNewInvitationsMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.new-invitations", "&6You have pending faction invitations! Use /recruit to view them."));
    }

    public String getAvailableSlotsMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.available-slots", "&aYou have %count% application slots available."));
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", "&8[&6FactionsRecruit&8] &r"));
    }

    public String getRecruitCommandUsage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.recruit_command_usage", "&cUsage: /recruit <player> | /recruit accept | /recruit deny"));
    }

    public String getInviterOfflineMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.inviter_offline", "&cThe inviter is no longer online. Invitation cancelled."));
    }

    public String getInvitationAcceptedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation_accepted", "&aYou have successfully joined &e{faction}&a!"));
    }

    public String getTargetAcceptedInvitationMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.target_accepted_invitation", "&e{target}&a has accepted your recruitment invitation!"));
    }

    public String getFailedToJoinFactionMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.failed_to_join_faction", "&cFailed to join the faction. Please try again later."));
    }

    public String getTargetFailedToJoinFactionMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.target_failed_to_join_faction", "&e{target}&c failed to join your faction."));
    }

    public String getInvitationDeniedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation_denied", "&cYou have denied the recruitment invitation."));
    }

    public String getInvitationRejectedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation_rejected", "&cYou have rejected the recruitment invitation."));
    }

    public String getTargetDeniedInvitationMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.target_denied_invitation", "&e{target}&c has denied the recruitment invitation."));
    }

    public String getApplicationReceivedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.application-received", "&6%player% has applied to join your faction!"));
    }

    public String getApplicationAcceptedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.application-accepted", "&aYour application to %faction% has been accepted!"));
    }

    public String getApplicationRejectedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.application-rejected", "&cYour application to %faction% has been rejected."));
    }

    public String getInvitationAcceptedPlayerMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation-accepted-player", "&a%player% has accepted your faction invitation!"));
    }

    public String getInvitationRejectedPlayerMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.invitation-rejected-player", "&c%player% has declined your faction invitation."));
    }

    // Simple conversion between display names and database keys
    public String getDatabaseKey(String displayName) {
        if (displayName == null) {
            plugin.getLogger().warning("getDatabaseKey called with null displayName");
            return null;
        }

        String databaseKey = guiConfig.getString("selection-editor.database-keys." + displayName, null);

        if (databaseKey == null) {
            plugin.getLogger().warning("No database key mapping found for display name: '" + displayName + "'. Available mappings: " +
                (guiConfig.getConfigurationSection("selection-editor.database-keys") != null ?
                    guiConfig.getConfigurationSection("selection-editor.database-keys").getKeys(false) : "none"));
            // Return null instead of original value to prevent database truncation
            return null;
        }

        if (isDebugMode()) {
            plugin.getLogger().info("Converted display name '" + displayName + "' to database key '" + databaseKey + "'");
        }

        return databaseKey;
    }

    public List<String> getDatabaseKeys(List<String> displayNames) {
        return displayNames.stream()
                .map(this::getDatabaseKey)
                .filter(key -> key != null)  // Filter out null values from failed conversions
                .collect(Collectors.toList());
    }

    // For loading existing data - reverse lookup (display name from database key)
    public String getDisplayName(String databaseKey) {
        if (databaseKey == null) {
            plugin.getLogger().warning("getDisplayName called with null databaseKey");
            return null;
        }

        // Find the display name that maps to this database key
        if (guiConfig.getConfigurationSection("selection-editor.database-keys") != null) {
            for (String displayName : guiConfig.getConfigurationSection("selection-editor.database-keys").getKeys(false)) {
                String mappedKey = guiConfig.getString("selection-editor.database-keys." + displayName);
                if (databaseKey.equals(mappedKey)) {
                    if (isDebugMode()) {
                        plugin.getLogger().info("Converted database key '" + databaseKey + "' to display name '" + displayName + "'");
                    }
                    return displayName;
                }
            }
        }

        plugin.getLogger().warning("No display name mapping found for database key: '" + databaseKey + "'. Available mappings: " +
            (guiConfig.getConfigurationSection("selection-editor.database-keys") != null ?
                guiConfig.getConfigurationSection("selection-editor.database-keys").getKeys(false) : "none"));

        return databaseKey; // Fallback to prevent null values in UI
    }

    public List<String> getDisplayNames(List<String> databaseKeys) {
        return databaseKeys.stream()
                .filter(key -> key != null)  // Filter out null values
                .map(this::getDisplayName)
                .filter(name -> name != null)  // Filter out null results
                .collect(Collectors.toList());
    }

    // Validation method to check if a value is a valid database key for the given category
    public boolean isValidDatabaseKey(String value, String category) {
        if (value == null) return false;

        // Check against database schema constraints
        switch (category.toLowerCase()) {
            case "timezone":
                return value.matches("^(NA_WEST|NA_EAST|EU_WEST|EU_CENTRAL|ASIA|OCEANIA)$");
            case "experience":
                return value.matches("^(UNDER_6MO|1_YEAR|1_2_YEARS|2_3_YEARS|3_4_YEARS|4_5_YEARS|5_PLUS_YEARS)$");
            case "days":
                return value.matches("^(MON|TUE|WED|THU|FRI|SAT|SUN)$");
            case "skills":
                return value.matches("^(CANNON|PVP|DEFENSE|DESIGN|REDSTONE|FARM|FISH)$");
            default:
                return false;
        }
    }

}