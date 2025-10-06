package com.gnut.factionsrecruit;

import java.util.UUID;

public class PlayerApplication {
    private int id;
    private UUID playerUuid;
    private String factionId;
    private long applicationDate;
    private long expiresAt;
    private long slotAvailableAt;
    private String status;

    public PlayerApplication(int id, UUID playerUuid, String factionId, long applicationDate, long expiresAt, long slotAvailableAt, String status) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.factionId = factionId;
        this.applicationDate = applicationDate;
        this.expiresAt = expiresAt;
        this.slotAvailableAt = slotAvailableAt;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getFactionId() { return factionId; }
    public long getApplicationDate() { return applicationDate; }
    public long getExpiresAt() { return expiresAt; }
    public long getSlotAvailableAt() { return slotAvailableAt; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }
    public void setFactionId(String factionId) { this.factionId = factionId; }
    public void setApplicationDate(long applicationDate) { this.applicationDate = applicationDate; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public void setSlotAvailableAt(long slotAvailableAt) { this.slotAvailableAt = slotAvailableAt; }
    public void setStatus(String status) { this.status = status; }
}
