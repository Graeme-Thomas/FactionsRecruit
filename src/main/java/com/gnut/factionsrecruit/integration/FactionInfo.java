package com.gnut.factionsrecruit.integration;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Immutable data class representing faction information retrieved from PlaceholderAPI
 * Used for displaying faction details in GUIs
 */
public class FactionInfo {
    private final UUID factionId;
    private final String factionName;
    private final String factionDescription;
    private final String leaderName;
    private final String founded;

    // Member statistics
    private final int onlineMembers;
    private final int offlineMembers;
    private final int totalMembers;

    // Faction value and ranking
    private final String factionValue;
    private final String ftopPosition;
    private final String factionAge;

    // Additional info
    private final boolean isJoining;
    private final boolean isPeaceful;
    private final int claims;
    private final int maxClaims;
    private final double bankBalance;

    // Power/DTR
    private final String power;
    private final String maxPower;

    private FactionInfo(Builder builder) {
        this.factionId = builder.factionId;
        this.factionName = builder.factionName;
        this.factionDescription = builder.factionDescription;
        this.leaderName = builder.leaderName;
        this.founded = builder.founded;
        this.onlineMembers = builder.onlineMembers;
        this.offlineMembers = builder.offlineMembers;
        this.totalMembers = builder.totalMembers;
        this.factionValue = builder.factionValue;
        this.ftopPosition = builder.ftopPosition;
        this.factionAge = builder.factionAge;
        this.isJoining = builder.isJoining;
        this.isPeaceful = builder.isPeaceful;
        this.claims = builder.claims;
        this.maxClaims = builder.maxClaims;
        this.bankBalance = builder.bankBalance;
        this.power = builder.power;
        this.maxPower = builder.maxPower;
    }

    // Getters
    public UUID getFactionId() { return factionId; }
    public String getFactionName() { return factionName; }
    public String getFactionDescription() { return factionDescription; }
    public String getLeaderName() { return leaderName; }
    public String getFounded() { return founded; }
    public int getOnlineMembers() { return onlineMembers; }
    public int getOfflineMembers() { return offlineMembers; }
    public int getTotalMembers() { return totalMembers; }
    public String getFactionValue() { return factionValue; }
    public String getFtopPosition() { return ftopPosition; }
    public String getFactionAge() { return factionAge; }
    public boolean isJoining() { return isJoining; }
    public boolean isPeaceful() { return isPeaceful; }
    public int getClaims() { return claims; }
    public int getMaxClaims() { return maxClaims; }
    public double getBankBalance() { return bankBalance; }
    public String getPower() { return power; }
    public String getMaxPower() { return maxPower; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID factionId;
        private String factionName = "Unknown";
        private String factionDescription = "";
        private String leaderName = "Unknown";
        private String founded = "Unknown";
        private int onlineMembers = 0;
        private int offlineMembers = 0;
        private int totalMembers = 0;
        private String factionValue = "0";
        private String ftopPosition = "N/A";
        private String factionAge = "Unknown";
        private boolean isJoining = false;
        private boolean isPeaceful = false;
        private int claims = 0;
        private int maxClaims = 0;
        private double bankBalance = 0.0;
        private String power = "0";
        private String maxPower = "0";

        public Builder factionId(UUID factionId) {
            this.factionId = factionId;
            return this;
        }

        public Builder factionName(String factionName) {
            this.factionName = factionName;
            return this;
        }

        public Builder factionDescription(String factionDescription) {
            this.factionDescription = factionDescription;
            return this;
        }

        public Builder leaderName(String leaderName) {
            this.leaderName = leaderName;
            return this;
        }

        public Builder founded(String founded) {
            this.founded = founded;
            return this;
        }

        public Builder onlineMembers(int onlineMembers) {
            this.onlineMembers = onlineMembers;
            return this;
        }

        public Builder offlineMembers(int offlineMembers) {
            this.offlineMembers = offlineMembers;
            return this;
        }

        public Builder totalMembers(int totalMembers) {
            this.totalMembers = totalMembers;
            return this;
        }

        public Builder factionValue(String factionValue) {
            this.factionValue = factionValue;
            return this;
        }

        public Builder ftopPosition(String ftopPosition) {
            this.ftopPosition = ftopPosition;
            return this;
        }

        public Builder factionAge(String factionAge) {
            this.factionAge = factionAge;
            return this;
        }

        public Builder isJoining(boolean isJoining) {
            this.isJoining = isJoining;
            return this;
        }

        public Builder isPeaceful(boolean isPeaceful) {
            this.isPeaceful = isPeaceful;
            return this;
        }

        public Builder claims(int claims) {
            this.claims = claims;
            return this;
        }

        public Builder maxClaims(int maxClaims) {
            this.maxClaims = maxClaims;
            return this;
        }

        public Builder bankBalance(double bankBalance) {
            this.bankBalance = bankBalance;
            return this;
        }

        public Builder power(String power) {
            this.power = power;
            return this;
        }

        public Builder maxPower(String maxPower) {
            this.maxPower = maxPower;
            return this;
        }

        public FactionInfo build() {
            return new FactionInfo(this);
        }
    }

    @Override
    public String toString() {
        return "FactionInfo{" +
                "factionName='" + factionName + '\'' +
                ", leaderName='" + leaderName + '\'' +
                ", totalMembers=" + totalMembers +
                ", ftopPosition='" + ftopPosition + '\'' +
                '}';
    }
}