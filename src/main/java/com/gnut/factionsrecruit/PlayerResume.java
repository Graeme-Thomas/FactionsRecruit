package com.gnut.factionsrecruit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerResume {
    private UUID playerUUID;
    private String timezone;
    private String experience;
    private Set<String> availableDays;
    private Set<String> skills;
    private boolean isLooking;
    private long displayUntil;
    private long lastUpdated;
    private long createdAt;
    private boolean isHidden;

    public PlayerResume(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.availableDays = new HashSet<>();
        this.skills = new HashSet<>();
        this.isLooking = false;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.lastUpdated = now;
        this.displayUntil = 0; // 0 means no expiry by default
    }

    public PlayerResume(UUID playerUUID, String timezone, String experience, Set<String> availableDays, Set<String> skills, boolean isLooking, long displayUntil, long lastUpdated, long createdAt, boolean isHidden) {
        this.playerUUID = playerUUID;
        this.timezone = timezone;
        this.experience = experience;
        this.availableDays = availableDays;
        this.skills = skills;
        this.isLooking = isLooking;
        this.displayUntil = displayUntil;
        this.lastUpdated = lastUpdated;
        this.createdAt = createdAt;
        this.isHidden = isHidden;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public Set<String> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(Set<String> availableDays) {
        this.availableDays = availableDays;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    public boolean isLooking() {
        return isLooking;
    }

    public void setLooking(boolean looking) {
        isLooking = looking;
    }

    public long getDisplayUntil() {
        return displayUntil;
    }

    public void setDisplayUntil(long displayUntil) {
        this.displayUntil = displayUntil;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Helper to convert Set<String> to comma-separated String
    public String getAvailableDaysAsString() {
        return String.join(",", availableDays);
    }

    public String getSkillsAsString() {
        return String.join(",", skills);
    }

    // Helper to convert comma-separated String to Set<String>
    public void setAvailableDaysFromString(String daysString) {
        if (daysString != null && !daysString.isEmpty()) {
            this.availableDays = new HashSet<>(Arrays.asList(daysString.split(",")));
        } else {
            this.availableDays = new HashSet<>();
        }
    }

    public void setSkillsFromString(String skillsString) {
        if (skillsString != null && !skillsString.isEmpty()) {
            this.skills = new HashSet<>(Arrays.asList(skillsString.split(",")));
        } else {
            this.skills = new HashSet<>();
        }
    }
}
