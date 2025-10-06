package com.gnut.factionsrecruit.model;

import java.util.UUID;

public class EditCooldown {
    private UUID uuid;
    private long lastResumeEdit;
    private long lastApplicationEdit;

    public EditCooldown(UUID uuid, long lastResumeEdit, long lastApplicationEdit) {
        this.uuid = uuid;
        this.lastResumeEdit = lastResumeEdit;
        this.lastApplicationEdit = lastApplicationEdit;
    }

    // Getters
    public UUID getUuid() { return uuid; }
    public long getLastResumeEdit() { return lastResumeEdit; }
    public long getLastApplicationEdit() { return lastApplicationEdit; }

    // Setters
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public void setLastResumeEdit(long lastResumeEdit) { this.lastResumeEdit = lastResumeEdit; }
    public void setLastApplicationEdit(long lastApplicationEdit) { this.lastApplicationEdit = lastApplicationEdit; }
}
