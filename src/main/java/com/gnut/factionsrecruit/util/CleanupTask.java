package com.gnut.factionsrecruit.util;

import org.bukkit.scheduler.BukkitRunnable;

import com.gnut.factionsrecruit.FactionsRecruit;

public class CleanupTask extends BukkitRunnable {

    private final FactionsRecruit plugin;

    public CleanupTask(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getLogger().info("Running automatic cleanup tasks...");

        // 1. Expire applications and set notification flags
        plugin.getDatabaseManager().expireApplicationsWithNotifications();

        // 2. Expire invitations and set notification flags
        plugin.getDatabaseManager().expireInvitationsWithNotifications();

        // 3. Free expired application slots and notify players
        plugin.getDatabaseManager().freeExpiredSlots();

        // 4. Hide expired resumes
        plugin.getDatabaseManager().hideExpiredResumes();

        // 5. Clean old data (30+ days)
        plugin.getDatabaseManager().cleanOldData();

        plugin.getLogger().info("Automatic cleanup tasks completed.");
    }
}
