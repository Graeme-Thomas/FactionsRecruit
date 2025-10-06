package com.gnut.factionsrecruit.model;

import java.util.UUID;

public class FactionInvitation {
    private int id;
    private String factionId;
    private UUID playerUuid;
    private UUID invitedBy;
    private long createdAt;
    private long expiresAt;
    private String status;

    public FactionInvitation(int id, String factionId, UUID playerUuid, UUID invitedBy, long createdAt, long expiresAt, String status) {
        this.id = id;
        this.factionId = factionId;
        this.playerUuid = playerUuid;
        this.invitedBy = invitedBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getFactionId() { return factionId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public UUID getInvitedBy() { return invitedBy; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFactionId(String factionId) { this.factionId = factionId; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }
    public void setInvitedBy(UUID invitedBy) { this.invitedBy = invitedBy; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public void setStatus(String status) { this.status = status; }
}
