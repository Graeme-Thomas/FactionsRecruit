package com.dirtygang.factionsrecruit;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PAPIIntegrationManager {

    private final FactionsRecruit plugin;
    private boolean papiEnabled;

    public PAPIIntegrationManager(FactionsRecruit plugin) {
        this.plugin = plugin;
        this.papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (this.papiEnabled) {
            plugin.getLogger().info("PlaceholderAPI detected. PAPI integration enabled.");
        } else {
            plugin.getLogger().warning("PlaceholderAPI not found. PAPI integration disabled. Factions integration will not work.");
        }
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public String getFactionName(OfflinePlayer player) {
        if (!papiEnabled) return null;
        return PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_name%");
    }

    public String getFactionInternalId(OfflinePlayer player) {
        if (!papiEnabled) return null;
        return PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_internal_id%");
    }

    public String getPlayerRole(OfflinePlayer player) {
        if (!papiEnabled) return null;
        return PlaceholderAPI.setPlaceholders(player, "%factionsuuid_player_role_name%");
    }

    public boolean isInFaction(OfflinePlayer player) {
        if (!papiEnabled) return false;
        String factionName = getFactionName(player);
        return factionName != null && !factionName.isEmpty() && !factionName.equalsIgnoreCase("none") && !factionName.contains("Wilderness");
    }

    // Method to add a player to a faction using PAPI (this will likely require a command execution)
    // PlaceholderAPI itself doesn't directly modify game state, it only provides information.
    // To add a player to a faction, you would typically execute a Factions command via console or as the player.
    // For example: /f invite <player> <faction> or /f join <faction>
    // This method will return the command string that needs to be executed.
    public String getFactionInviteCommand(Player inviter, Player invitedPlayer, String factionId) {
        if (!papiEnabled) return null;
        // This assumes the Factions plugin has a command to invite players.
        // The exact command might vary, but a common one is /f invite <player>
        // We need to get the inviter's faction name to invite to.
        String inviterFactionName = getFactionName(inviter);
        if (inviterFactionName == null || inviterFactionName.isEmpty() || inviterFactionName.equalsIgnoreCase("none")) {
            return null; // Inviter is not in a faction
        }
        return "f invite " + invitedPlayer.getName();
    }

    // Method to check if a player has a certain role in their faction
    public boolean hasRole(OfflinePlayer player, String role) {
        if (!papiEnabled) return false;
        String playerRole = getPlayerRole(player);
        return playerRole != null && playerRole.equalsIgnoreCase(role);
    }

    public String getFactionLeader(OfflinePlayer player) {
        if (!papiEnabled) return null;
        return PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_leader%");
    }

    public int getFactionSize(OfflinePlayer player) {
        if (!papiEnabled) return 0;
        String size = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_size%");
        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction size: " + size);
            return 0;
        }
    }

    public String getFactionDescription(OfflinePlayer player) {
        if (!papiEnabled) return null;
        return PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_description%");
    }

    public boolean isFactionPeaceful(OfflinePlayer player) {
        if (!papiEnabled) return false;
        String peaceful = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_peaceful%");
        return Boolean.parseBoolean(peaceful);
    }

    public double getFactionPower(OfflinePlayer player) {
        if (!papiEnabled) return 0.0;
        String power = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_power%");
        try {
            return Double.parseDouble(power);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction power: " + power);
            return 0.0;
        }
    }

    public double getFactionMaxPower(OfflinePlayer player) {
        if (!papiEnabled) return 0.0;
        String maxPower = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_powermax%");
        try {
            return Double.parseDouble(maxPower);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction max power: " + maxPower);
            return 0.0;
        }
    }

    public double getFactionDTR(OfflinePlayer player) {
        if (!papiEnabled) return 0.0;
        String dtr = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_dtr%");
        try {
            return Double.parseDouble(dtr);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction DTR: " + dtr);
            return 0.0;
        }
    }

    public double getFactionMaxDTR(OfflinePlayer player) {
        if (!papiEnabled) return 0.0;
        String maxDTR = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_dtrmax%");
        try {
            return Double.parseDouble(maxDTR);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction max DTR: " + maxDTR);
            return 0.0;
        }
    }

    public int getFactionClaims(OfflinePlayer player) {
        if (!papiEnabled) return 0;
        String claims = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_claims%");
        try {
            return Integer.parseInt(claims);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction claims: " + claims);
            return 0;
        }
    }

    public int getFactionMaxClaims(OfflinePlayer player) {
        if (!papiEnabled) return 0;
        String maxClaims = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_maxclaims%");
        try {
            return Integer.parseInt(maxClaims);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction max claims: " + maxClaims);
            return 0;
        }
    }

    public double getFactionBankBalance(OfflinePlayer player) {
        if (!papiEnabled) return 0.0;
        String balance = PlaceholderAPI.setPlaceholders(player, "%factionsuuid_faction_bank_balance%");
        try {
            return Double.parseDouble(balance);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Could not parse faction bank balance: " + balance);
            return 0.0;
        }
    }

    /**
     * Generic method to get any placeholder value
     * @param player The player to get the placeholder for
     * @param placeholder The placeholder string (should include % symbols)
     * @return The placeholder value or null if not available
     */
    public String getPlaceholderValue(OfflinePlayer player, String placeholder) {
        if (!papiEnabled) return null;
        String result = PlaceholderAPI.setPlaceholders(player, placeholder);
        // Return null if placeholder wasn't replaced (still contains % symbols)
        if (result != null && result.equals(placeholder)) {
            return null;
        }
        return result;
    }

    /**
     * Generic method to get any placeholder value with a fallback
     * @param player The player to get the placeholder for
     * @param placeholder The placeholder string (should include % symbols)
     * @param fallback The fallback value if placeholder is not available
     * @return The placeholder value or fallback if not available
     */
    public String getPlaceholderValue(OfflinePlayer player, String placeholder, String fallback) {
        String result = getPlaceholderValue(player, placeholder);
        return result != null ? result : fallback;
    }
}