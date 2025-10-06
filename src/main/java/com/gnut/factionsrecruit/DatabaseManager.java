package com.gnut.factionsrecruit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import org.bukkit.Bukkit;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.PlayerResume;
import com.gnut.factionsrecruit.ConfigManager;

public class DatabaseManager {

    private final FactionsRecruit plugin;
    private final ConfigManager configManager;
    private Connection connection;

    public DatabaseManager(FactionsRecruit plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        initializeDatabase();
    }

    private String formatSetValue(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        // Simple formatting for MySQL SET - values should already be database keys
        return String.join(",", values);
    }

    private List<String> parseSetValue(String setValue) {
        if (setValue == null || setValue.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(setValue.split(","));
    }

    private void initializeDatabase() {
        plugin.getLogger().info("Starting database initialization...");
        try {
            plugin.getLogger().info("Attempting to load JDBC driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            plugin.getLogger().info("JDBC driver loaded.");

            String connectionString = configManager.getDatabaseConnectionString();

            plugin.getLogger().info("Attempting to establish database connection...");
            connection = DriverManager.getConnection(connectionString);
            plugin.getLogger().info("MySQL database connected.");
            plugin.getLogger().info("Attempting to create tables...");
            createTables();
            plugin.getLogger().info("Database tables created or checked.");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL JDBC driver not found!", e);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database!", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // player_resumes table
            String playerResumesTable = "CREATE TABLE IF NOT EXISTS player_resumes (" +
                    "    uuid VARCHAR(36) PRIMARY KEY," +
                    "    timezone ENUM('NA_WEST','NA_EAST','EU_WEST','EU_CENTRAL','ASIA','OCEANIA')," +
                    "    experience ENUM('UNDER_6MO','1_YEAR','1_2_YEARS','2_3_YEARS','3_4_YEARS','4_5_YEARS','5_PLUS_YEARS')," +
                    "    available_days SET('MON','TUE','WED','THU','FRI','SAT','SUN')," +
                    "    skills SET('CANNON','PVP','DEFENSE','DESIGN','REDSTONE','FARM','FISH')," +
                    "    is_looking BOOLEAN DEFAULT FALSE," +
                    "    display_until TIMESTAMP NULL," +
                    "    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "    is_hidden BOOLEAN DEFAULT FALSE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            statement.execute(playerResumesTable);

            // Add is_hidden column if it doesn't exist
            if (!columnExists("player_resumes", "is_hidden")) {
                statement.execute("ALTER TABLE player_resumes ADD COLUMN is_hidden BOOLEAN DEFAULT FALSE;");
                plugin.getLogger().info("Added 'is_hidden' column to 'player_resumes' table.");
            }

            // faction_applications table
            String factionApplicationsTable = "CREATE TABLE IF NOT EXISTS faction_applications (" +
                    "    faction_id VARCHAR(50) PRIMARY KEY," +
                    "    faction_name VARCHAR(100)," +
                    "    leader_uuid VARCHAR(36)," +
                    "    desired_timezones SET('NA_WEST','NA_EAST','EU_WEST','EU_CENTRAL','ASIA','OCEANIA')," +
                    "    experience_levels SET('UNDER_6MO','1_YEAR','1_2_YEARS','2_3_YEARS','3_4_YEARS','4_5_YEARS','5_PLUS_YEARS')," +
                    "    required_days SET('MON','TUE','WED','THU','FRI','SAT','SUN')," +
                    "    desired_skills SET('CANNON','PVP','DEFENSE','DESIGN','REDSTONE','FARM','FISH')," +
                    "    is_accepting BOOLEAN DEFAULT TRUE," +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            statement.execute(factionApplicationsTable);
//[16:33:03 ERROR]: [FactionsRecruit] Connection error details: Function or expression 'processed_by' cannot be used in the CHECK clause of `chk_proc_logic`

            // recruitment_requests table
            String recruitmentRequestsTable = "CREATE TABLE IF NOT EXISTS recruitment_requests (" +
                    "    id INT PRIMARY KEY AUTO_INCREMENT," +
                    "    player_uuid VARCHAR(36)," +
                    "    faction_id VARCHAR(50)," +
                    "    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "    expires_at TIMESTAMP," +
                    "    slot_available_at TIMESTAMP," +
                    "    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING'," +
                    "    INDEX idx_player_uuid (player_uuid)," +
                    "    INDEX idx_faction_id (faction_id)," +
                    "    INDEX idx_expires_at (expires_at)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            statement.execute(recruitmentRequestsTable);

            // faction_invitations table
            String factionInvitationsTable = "CREATE TABLE IF NOT EXISTS faction_invitations (" +
                    "    id INT PRIMARY KEY AUTO_INCREMENT," +
                    "    faction_id VARCHAR(50)," +
                    "    player_uuid VARCHAR(36)," +
                    "    invited_by VARCHAR(36)," +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "    expires_at TIMESTAMP," +
                    "    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') DEFAULT 'PENDING'," +
                    "    INDEX idx_player_uuid (player_uuid)," +
                    "    INDEX idx_expires_at (expires_at)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            statement.execute(factionInvitationsTable);

            // edit_cooldowns table
            String editCooldownsTable = "CREATE TABLE IF NOT EXISTS edit_cooldowns (" +
                    "    uuid VARCHAR(36) PRIMARY KEY," +
                    "    last_resume_edit TIMESTAMP," +
                    "    last_application_edit TIMESTAMP," +
                    "    INDEX idx_resume_edit (last_resume_edit)," +
                    "    INDEX idx_application_edit (last_application_edit)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            statement.execute(editCooldownsTable);

            // login_notifications table
            String loginNotificationsTable = "CREATE TABLE IF NOT EXISTS login_notifications (" +
                    "    uuid VARCHAR(36) PRIMARY KEY," +
                    "    has_expired_applications BOOLEAN DEFAULT FALSE," +
                    "    has_new_invitations BOOLEAN DEFAULT FALSE," +
                    "    has_available_slots BOOLEAN DEFAULT FALSE," +
                    "    has_new_applications BOOLEAN DEFAULT FALSE," +
                    "    has_accepted_applications BOOLEAN DEFAULT FALSE," +
                    "    has_rejected_applications BOOLEAN DEFAULT FALSE," +
                    "    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            statement.execute(loginNotificationsTable);

            // Add Indexes for performance
            if (!indexExists("player_resumes", "idx_player_resumes_looking")) {
                statement.execute("CREATE INDEX idx_player_resumes_looking ON player_resumes(is_looking, display_until);");
            }
            if (!indexExists("faction_applications", "idx_faction_applications_active")) {
                statement.execute("CREATE INDEX idx_faction_applications_active ON faction_applications(is_accepting);");
            }
            if (!indexExists("recruitment_requests", "idx_applications_status_expiry")) {
                statement.execute("CREATE INDEX idx_applications_status_expiry ON recruitment_requests(status, expires_at);");
            }
            if (!indexExists("faction_invitations", "idx_invitations_status_expiry")) {
                statement.execute("CREATE INDEX idx_invitations_status_expiry ON faction_invitations(status, expires_at);");
            }

            plugin.getLogger().info("Database tables created or checked.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("MySQL database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL database connection!", e);
        }
    }

    private boolean indexExists(String tableName, String indexName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, indexName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void savePlayerResume(PlayerResume resume) {
        String sql = "INSERT INTO player_resumes (uuid, timezone, experience, available_days, skills, is_looking, display_until, last_updated, created_at, is_hidden) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE timezone = ?, experience = ?, available_days = ?, skills = ?, is_looking = ?, display_until = ?, last_updated = ?, is_hidden = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, resume.getPlayerUUID().toString());
            ps.setString(2, resume.getTimezone());
            ps.setString(3, resume.getExperience());
            ps.setString(4, resume.getAvailableDaysAsString());
            ps.setString(5, resume.getSkillsAsString());
            ps.setBoolean(6, resume.isLooking());
            if (resume.getDisplayUntil() == 0) {
                ps.setNull(7, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(7, new java.sql.Timestamp(resume.getDisplayUntil()));
            }
            ps.setTimestamp(8, new java.sql.Timestamp(resume.getLastUpdated()));
            ps.setTimestamp(9, new java.sql.Timestamp(resume.getCreatedAt()));
            ps.setBoolean(10, resume.isHidden());

            // For ON DUPLICATE KEY UPDATE
            ps.setString(11, resume.getTimezone());
            ps.setString(12, resume.getExperience());
            ps.setString(13, resume.getAvailableDaysAsString());
            ps.setString(14, resume.getSkillsAsString());
            ps.setBoolean(15, resume.isLooking());
            if (resume.getDisplayUntil() == 0) {
                ps.setNull(16, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(16, new java.sql.Timestamp(resume.getDisplayUntil()));
            }
            ps.setTimestamp(17, new java.sql.Timestamp(resume.getLastUpdated()));
            ps.setBoolean(18, resume.isHidden());

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player resume: " + resume.getPlayerUUID(), e);
        }
    }

    public PlayerResume getPlayerResume(UUID playerUuid) {
        String sql = "SELECT * FROM player_resumes WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                PlayerResume resume = new PlayerResume(playerUuid);
                resume.setTimezone(rs.getString("timezone"));
                resume.setExperience(rs.getString("experience"));
                resume.setAvailableDaysFromString(rs.getString("available_days"));
                resume.setSkillsFromString(rs.getString("skills"));
                resume.setLooking(rs.getBoolean("is_looking"));
                java.sql.Timestamp displayUntil = rs.getTimestamp("display_until");
                resume.setDisplayUntil(displayUntil == null ? 0 : displayUntil.getTime());
                java.sql.Timestamp lastUpdated = rs.getTimestamp("last_updated");
                resume.setLastUpdated(lastUpdated == null ? 0 : lastUpdated.getTime());
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                resume.setCreatedAt(createdAt == null ? 0 : createdAt.getTime());
                resume.setHidden(rs.getBoolean("is_hidden")); // New line
                return resume;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get player resume: " + playerUuid, e);
        }
        return null;
    }

    public void deletePlayerResume(UUID playerUuid) {
        String sql = "DELETE FROM player_resumes WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete player resume: " + playerUuid, e);
        }
    }

    // FactionApplication CRUD
    public void saveFactionApplication(FactionApplication application) {
        String sql = "INSERT INTO faction_applications (faction_id, faction_name, leader_uuid, desired_timezones, experience_levels, required_days, desired_skills, is_accepting, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE faction_name = ?, leader_uuid = ?, desired_timezones = ?, experience_levels = ?, required_days = ?, desired_skills = ?, is_accepting = ?, updated_at = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, application.getFactionId());
            ps.setString(2, application.getFactionName());
            ps.setString(3, application.getLeaderUuid().toString());
            ps.setString(4, formatSetValue(application.getDesiredTimezones()));
            ps.setString(5, formatSetValue(application.getExperienceLevels()));
            ps.setString(6, formatSetValue(application.getRequiredDays()));
            ps.setString(7, formatSetValue(application.getDesiredSkills()));
            ps.setBoolean(8, application.isAccepting());
            ps.setTimestamp(9, new java.sql.Timestamp(application.getCreatedAt()));
            ps.setTimestamp(10, new java.sql.Timestamp(application.getUpdatedAt()));

            // For ON DUPLICATE KEY UPDATE
            ps.setString(11, application.getFactionName());
            ps.setString(12, application.getLeaderUuid().toString());
            ps.setString(13, formatSetValue(application.getDesiredTimezones()));
            ps.setString(14, formatSetValue(application.getExperienceLevels()));
            ps.setString(15, formatSetValue(application.getRequiredDays()));
            ps.setString(16, formatSetValue(application.getDesiredSkills()));
            ps.setBoolean(17, application.isAccepting());
            ps.setTimestamp(18, new java.sql.Timestamp(application.getUpdatedAt()));

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save faction application: " + application.getFactionId(), e);
        }
    }

    public FactionApplication getFactionApplication(String factionId) {
        String sql = "SELECT * FROM faction_applications WHERE faction_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, factionId);

            // Debug logging
            if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] Querying for faction application with ID: '" + factionId + "'");
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String desiredTimezones = rs.getString("desired_timezones");
                String experienceLevels = rs.getString("experience_levels");
                String requiredDays = rs.getString("required_days");
                String desiredSkills = rs.getString("desired_skills");

                FactionApplication result = new FactionApplication(
                        rs.getString("faction_id"),
                        rs.getString("faction_name"),
                        UUID.fromString(rs.getString("leader_uuid")),
                        parseSetValue(desiredTimezones),
                        parseSetValue(experienceLevels),
                        parseSetValue(requiredDays),
                        parseSetValue(desiredSkills),
                        rs.getBoolean("is_accepting"),
                        rs.getTimestamp("created_at").getTime(),
                        rs.getTimestamp("updated_at").getTime()
                );

                // Debug logging
                if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] Found faction application: " + result.getFactionName() + " (ID: " + result.getFactionId() + ")");
                }

                return result;
            } else {
                // Debug logging for not found
                if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                    plugin.getLogger().warning("[DEBUG] No faction application found for ID: '" + factionId + "'");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get faction application: " + factionId, e);
        }
        return null;
    }

    public void deleteFactionApplication(String factionId) {
        String sql = "DELETE FROM faction_applications WHERE faction_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, factionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete faction application: " + factionId, e);
        }
    }

    // PlayerApplication CRUD
    public void savePlayerApplication(PlayerApplication application) {
        String sql = "INSERT INTO recruitment_requests (player_uuid, faction_id, application_date, expires_at, slot_available_at, status) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE player_uuid = ?, faction_id = ?, application_date = ?, expires_at = ?, slot_available_at = ?, status = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, application.getPlayerUuid().toString());
            ps.setString(2, application.getFactionId());
            ps.setTimestamp(3, new java.sql.Timestamp(application.getApplicationDate()));
            ps.setTimestamp(4, new java.sql.Timestamp(application.getExpiresAt()));
            if (application.getSlotAvailableAt() == 0) {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(5, new java.sql.Timestamp(application.getSlotAvailableAt()));
            }
            ps.setString(6, application.getStatus());

            // For ON DUPLICATE KEY UPDATE
            ps.setString(7, application.getPlayerUuid().toString());
            ps.setString(8, application.getFactionId());
            ps.setTimestamp(9, new java.sql.Timestamp(application.getApplicationDate()));
            ps.setTimestamp(10, new java.sql.Timestamp(application.getExpiresAt()));
            ps.setTimestamp(11, new java.sql.Timestamp(application.getSlotAvailableAt()));
            ps.setString(12, application.getStatus());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating player application failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    application.setId(generatedKeys.getInt(1));
                }
                else {
                    throw new SQLException("Creating player application failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player application: " + application.getPlayerUuid(), e);
        }
    }

    public PlayerApplication getPlayerApplication(int id) {
        String sql = "SELECT * FROM recruitment_requests WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new PlayerApplication(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("faction_id"),
                        rs.getTimestamp("application_date").getTime(),
                        rs.getTimestamp("expires_at") == null ? 0L : rs.getTimestamp("expires_at").getTime(),
                        rs.getTimestamp("slot_available_at") == null ? 0L : rs.getTimestamp("slot_available_at").getTime(),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get player application: " + id, e);
        }
        return null;
    }

    public List<PlayerApplication> getPlayerApplicationsByPlayer(UUID playerUuid) {
        List<PlayerApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM recruitment_requests WHERE player_uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                applications.add(new PlayerApplication(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("faction_id"),
                        rs.getTimestamp("application_date").getTime(),
                        rs.getTimestamp("expires_at") == null ? 0L : rs.getTimestamp("expires_at").getTime(),
                        rs.getTimestamp("slot_available_at") == null ? 0L : rs.getTimestamp("slot_available_at").getTime(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get player applications for player: " + playerUuid, e);
        }
        return applications;
    }

    public List<PlayerApplication> getPlayerApplicationsByFaction(String factionId) {
        List<PlayerApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM recruitment_requests WHERE faction_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, factionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                applications.add(new PlayerApplication(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("faction_id"),
                        rs.getTimestamp("application_date").getTime(),
                        rs.getTimestamp("expires_at") == null ? 0L : rs.getTimestamp("expires_at").getTime(),
                        rs.getTimestamp("slot_available_at") == null ? 0L : rs.getTimestamp("slot_available_at").getTime(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get player applications for faction: " + factionId, e);
        }
        return applications;
    }

    public void deletePlayerApplication(int id) {
        String sql = "DELETE FROM recruitment_requests WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete player application: " + id, e);
        }
    }

    // FactionInvitation CRUD
    public void saveFactionInvitation(FactionInvitation invitation) {
        String sql = "INSERT INTO faction_invitations (faction_id, player_uuid, invited_by, created_at, expires_at, status) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE faction_id = ?, player_uuid = ?, invited_by = ?, created_at = ?, expires_at = ?, status = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invitation.getFactionId());
            ps.setString(2, invitation.getPlayerUuid().toString());
            ps.setString(3, invitation.getInvitedBy().toString());
            ps.setTimestamp(4, new java.sql.Timestamp(invitation.getCreatedAt()));
            ps.setTimestamp(5, new java.sql.Timestamp(invitation.getExpiresAt()));
            ps.setString(6, invitation.getStatus());

            // For ON DUPLICATE KEY UPDATE
            ps.setString(7, invitation.getFactionId());
            ps.setString(8, invitation.getPlayerUuid().toString());
            ps.setString(9, invitation.getInvitedBy().toString());
            ps.setTimestamp(10, new java.sql.Timestamp(invitation.getCreatedAt()));
            ps.setTimestamp(11, new java.sql.Timestamp(invitation.getExpiresAt()));
            ps.setString(12, invitation.getStatus());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating faction invitation failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    invitation.setId(generatedKeys.getInt(1));
                }
                else {
                    throw new SQLException("Creating faction invitation failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save faction invitation: " + invitation.getId(), e);
        }
    }

    public FactionInvitation getFactionInvitation(int id) {
        String sql = "SELECT * FROM faction_invitations WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FactionInvitation(
                        rs.getInt("id"),
                        rs.getString("faction_id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("invited_by")),
                        rs.getTimestamp("created_at").getTime(),
                        rs.getTimestamp("expires_at").getTime(),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get faction invitation: " + id, e);
        }
        return null;
    }

    public List<FactionInvitation> getFactionInvitationsByPlayer(UUID playerUuid) {
        List<FactionInvitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM faction_invitations WHERE player_uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                invitations.add(new FactionInvitation(
                        rs.getInt("id"),
                        rs.getString("faction_id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("invited_by")),
                        rs.getLong("created_at"),
                        rs.getLong("expires_at"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get faction invitations for player: " + playerUuid, e);
        }
        return invitations;
    }

    public List<FactionInvitation> getFactionInvitationsByFaction(String factionId) {
        List<FactionInvitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM faction_invitations WHERE faction_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, factionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                invitations.add(new FactionInvitation(
                        rs.getInt("id"),
                        rs.getString("faction_id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("invited_by")),
                        rs.getLong("created_at"),
                        rs.getLong("expires_at"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get faction invitations for faction: " + factionId, e);
        }
        return invitations;
    }

    public void deleteFactionInvitation(int id) {
        String sql = "DELETE FROM faction_invitations WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete faction invitation: " + id, e);
        }
    }

    public FactionInvitation getPendingFactionInvitation(UUID playerUuid) {
        String sql = "SELECT * FROM faction_invitations WHERE player_uuid = ? AND status = 'PENDING' ORDER BY created_at DESC LIMIT 1;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new FactionInvitation(
                        rs.getInt("id"),
                        rs.getString("faction_id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("invited_by")),
                        rs.getLong("created_at"),
                        rs.getLong("expires_at") == 0 ? -1 : rs.getLong("expires_at"), // Handle 0 as -1 for no expiry
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get pending faction invitation for player: " + playerUuid, e);
        }
        return null;
    }

    // EditCooldown CRUD
    public void saveEditCooldown(EditCooldown cooldown) {
        String sql = "INSERT INTO edit_cooldowns (uuid, last_resume_edit, last_application_edit) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE last_resume_edit = ?, last_application_edit = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cooldown.getUuid().toString());
            ps.setTimestamp(2, new java.sql.Timestamp(cooldown.getLastResumeEdit()));
            ps.setTimestamp(3, new java.sql.Timestamp(cooldown.getLastApplicationEdit()));

            // For ON DUPLICATE KEY UPDATE
            ps.setTimestamp(4, new java.sql.Timestamp(cooldown.getLastResumeEdit()));
            ps.setTimestamp(5, new java.sql.Timestamp(cooldown.getLastApplicationEdit()));

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save edit cooldown for player: " + cooldown.getUuid(), e);
        }
    }

    public EditCooldown getEditCooldown(UUID uuid) {
        String sql = "SELECT * FROM edit_cooldowns WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new EditCooldown(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getTimestamp("last_resume_edit").getTime(),
                        rs.getTimestamp("last_application_edit").getTime()
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get edit cooldown for player: " + uuid, e);
        }
        return null;
    }

    // LoginNotification CRUD
    public void saveLoginNotification(LoginNotification notification) {
        String sql = "INSERT INTO login_notifications (uuid, has_expired_applications, has_new_invitations, has_available_slots, has_new_applications, has_accepted_applications, has_rejected_applications, last_checked) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE has_expired_applications = ?, has_new_invitations = ?, has_available_slots = ?, has_new_applications = ?, has_accepted_applications = ?, has_rejected_applications = ?, last_checked = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, notification.getUuid().toString());
            ps.setBoolean(2, notification.hasExpiredApplications());
            ps.setBoolean(3, notification.hasNewInvitations());
            ps.setBoolean(4, notification.hasAvailableSlots());
            ps.setBoolean(5, notification.hasNewApplications());
            ps.setBoolean(6, notification.hasAcceptedApplications());
            ps.setBoolean(7, notification.hasRejectedApplications());
            ps.setTimestamp(8, new java.sql.Timestamp(notification.getLastChecked()));

            // For ON DUPLICATE KEY UPDATE
            ps.setBoolean(9, notification.hasExpiredApplications());
            ps.setBoolean(10, notification.hasNewInvitations());
            ps.setBoolean(11, notification.hasAvailableSlots());
            ps.setBoolean(12, notification.hasNewApplications());
            ps.setBoolean(13, notification.hasAcceptedApplications());
            ps.setBoolean(14, notification.hasRejectedApplications());
            ps.setTimestamp(15, new java.sql.Timestamp(notification.getLastChecked()));

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save login notification for player: " + notification.getUuid(), e);
        }
    }

    public LoginNotification getLoginNotification(UUID uuid) {
        String sql = "SELECT * FROM login_notifications WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new LoginNotification(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getBoolean("has_expired_applications"),
                        rs.getBoolean("has_new_invitations"),
                        rs.getBoolean("has_available_slots"),
                        rs.getBoolean("has_new_applications"),
                        rs.getBoolean("has_accepted_applications"),
                        rs.getBoolean("has_rejected_applications"),
                        rs.getTimestamp("last_checked").getTime()
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get login notification for player: " + uuid, e);
        }
        return null;
    }

    public int countPendingPlayerApplications(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM recruitment_requests WHERE player_uuid = ? AND status = 'PENDING';";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to count pending applications for player: " + playerUuid, e);
        }
        return 0;
    }

    public List<PlayerResume> getLookingPlayers(FactionApplication filter) {
        List<PlayerResume> resumes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM player_resumes WHERE is_looking = 1 AND is_hidden = FALSE");

        if (filter != null) {
            if (!filter.getDesiredTimezones().isEmpty()) {
                sql.append(" AND timezone IN (");
                for (int i = 0; i < filter.getDesiredTimezones().size(); i++) {
                    sql.append("?");
                    if (i < filter.getDesiredTimezones().size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }
            if (!filter.getExperienceLevels().isEmpty()) {
                sql.append(" AND experience IN (");
                for (int i = 0; i < filter.getExperienceLevels().size(); i++) {
                    sql.append("?");
                    if (i < filter.getExperienceLevels().size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }
            if (!filter.getRequiredDays().isEmpty()) {
                sql.append(" AND (");
                for (int i = 0; i < filter.getRequiredDays().size(); i++) {
                    sql.append("FIND_IN_SET(?, available_days)");
                    if (i < filter.getRequiredDays().size() - 1) {
                        sql.append(" OR ");
                    }
                }
                sql.append(")");
            }
            if (!filter.getDesiredSkills().isEmpty()) {
                sql.append(" AND (");
                for (int i = 0; i < filter.getDesiredSkills().size(); i++) {
                    sql.append("FIND_IN_SET(?, skills)");
                    if (i < filter.getDesiredSkills().size() - 1) {
                        sql.append(" OR ");
                    }
                }
                sql.append(")");
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (filter != null) {
                if (!filter.getDesiredTimezones().isEmpty()) {
                    for (String timezone : filter.getDesiredTimezones()) {
                        ps.setString(paramIndex++, timezone);
                    }
                }
                if (!filter.getExperienceLevels().isEmpty()) {
                    for (String experience : filter.getExperienceLevels()) {
                        ps.setString(paramIndex++, experience);
                    }
                }
                if (!filter.getRequiredDays().isEmpty()) {
                    for (String day : filter.getRequiredDays()) {
                        ps.setString(paramIndex++, day);
                    }
                }
                if (!filter.getDesiredSkills().isEmpty()) {
                    for (String skill : filter.getDesiredSkills()) {
                        ps.setString(paramIndex++, skill);
                    }
                }
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PlayerResume resume = new PlayerResume(UUID.fromString(rs.getString("uuid")));
                resume.setTimezone(rs.getString("timezone"));
                resume.setExperience(rs.getString("experience"));
                resume.setAvailableDaysFromString(rs.getString("available_days"));
                resume.setSkillsFromString(rs.getString("skills"));
                resume.setLooking(rs.getBoolean("is_looking"));
                java.sql.Timestamp displayUntil = rs.getTimestamp("display_until");
                resume.setDisplayUntil(displayUntil == null ? 0 : displayUntil.getTime());
                java.sql.Timestamp lastUpdated = rs.getTimestamp("last_updated");
                resume.setLastUpdated(lastUpdated == null ? 0 : lastUpdated.getTime());
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                resume.setCreatedAt(createdAt == null ? 0 : createdAt.getTime());
                resume.setHidden(rs.getBoolean("is_hidden"));
                resumes.add(resume);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get looking players: ", e);
        }
        return resumes;
    }

    public List<FactionApplication> getRecruitingFactions(FactionApplication filter) {
        List<FactionApplication> applications = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM faction_applications WHERE is_accepting = 1");

        if (filter != null) {
            if (!filter.getDesiredTimezones().isEmpty()) {
                sql.append(" AND (");
                for (int i = 0; i < filter.getDesiredTimezones().size(); i++) {
                    sql.append("FIND_IN_SET(?, desired_timezones)");
                    if (i < filter.getDesiredTimezones().size() - 1) {
                        sql.append(" OR ");
                    }
                }
                sql.append(")");
            }
            if (!filter.getExperienceLevels().isEmpty()) {
                sql.append(" AND (");
                for (int i = 0; i < filter.getExperienceLevels().size(); i++) {
                    sql.append("FIND_IN_SET(?, experience_levels)");
                    if (i < filter.getExperienceLevels().size() - 1) {
                        sql.append(" OR ");
                    }
                }
                sql.append(")");
            }
            if (!filter.getRequiredDays().isEmpty()) {
                sql.append(" AND (");
                for (int i = 0; i < filter.getRequiredDays().size(); i++) {
                    sql.append("FIND_IN_SET(?, required_days)");
                    if (i < filter.getRequiredDays().size() - 1) {
                        sql.append(" OR ");
                    }
                }
                sql.append(")");
            }
            if (!filter.getDesiredSkills().isEmpty()) {
                sql.append(" AND (");
                for (int i = 0; i < filter.getDesiredSkills().size(); i++) {
                    sql.append("FIND_IN_SET(?, desired_skills)");
                    if (i < filter.getDesiredSkills().size() - 1) {
                        sql.append(" OR ");
                    }
                }
                sql.append(")");
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (filter != null) {
                if (!filter.getDesiredTimezones().isEmpty()) {
                    for (String timezone : filter.getDesiredTimezones()) {
                        ps.setString(paramIndex++, timezone);
                    }
                }
                if (!filter.getExperienceLevels().isEmpty()) {
                    for (String experience : filter.getExperienceLevels()) {
                        ps.setString(paramIndex++, experience);
                    }
                }
                if (!filter.getRequiredDays().isEmpty()) {
                    for (String day : filter.getRequiredDays()) {
                        ps.setString(paramIndex++, day);
                    }
                }
                if (!filter.getDesiredSkills().isEmpty()) {
                    for (String skill : filter.getDesiredSkills()) {
                        ps.setString(paramIndex++, skill);
                    }
                }
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String factionId = rs.getString("faction_id");
                UUID leaderUuid = UUID.fromString(rs.getString("leader_uuid"));

                // Check if faction has reached member limit (30)
                int memberLimit = plugin.getConfigManager().getFactionMemberLimit();
                int currentMembers = plugin.getPapiIntegrationManager().getFactionSize(Bukkit.getOfflinePlayer(leaderUuid));

                // Skip factions that have reached their member limit
                if (currentMembers >= memberLimit) {
                    continue;
                }

                String desiredTimezones = rs.getString("desired_timezones");
                String experienceLevels = rs.getString("experience_levels");
                String requiredDays = rs.getString("required_days");
                String desiredSkills = rs.getString("desired_skills");

                applications.add(new FactionApplication(
                        factionId,
                        rs.getString("faction_name"),
                        leaderUuid,
                        parseSetValue(desiredTimezones),
                        parseSetValue(experienceLevels),
                        parseSetValue(requiredDays),
                        parseSetValue(desiredSkills),
                        rs.getBoolean("is_accepting"),
                        rs.getTimestamp("created_at").getTime(),
                        rs.getTimestamp("updated_at").getTime()
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get recruiting factions: ", e);
        }
        return applications;
    }

    public boolean hasExpiredApplications(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM recruitment_requests WHERE player_uuid = ? AND status = 'EXPIRED';";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check for expired applications for player: " + playerUuid, e);
        }
        return false;
    }

    public boolean hasNewInvitations(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM faction_invitations WHERE player_uuid = ? AND status = 'PENDING';";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check for new invitations for player: " + playerUuid, e);
        }
        return false;
    }

    public int getAvailableApplicationSlots(UUID playerUuid, int maxSlots) {
        int activeSlots = getActiveApplicationSlots(playerUuid);
        return Math.max(0, maxSlots - activeSlots);
    }

    public int getActiveApplicationSlots(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM recruitment_requests WHERE player_uuid = ? AND (slot_available_at > ? OR slot_available_at IS NULL);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get active application slots for player: " + playerUuid, e);
        }
        return 0;
    }

    public boolean hasRecentApplicationToFaction(UUID playerUuid, String factionId, long cooldownMillis) {
        String sql = "SELECT COUNT(*) FROM recruitment_requests WHERE player_uuid = ? AND faction_id = ? AND application_date > ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, factionId);
            ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis() - cooldownMillis));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check recent applications for player: " + playerUuid + " to faction: " + factionId, e);
        }
        return false;
    }

    public boolean isResumeExpired(UUID playerUuid) {
        String sql = "SELECT display_until FROM player_resumes WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long displayUntil = rs.getLong("display_until");
                return displayUntil != 0 && System.currentTimeMillis() > displayUntil;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check if resume is expired for player: " + playerUuid, e);
        }
        return false; // Assume not expired if no resume or error
    }

    public List<PlayerResume> searchPlayers(String name) {
        List<PlayerResume> resumes = new ArrayList<>();
        String lowerCaseName = name.toLowerCase();

        String sql = "SELECT uuid FROM player_resumes WHERE is_looking = 1 AND is_hidden = FALSE;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UUID playerUuid = UUID.fromString(rs.getString("uuid"));
                // Get player name from UUID (can be offline player)
                String playerName = Bukkit.getOfflinePlayer(playerUuid).getName();

                if (playerName != null && playerName.toLowerCase().contains(lowerCaseName)) {
                    PlayerResume resume = getPlayerResume(playerUuid);
                    if (resume != null) {
                        resumes.add(resume);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to search players: ", e);
        }
        return resumes;
    }

    public List<FactionApplication> searchFactions(String name) {
        List<FactionApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM faction_applications WHERE is_accepting = 1 AND LOWER(faction_name) LIKE LOWER(?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String desiredTimezones = rs.getString("desired_timezones");
                String experienceLevels = rs.getString("experience_levels");
                String requiredDays = rs.getString("required_days");
                String desiredSkills = rs.getString("desired_skills");

                applications.add(new FactionApplication(
                        rs.getString("faction_id"),
                        rs.getString("faction_name"),
                        UUID.fromString(rs.getString("leader_uuid")),
                        parseSetValue(desiredTimezones),
                        parseSetValue(experienceLevels),
                        parseSetValue(requiredDays),
                        parseSetValue(desiredSkills),
                        rs.getBoolean("is_accepting"),
                        rs.getTimestamp("created_at").getTime(),
                        rs.getTimestamp("updated_at").getTime()
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to search factions: ", e);
        }
        return applications;
    }

    public void expireApplications() {
        String sql = "UPDATE recruitment_requests SET status = 'EXPIRED' WHERE expires_at < ? AND status = 'PENDING';";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to expire applications: ", e);
        }
    }

    public void expireInvitations() {
        String sql = "UPDATE faction_invitations SET status = 'EXPIRED' WHERE expires_at < ? AND status = 'PENDING';";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to expire invitations: ", e);
        }
    }

    public void hideExpiredResumes() {
        String sql = "UPDATE player_resumes SET is_looking = 0 WHERE display_until < ? AND is_looking = 1;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to hide expired resumes: ", e);
        }
    }

    public void freeExpiredSlots() {
        // Find players whose slots have become available and set notification flags
        String findPlayersSQL = "SELECT DISTINCT player_uuid FROM recruitment_requests WHERE slot_available_at <= ? AND slot_available_at IS NOT NULL";

        try (PreparedStatement ps = connection.prepareStatement(findPlayersSQL)) {
            ps.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));

                // Set notification flag for this player
                LoginNotification notification = getLoginNotification(playerUuid);
                if (notification == null) {
                    notification = new LoginNotification(playerUuid, false, false, true, false, false, false, System.currentTimeMillis());
                } else {
                    notification.setHasAvailableSlots(true);
                }
                saveLoginNotification(notification);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to process expired slots: ", e);
        }
    }

    public void expireApplicationsWithNotifications() {
        // Find applications about to expire and set notification flags
        String findExpiredSQL = "SELECT DISTINCT player_uuid FROM recruitment_requests WHERE expires_at <= ? AND status = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(findExpiredSQL)) {
            ps.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));

                // Set notification flag for expired applications
                LoginNotification notification = getLoginNotification(playerUuid);
                if (notification == null) {
                    notification = new LoginNotification(playerUuid, true, false, false, false, false, false, System.currentTimeMillis());
                } else {
                    notification.setHasExpiredApplications(true);
                }
                saveLoginNotification(notification);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to process expired applications notifications: ", e);
        }

        // Now expire the applications
        expireApplications();
    }

    public void expireInvitationsWithNotifications() {
        // Similar to applications, but for invitations
        String findExpiredSQL = "SELECT DISTINCT player_uuid FROM faction_invitations WHERE expires_at <= ? AND status = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(findExpiredSQL)) {
            ps.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Note: expired invitations don't need special notifications as they're cleaned up
                // The player will simply see they have no pending invitations
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to process expired invitations notifications: ", e);
        }

        // Now expire the invitations
        expireInvitations();
    }

    public void cleanOldData() {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        String sqlApplications = "DELETE FROM recruitment_requests WHERE expires_at < ? AND status = 'EXPIRED';";
        String sqlInvitations = "DELETE FROM faction_invitations WHERE expires_at < ? AND status = 'EXPIRED';";

        try (PreparedStatement psApplications = connection.prepareStatement(sqlApplications);
             PreparedStatement psInvitations = connection.prepareStatement(sqlInvitations)) {

            psApplications.setTimestamp(1, new java.sql.Timestamp(thirtyDaysAgo));
            psApplications.executeUpdate();

            psInvitations.setTimestamp(1, new java.sql.Timestamp(thirtyDaysAgo));
            psInvitations.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to clean old data: ", e);
        }
    }
}