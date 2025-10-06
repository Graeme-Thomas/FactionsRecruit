package com.gnut.factionsrecruit.model;

import java.util.UUID;

public class LoginNotification {
    private UUID uuid;
    private boolean hasExpiredApplications;
    private boolean hasNewInvitations;
    private boolean hasAvailableSlots;
    private boolean hasNewApplications;
    private boolean hasAcceptedApplications;
    private boolean hasRejectedApplications;
    private long lastChecked;

    public LoginNotification(UUID uuid, boolean hasExpiredApplications, boolean hasNewInvitations, boolean hasAvailableSlots, boolean hasNewApplications, boolean hasAcceptedApplications, boolean hasRejectedApplications, long lastChecked) {
        this.uuid = uuid;
        this.hasExpiredApplications = hasExpiredApplications;
        this.hasNewInvitations = hasNewInvitations;
        this.hasAvailableSlots = hasAvailableSlots;
        this.hasNewApplications = hasNewApplications;
        this.hasAcceptedApplications = hasAcceptedApplications;
        this.hasRejectedApplications = hasRejectedApplications;
        this.lastChecked = lastChecked;
    }

    // Getters
    public UUID getUuid() { return uuid; }
    public boolean hasExpiredApplications() { return hasExpiredApplications; }
    public boolean hasNewInvitations() { return hasNewInvitations; }
    public boolean hasAvailableSlots() { return hasAvailableSlots; }
    public boolean hasNewApplications() { return hasNewApplications; }
    public boolean hasAcceptedApplications() { return hasAcceptedApplications; }
    public boolean hasRejectedApplications() { return hasRejectedApplications; }
    public long getLastChecked() { return lastChecked; }

    // Setters
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public void setHasExpiredApplications(boolean hasExpiredApplications) { this.hasExpiredApplications = hasExpiredApplications; }
    public void setHasNewInvitations(boolean hasNewInvitations) { this.hasNewInvitations = hasNewInvitations; }
    public void setHasAvailableSlots(boolean hasAvailableSlots) { this.hasAvailableSlots = hasAvailableSlots; }
    public void setHasNewApplications(boolean hasNewApplications) { this.hasNewApplications = hasNewApplications; }
    public void setHasAcceptedApplications(boolean hasAcceptedApplications) { this.hasAcceptedApplications = hasAcceptedApplications; }
    public void setHasRejectedApplications(boolean hasRejectedApplications) { this.hasRejectedApplications = hasRejectedApplications; }
    public void setLastChecked(long lastChecked) { this.lastChecked = lastChecked; }
}