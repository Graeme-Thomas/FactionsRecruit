package com.gnut.factionsrecruit;

import org.bukkit.entity.Player;

import java.util.UUID;

public class RecruitmentRequest {
    private final UUID recruiterUUID;
    private final UUID recruitUUID;
    private final String factionId; // Faction ID of the recruiter's faction
    private final long creationTime;

    public RecruitmentRequest(UUID recruiterUUID, UUID recruitUUID, String factionId) {
        this.recruiterUUID = recruiterUUID;
        this.recruitUUID = recruitUUID;
        this.factionId = factionId;
        this.creationTime = System.currentTimeMillis();
    }

    public UUID getRecruiterUUID() {
        return recruiterUUID;
    }

    public UUID getRecruitUUID() {
        return recruitUUID;
    }

    public String getFactionId() {
        return factionId;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
