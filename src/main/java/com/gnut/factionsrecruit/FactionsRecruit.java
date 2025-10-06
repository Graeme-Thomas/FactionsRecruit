package com.gnut.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gnut.factionsrecruit.interfaces.FactionApplicationEditorGUI;
import com.gnut.factionsrecruit.interfaces.FactionApplicationReviewGUI;
import com.gnut.factionsrecruit.interfaces.FilterGUI;
import com.gnut.factionsrecruit.interfaces.HelpGUI;
import com.gnut.factionsrecruit.interfaces.InvitationsGUI;
import com.gnut.factionsrecruit.interfaces.ManageApplicationGUI;
import com.gnut.factionsrecruit.interfaces.ManageResumeGUI;
import com.gnut.factionsrecruit.interfaces.PendingApplicationsGUI;
import com.gnut.factionsrecruit.interfaces.PlayerInfoGUI;
import com.gnut.factionsrecruit.interfaces.RecruitGUI;
import com.gnut.factionsrecruit.interfaces.ResumeEditorGUI;
import com.gnut.factionsrecruit.manager.ConfigManager;
import com.gnut.factionsrecruit.manager.DatabaseManager;
import com.gnut.factionsrecruit.manager.FilterManager;
import com.gnut.factionsrecruit.manager.GuiManager;
import com.gnut.factionsrecruit.manager.PAPIIntegrationManager;
import com.gnut.factionsrecruit.model.FactionApplication;
import com.gnut.factionsrecruit.model.FactionInvitation;
import com.gnut.factionsrecruit.model.LoginNotification;
import com.gnut.factionsrecruit.model.PlayerApplication;
import com.gnut.factionsrecruit.model.PlayerResume;
import com.gnut.factionsrecruit.util.CleanupTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class FactionsRecruit extends JavaPlugin {

    public enum MainMenuDisplayMode {
        PLAYER_MODE,
        FACTION_MODE
    }

    private static FactionsRecruit instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PAPIIntegrationManager papiIntegrationManager;
    private ResumeEditorGUI resumeEditorGUI;
    private FactionApplicationEditorGUI factionApplicationEditorGUI;
    private FactionApplicationReviewGUI factionApplicationReviewGUI;
    private RecruitGUI recruitGUI; 
    private GuiManager guiManager; 
    private FilterManager filterManager; // Add this 
    private PlayerInfoGUI playerInfoGUI;
    private HelpGUI helpGUI;
    private ManageApplicationGUI manageApplicationGUI;
    private ManageResumeGUI manageResumeGUI;
    private PendingApplicationsGUI pendingApplicationsGUI;
    private InvitationsGUI invitationsGUI;
    private FilterGUI filterGUI;
    private Map<UUID, PlayerResume> pendingResumes; // Temporary storage for resumes being edited
    private Map<UUID, UUID> pendingInvitations; // Key: Inviter UUID, Value: Invited Player UUID
    private Map<UUID, FactionApplication> pendingFactionApplications; // Key: Player UUID, Value: FactionApplication being edited
    private Map<UUID, FactionApplication> pendingPlayerApplications; // Key: Player UUID, Value: FactionApplication being applied to
    private MainMenuDisplayMode currentMainMenuDisplayMode; // Current display mode for the main menu
    private FactionApplication filter;
    private UUID searchingPlayer;
    private int playerListPage = 1;
    private int factionListPage = 1;
    private List<PlayerResume> searchResultPlayers = null;
    private List<FactionApplication> searchResultFactions = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        getLogger().info("Initializing ConfigManager...");
        configManager = new ConfigManager(this);
        getLogger().info("ConfigManager initialized.");

        getLogger().info("Initializing DatabaseManager...");
        databaseManager = new DatabaseManager(this, configManager);
        getLogger().info("DatabaseManager initialized.");

        getLogger().info("Initializing PAPIIntegrationManager...");
        papiIntegrationManager = new PAPIIntegrationManager(this);
        getLogger().info("PAPIIntegrationManager initialized.");

        getLogger().info("Initializing ResumeEditorGUI...");
        resumeEditorGUI = new ResumeEditorGUI(this, databaseManager);
        getLogger().info("ResumeEditorGUI initialized.");

        getLogger().info("Initializing FactionApplicationEditorGUI...");
        factionApplicationEditorGUI = new FactionApplicationEditorGUI(this, databaseManager, papiIntegrationManager);
        getLogger().info("FactionApplicationEditorGUI initialized.");

        getLogger().info("Initializing GuiManager...");
        guiManager = new GuiManager();
        getLogger().info("GuiManager initialized.");

        filterManager = new FilterManager();
        getLogger().info("FilterManager initialized.");

        playerInfoGUI = new PlayerInfoGUI(this);
        helpGUI = new HelpGUI(this);
        manageApplicationGUI = new ManageApplicationGUI(this);
        manageResumeGUI = new ManageResumeGUI(this);
        pendingApplicationsGUI = new PendingApplicationsGUI(this);
        invitationsGUI = new InvitationsGUI(this);
        filterGUI = new FilterGUI(this);

        pendingResumes = new HashMap<>();
        pendingInvitations = new HashMap<>();
        pendingFactionApplications = new HashMap<>();
        pendingPlayerApplications = new HashMap<>();
        currentMainMenuDisplayMode = MainMenuDisplayMode.PLAYER_MODE; // Default mode

        recruitGUI = new RecruitGUI(this); // Initialize RecruitGUI
        factionApplicationReviewGUI = new FactionApplicationReviewGUI(this, recruitGUI);
        getCommand("recruit").setExecutor(new RecruitCommandExecutor(this, recruitGUI));
        getCommand("recruitadmin").setExecutor(new RecruitAdminCommandExecutor(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this, recruitGUI), this);
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);

        // Schedule cleanup task to run every hour (20 ticks * 60 seconds * 60 minutes = 72000 ticks)
        new CleanupTask(this).runTaskTimer(this, 0L, 72000L);

        getLogger().info("FactionsRecruit has been enabled!");
    }

    public MainMenuDisplayMode getCurrentMainMenuDisplayMode() {
        return currentMainMenuDisplayMode;
    }

    public void setMainMenuDisplayMode(MainMenuDisplayMode mode) {
        this.currentMainMenuDisplayMode = mode;
    }

    public void addPendingInvitation(UUID inviterUuid, UUID invitedPlayerUuid) {
        pendingInvitations.put(inviterUuid, invitedPlayerUuid);
    }

    public UUID getPendingInvitedPlayer(UUID inviterUuid) {
        return pendingInvitations.get(inviterUuid);
    }

    public void removePendingInvitation(UUID inviterUuid) {
        pendingInvitations.remove(inviterUuid);
    }

    public void removePendingFactionApplication(UUID playerUuid) {
        pendingFactionApplications.remove(playerUuid);
    }

    public FactionApplication getPendingFactionApplication(UUID playerUuid) {
        return pendingFactionApplications.get(playerUuid);
    }

    public void addPendingFactionApplication(UUID playerUuid, FactionApplication application) {
        pendingFactionApplications.put(playerUuid, application);
    }

    public void addPendingPlayerApplication(UUID playerUuid, FactionApplication application) {
        pendingPlayerApplications.put(playerUuid, application);
    }

    public FactionApplication getPendingPlayerApplication(UUID playerUuid) {
        return pendingPlayerApplications.get(playerUuid);
    }

    public void removePendingPlayerApplication(UUID playerUuid) {
        pendingPlayerApplications.remove(playerUuid);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("FactionsRecruit has been disabled!");
    }

    public static FactionsRecruit getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PAPIIntegrationManager getPapiIntegrationManager() {
        return papiIntegrationManager;
    }

    public ResumeEditorGUI getResumeEditorGUI() {
        return resumeEditorGUI;
    }

    public FactionApplicationEditorGUI getFactionApplicationEditorGUI() {
        return factionApplicationEditorGUI;
    }

    public FactionApplicationReviewGUI getFactionApplicationReviewGUI() {
        return factionApplicationReviewGUI;
    }

    public RecruitGUI getRecruitGUI() {
        return recruitGUI;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public FilterManager getFilterManager() {
        return filterManager;
    }

    public PlayerInfoGUI getPlayerInfoGUI() {
        return playerInfoGUI;
    }

    public HelpGUI getHelpGUI() {
        return helpGUI;
    }

    public ManageApplicationGUI getManageApplicationGUI() {
        return manageApplicationGUI;
    }

    public ManageResumeGUI getManageResumeGUI() {
        return manageResumeGUI;
    }

    public PendingApplicationsGUI getPendingApplicationsGUI() {
        return pendingApplicationsGUI;
    }

    public InvitationsGUI getInvitationsGUI() {
        return invitationsGUI;
    }

    public FilterGUI getFilterGUI() {
        return filterGUI;
    }

    public void addPendingResume(UUID playerUuid, PlayerResume resume) {
        pendingResumes.put(playerUuid, resume);
    }

    public PlayerResume getPendingResume(UUID playerUuid) {
        return pendingResumes.get(playerUuid);
    }

    public void removePendingResume(UUID playerUuid) {
        pendingResumes.remove(playerUuid);
    }

    public FactionApplication getFilter() {
        return filter;
    }

    public void setFilter(FactionApplication filter) {
        this.filter = filter;
    }

    public UUID getSearchingPlayer() {
        return searchingPlayer;
    }

    public void setSearchingPlayer(UUID searchingPlayer) {
        this.searchingPlayer = searchingPlayer;
    }

    public int getPlayerListPage() {
        return playerListPage;
    }

    public void setPlayerListPage(int playerListPage) {
        this.playerListPage = playerListPage;
    }

    public int getFactionListPage() {
        return factionListPage;
    }

    public void setFactionListPage(int factionListPage) {
        this.factionListPage = factionListPage;
    }

    public List<PlayerResume> getSearchResultPlayers() {
        return searchResultPlayers;
    }

    public void setSearchResultPlayers(List<PlayerResume> players) {
        this.searchResultPlayers = players;
    }

    public List<FactionApplication> getSearchResultFactions() {
        return searchResultFactions;
    }

    public void setSearchResultFactions(List<FactionApplication> factions) {
        this.searchResultFactions = factions;
    }

    public void clearSearchResults() {
        this.searchResultPlayers = null;
        this.searchResultFactions = null;
    }

    public void sendApplication(Player player, FactionApplication factionApplication) {
        int maxOutgoingApplications = configManager.getMaxOutgoingApplications();
        int currentActiveApplications = databaseManager.getActiveApplicationSlots(player.getUniqueId());

        if (currentActiveApplications >= maxOutgoingApplications) {
            player.sendMessage(configManager.getPrefix() + "§cYou have reached the maximum number of application slots (" + maxOutgoingApplications + "). Please wait for slots to become available.");
            return;
        }

        // Check 24-hour cooldown for applications to the same faction
        if (databaseManager.hasRecentApplicationToFaction(player.getUniqueId(), factionApplication.getFactionId(), 24 * 60 * 60 * 1000L)) {
            player.sendMessage(configManager.getPrefix() + "§cYou must wait 24 hours before applying to this faction again.");
            return;
        }

        // Calculate slot availability (3 days from now)
        long slotAvailableAt = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L);
        PlayerApplication application = new PlayerApplication(0, player.getUniqueId(), factionApplication.getFactionId(), System.currentTimeMillis(), System.currentTimeMillis() + (configManager.getApplicationExpiryDays() * 24 * 60 * 60 * 1000L), slotAvailableAt, "PENDING");
        databaseManager.savePlayerApplication(application);

        player.sendMessage(configManager.getPrefix() + configManager.getApplicationSentMessage().replace("%faction%", factionApplication.getFactionName()));

        Player leader = Bukkit.getPlayer(factionApplication.getLeaderUuid());
        if (leader != null) {
            leader.sendMessage(configManager.getPrefix() + "§6" + player.getName() + " has applied to join your faction!");
        }

        LoginNotification notification = databaseManager.getLoginNotification(factionApplication.getLeaderUuid());
        if (notification == null) {
            notification = new LoginNotification(factionApplication.getLeaderUuid(), false, false, false, true, false, false, System.currentTimeMillis());
        } else {
            notification.setHasNewApplications(true);
        }
        databaseManager.saveLoginNotification(notification);
    }

    public void sendInvitation(Player inviter, Player invitedPlayer, String factionId) {
        FactionInvitation invitation = new FactionInvitation(0, factionId, invitedPlayer.getUniqueId(), inviter.getUniqueId(), System.currentTimeMillis(), System.currentTimeMillis() + (configManager.getInvitationExpiryDays() * 24 * 60 * 60 * 1000), "PENDING");
        databaseManager.saveFactionInvitation(invitation);

        inviter.sendMessage(configManager.getPrefix() + configManager.getInvitationSentMessage().replace("%player%", invitedPlayer.getName()));

        invitedPlayer.sendMessage(configManager.getPrefix() + configManager.getInvitationReceivedMessage().replace("%faction%", papiIntegrationManager.getFactionName(inviter)));

        LoginNotification notification = databaseManager.getLoginNotification(invitedPlayer.getUniqueId());
        if (notification == null) {
            notification = new LoginNotification(invitedPlayer.getUniqueId(), false, true, false, false, false, false, System.currentTimeMillis());
        } else {
            notification.setHasNewInvitations(true);
        }
        databaseManager.saveLoginNotification(notification);
    }

    public void acceptApplication(Player player, PlayerApplication application) {
        application.setStatus("ACCEPTED");
        databaseManager.savePlayerApplication(application);

        player.sendMessage(configManager.getPrefix() + configManager.getApplicationAcceptedMessage().replace("%faction%", papiIntegrationManager.getFactionName(player)));

        Player applicant = Bukkit.getPlayer(application.getPlayerUuid());
        if (applicant != null) {
            applicant.sendMessage(configManager.getPrefix() + configManager.getApplicationAcceptedMessage().replace("%faction%", papiIntegrationManager.getFactionName(player)));
        }

        LoginNotification notification = databaseManager.getLoginNotification(application.getPlayerUuid());
        if (notification == null) {
            notification = new LoginNotification(application.getPlayerUuid(), false, false, false, false, true, false, System.currentTimeMillis());
        } else {
            notification.setHasAcceptedApplications(true);
        }
        databaseManager.saveLoginNotification(notification);
    }

    public void rejectApplication(Player player, PlayerApplication application) {
        application.setStatus("REJECTED");
        databaseManager.savePlayerApplication(application);

        player.sendMessage(configManager.getPrefix() + configManager.getApplicationRejectedMessage().replace("%faction%", papiIntegrationManager.getFactionName(player)));

        Player applicant = Bukkit.getPlayer(application.getPlayerUuid());
        if (applicant != null) {
            applicant.sendMessage(configManager.getPrefix() + configManager.getApplicationRejectedMessage().replace("%faction%", papiIntegrationManager.getFactionName(player)));
        }

        LoginNotification notification = databaseManager.getLoginNotification(application.getPlayerUuid());
        if (notification == null) {
            notification = new LoginNotification(application.getPlayerUuid(), false, false, false, false, false, true, System.currentTimeMillis());
        } else {
            notification.setHasRejectedApplications(true);
        }
        databaseManager.saveLoginNotification(notification);
    }

    public void acceptInvitation(Player player, FactionInvitation invitation) {
        invitation.setStatus("ACCEPTED");
        databaseManager.saveFactionInvitation(invitation);

        player.sendMessage(configManager.getPrefix() + configManager.getInvitationAcceptedMessage().replace("%player%", player.getName()));

        Player inviter = Bukkit.getPlayer(invitation.getInvitedBy());
        if (inviter != null) {
            inviter.sendMessage(configManager.getPrefix() + configManager.getInvitationAcceptedMessage().replace("%player%", player.getName()));
        }

        LoginNotification notification = databaseManager.getLoginNotification(invitation.getInvitedBy());
        if (notification == null) {
            notification = new LoginNotification(invitation.getInvitedBy(), false, false, false, false, true, false, System.currentTimeMillis());
        } else {
            notification.setHasAcceptedApplications(true);
        }
        databaseManager.saveLoginNotification(notification);
    }

    public void rejectInvitation(Player player, FactionInvitation invitation) {
        invitation.setStatus("REJECTED");
        databaseManager.saveFactionInvitation(invitation);

        player.sendMessage(configManager.getPrefix() + configManager.getInvitationRejectedMessage().replace("%player%", player.getName()));

        Player inviter = Bukkit.getPlayer(invitation.getInvitedBy());
        if (inviter != null) {
            inviter.sendMessage(configManager.getPrefix() + configManager.getInvitationRejectedMessage().replace("%player%", player.getName()));
        }

        LoginNotification notification = databaseManager.getLoginNotification(invitation.getInvitedBy());
        if (notification == null) {
            notification = new LoginNotification(invitation.getInvitedBy(), false, false, false, false, false, true, System.currentTimeMillis());
        } else {
            notification.setHasRejectedApplications(true);
        }
        databaseManager.saveLoginNotification(notification);
    }
}