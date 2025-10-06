package com.gnut.factionsrecruit;

import java.util.List;
import java.util.UUID;

public class FactionApplication {
    private String factionId;
    private String factionName;
    private UUID leaderUuid;
    private List<String> desiredTimezones;
    private List<String> experienceLevels;
    private List<String> requiredDays;
    private List<String> desiredSkills;
    private boolean isAccepting;
    private long createdAt;
    private long updatedAt;

    public FactionApplication(String factionId, String factionName, UUID leaderUuid, List<String> desiredTimezones, List<String> experienceLevels, List<String> requiredDays, List<String> desiredSkills, boolean isAccepting, long createdAt, long updatedAt) {
        this.factionId = factionId;
        this.factionName = factionName;
        this.leaderUuid = leaderUuid;
        this.desiredTimezones = desiredTimezones;
        this.experienceLevels = experienceLevels;
        this.requiredDays = requiredDays;
        this.desiredSkills = desiredSkills;
        this.isAccepting = isAccepting;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getFactionId() {
        return factionId;
    }

    public String getFactionName() {
        return factionName;
    }

    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public List<String> getDesiredTimezones() {
        return desiredTimezones;
    }

    public List<String> getExperienceLevels() {
        return experienceLevels;
    }

    public List<String> getRequiredDays() {
        return requiredDays;
    }

    public List<String> getDesiredSkills() {
        return desiredSkills;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    // Setters (if needed)
    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    public void setLeaderUuid(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public void setDesiredTimezones(List<String> desiredTimezones) {
        this.desiredTimezones = desiredTimezones;
    }

    public void setExperienceLevels(List<String> experienceLevels) {
        this.experienceLevels = experienceLevels;
    }

    public void setRequiredDays(List<String> requiredDays) {
        this.requiredDays = requiredDays;
    }

    public void setDesiredSkills(List<String> desiredSkills) {
        this.desiredSkills = desiredSkills;
    }

    public void setAccepting(boolean accepting) {
        isAccepting = accepting;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
