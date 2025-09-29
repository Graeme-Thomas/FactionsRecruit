package com.gnut.factionsrecruit.database;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages MySQL database connections using DriverManager.
 * Provides thread-safe, async database operations for the FactionsRecruit plugin.
 */
public class DatabaseManager {

    private final Plugin plugin;
    private final Logger logger;
    private Connection connection;
    private boolean initialized = false;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Initializes the database connection and creates tables if needed.
     * This method is async-safe and can be called from the main thread.
     *
     * @return CompletableFuture that completes when initialization is done
     */
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting database initialization...");

                // Load JDBC driver
                logger.info("Attempting to load JDBC driver...");
                Class.forName("com.mysql.cj.jdbc.Driver");
                logger.info("JDBC driver loaded.");

                // Build connection string
                String connectionString = buildConnectionString();
                logger.info("Connection string built successfully");

                // Establish connection
                logger.info("Attempting to establish database connection...");
                connection = DriverManager.getConnection(connectionString);
                logger.info("MySQL database connected.");

                // Test connection
                if (!connection.isValid(5)) {
                    throw new SQLException("Database connection validation failed");
                }

                // Initialize schema
                logger.info("Attempting to create tables...");
                initializeSchema();
                logger.info("Database tables created or checked.");

                this.initialized = true;
                logger.info("Database initialization completed");
                return true;

            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "MySQL JDBC driver not found!", e);
                return false;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to connect to MySQL database!", e);
                logger.log(Level.SEVERE, "Connection error details: " + e.getMessage());
                return false;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error during database initialization", e);
                return false;
            }
        });
    }

    /**
     * Builds connection string from plugin config.yml
     * Supports both connection string and individual parameters
     */
    private String buildConnectionString() {
        ConfigurationSection dbConfig = plugin.getConfig().getConfigurationSection("database");
        if (dbConfig == null) {
            throw new IllegalStateException("Database configuration section not found in config.yml");
        }

        // Check if connection string is provided
        String connectionString = dbConfig.getString("connection-string");

        if (connectionString != null && !connectionString.isEmpty()) {
            // Use provided connection string
            logger.info("Using connection string from config");

            // Extract username and password if provided separately
            String username = dbConfig.getString("username");
            String password = dbConfig.getString("password");

            // If credentials are in the config, append them to connection string
            if (username != null && !username.isEmpty() && !connectionString.contains(username)) {
                // Build full connection string with credentials
                String host = extractHostFromJdbc(connectionString);
                return connectionString + (connectionString.contains("?") ? "&" : "?") +
                       "user=" + username + "&password=" + password;
            }

            return connectionString;
        } else {
            // Build from individual parameters
            logger.info("Using individual parameters for database configuration");

            String host = dbConfig.getString("host", "localhost");
            int port = dbConfig.getInt("port", 3306);
            String database = dbConfig.getString("database", "customer_1099940_detrixmain");
            String username = dbConfig.getString("username", "minecraft");
            String password = dbConfig.getString("password", "");

            // Build connection string with safe defaults for MySQL 8.x+
            String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&user=%s&password=%s",
                host, port, database, username, password
            );

            logger.info(String.format("Database connection configured: %s@%s:%d/%s",
                username, host, port, database));

            return jdbcUrl;
        }
    }

    /**
     * Extract host from JDBC URL for logging
     */
    private String extractHostFromJdbc(String jdbcUrl) {
        try {
            // jdbc:mysql://host:port/database?params
            String[] parts = jdbcUrl.split("//");
            if (parts.length > 1) {
                String hostPart = parts[1].split("/")[0];
                return hostPart.split(":")[0];
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return "remote-host";
    }

    /**
     * Initializes database schema by executing schema.sql file.
     * Creates all necessary tables if they don't exist.
     */
    private void initializeSchema() throws SQLException {
        logger.info("Creating tables, views, and triggers...");

        try (Statement statement = connection.createStatement()) {

            // PLAYER RESUME
            statement.execute("CREATE TABLE IF NOT EXISTS player_resume (" +
                    "player_uuid CHAR(36) PRIMARY KEY COMMENT 'Minecraft player UUID'," +
                    "player_name VARCHAR(16) NOT NULL COMMENT 'Current Minecraft username (max 16 chars)'," +
                    "timezone ENUM('NA_WEST','NA_EAST','EU_WEST','EU_CENTRAL','ASIA','OCEANIA') NOT NULL COMMENT 'Player timezone region'," +
                    "discord_tag VARCHAR(32)," +
                    "factions_experience TINYINT UNSIGNED CHECK (factions_experience BETWEEN 1 AND 10)," +
                    "raiding_skill TINYINT UNSIGNED CHECK (raiding_skill BETWEEN 1 AND 10)," +
                    "building_skill TINYINT UNSIGNED CHECK (building_skill BETWEEN 1 AND 10)," +
                    "pvp_skill TINYINT UNSIGNED CHECK (pvp_skill BETWEEN 1 AND 10)," +
                    "availability_hours TINYINT UNSIGNED CHECK (availability_hours BETWEEN 1 AND 10)," +
                    "previous_factions JSON," +
                    "is_looking BOOLEAN DEFAULT TRUE," +
                    "is_active BOOLEAN DEFAULT TRUE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "CONSTRAINT chk_prev_factions CHECK (JSON_VALID(previous_factions) AND JSON_LENGTH(previous_factions) <= 10)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // FACTION RECRUITMENT
            statement.execute("CREATE TABLE IF NOT EXISTS faction_recruitment (" +
                    "faction_uuid CHAR(36) PRIMARY KEY," +
                    "faction_name VARCHAR(32) NOT NULL," +
                    "leader_uuid CHAR(36) NOT NULL," +
                    "is_recruiting BOOLEAN DEFAULT FALSE," +
                    "max_members INT UNSIGNED," +
                    "current_members INT UNSIGNED DEFAULT 1," +
                    "requirements JSON," +
                    "banner_data JSON," +
                    "description TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "CONSTRAINT chk_req_valid CHECK (requirements IS NULL OR JSON_VALID(requirements))," +
                    "CONSTRAINT chk_banner_valid CHECK (banner_data IS NULL OR JSON_VALID(banner_data))," +
                    "CONSTRAINT chk_members CHECK (current_members <= max_members)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // APPLICATIONS
            statement.execute("CREATE TABLE IF NOT EXISTS applications (" +
                    "application_id CHAR(36) PRIMARY KEY," +
                    "faction_uuid CHAR(36) NOT NULL," +
                    "player_uuid CHAR(36) NOT NULL," +
                    "status ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') DEFAULT 'PENDING'," +
                    "message TEXT," +
                    "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "processed_at TIMESTAMP NULL," +
                    "processed_by CHAR(36) NULL," +
                    "expires_at TIMESTAMP," +
                    "rejection_reason TEXT," +
                    "CONSTRAINT fk_app_faction FOREIGN KEY (faction_uuid) REFERENCES faction_recruitment(faction_uuid) ON DELETE CASCADE," +
                    "CONSTRAINT fk_app_player FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE," +
                    "CONSTRAINT fk_app_proc FOREIGN KEY (processed_by) REFERENCES player_resume(player_uuid) ON DELETE SET NULL," +
                    // "CONSTRAINT chk_proc_logic CHECK ((status = 'PENDING' AND processed_at IS NULL AND processed_by IS NULL) OR (status != 'PENDING' AND processed_at IS NOT NULL))," + // <-- REMOVED THIS LINE
                    "CONSTRAINT uk_app UNIQUE (player_uuid, faction_uuid, status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            
            // TRIGGERS FOR 'applications' TABLE LOGIC
            // This replaces the removed CHECK constraint 'chk_proc_logic'
            statement.execute("DROP TRIGGER IF EXISTS trg_applications_insert_logic;");
            statement.execute("CREATE TRIGGER trg_applications_insert_logic " +
                    "BEFORE INSERT ON applications FOR EACH ROW " +
                    "BEGIN " +
                    "    IF NOT ((NEW.status = 'PENDING' AND NEW.processed_at IS NULL AND NEW.processed_by IS NULL) OR " +
                    "            (NEW.status != 'PENDING' AND NEW.processed_at IS NOT NULL)) THEN " +
                    "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Check constraint violation on insert: chk_proc_logic failed'; " +
                    "    END IF; " +
                    "END;");

            statement.execute("DROP TRIGGER IF EXISTS trg_applications_update_logic;");
            statement.execute("CREATE TRIGGER trg_applications_update_logic " +
                    "BEFORE UPDATE ON applications FOR EACH ROW " +
                    "BEGIN " +
                    "    IF NOT ((NEW.status = 'PENDING' AND NEW.processed_at IS NULL AND NEW.processed_by IS NULL) OR " +
                    "            (NEW.status != 'PENDING' AND NEW.processed_at IS NOT NULL)) THEN " +
                    "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Check constraint violation on update: chk_proc_logic failed'; " +
                    "    END IF; " +
                    "END;");


            // INVITATIONS
            statement.execute("CREATE TABLE IF NOT EXISTS invitations (" +
                    "invitation_id CHAR(36) PRIMARY KEY," +
                    "faction_uuid CHAR(36) NOT NULL," +
                    "player_uuid CHAR(36) NOT NULL," +
                    "invited_by CHAR(36) NOT NULL," +
                    "status ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') DEFAULT 'PENDING'," +
                    "message TEXT," +
                    "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "processed_at TIMESTAMP NULL," +
                    "expires_at TIMESTAMP," +
                    "CONSTRAINT fk_inv_faction FOREIGN KEY (faction_uuid) REFERENCES faction_recruitment(faction_uuid) ON DELETE CASCADE," +
                    "CONSTRAINT fk_inv_player FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE," +
                    "CONSTRAINT fk_inv_sender FOREIGN KEY (invited_by) REFERENCES player_resume(player_uuid) ON DELETE CASCADE," +
                    "CONSTRAINT chk_inv_logic CHECK ((status = 'PENDING' AND processed_at IS NULL) OR (status != 'PENDING' AND processed_at IS NOT NULL))," +
                    "CONSTRAINT uk_inv UNIQUE (faction_uuid, player_uuid, status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // PLAYER SESSIONS
            statement.execute("CREATE TABLE IF NOT EXISTS player_sessions (" +
                    "session_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "player_uuid CHAR(36) NOT NULL," +
                    "login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "logout_time TIMESTAMP NULL," +
                    "server_name VARCHAR(64)," +
                    "last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_sess_player FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE," +
                    "INDEX idx_sess_player (player_uuid, login_time)," +
                    "INDEX idx_sess_active (logout_time, last_activity)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // VIEWS
            statement.execute("CREATE OR REPLACE VIEW active_recruiting_factions AS " +
                    "SELECT fr.faction_uuid, fr.faction_name, pr.player_name AS leader_name, fr.current_members, fr.max_members, fr.description, fr.updated_at " +
                    "FROM faction_recruitment fr JOIN player_resume pr ON fr.leader_uuid = pr.player_uuid " +
                    "WHERE fr.is_recruiting = TRUE AND pr.is_active = TRUE;");

            statement.execute("CREATE OR REPLACE VIEW available_players AS " +
                    "SELECT pr.player_uuid, pr.player_name, pr.timezone, pr.factions_experience, pr.raiding_skill, pr.building_skill, pr.pvp_skill, pr.availability_hours, pr.discord_tag, pr.updated_at " +
                    "FROM player_resume pr WHERE pr.is_looking = TRUE AND pr.is_active = TRUE;");

            statement.execute("CREATE OR REPLACE VIEW pending_applications_view AS " +
                    "SELECT a.application_id, a.faction_uuid, fr.faction_name, a.player_uuid, pr.player_name, a.message, a.submitted_at, a.expires_at " +
                    "FROM applications a JOIN faction_recruitment fr ON a.faction_uuid = fr.faction_uuid " +
                    "JOIN player_resume pr ON a.player_uuid = pr.player_uuid WHERE a.status = 'PENDING';");

            logger.info("Database schema initialized successfully");
        }
    }


    /**
     * Gets a database connection.
     * Creates a new connection if the current one is closed.
     *
     * @return Active database connection
     * @throws SQLException if connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            logger.warning("Connection was closed, reconnecting...");
            String connectionString = buildConnectionString();
            connection = DriverManager.getConnection(connectionString);
        }
        return connection;
    }

    /**
     * Executes a database operation asynchronously.
     *
     * @param operation The operation to execute
     * @return CompletableFuture that completes when operation is done
     */
    public CompletableFuture<Void> executeAsync(DatabaseOperation operation) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                operation.execute(conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database operation failed", e);
            }
        });
    }

    /**
     * Executes a database query asynchronously.
     *
     * @param query The query to execute
     * @param <T> Result type
     * @return CompletableFuture with query result
     */
    public <T> CompletableFuture<T> queryAsync(DatabaseQuery<T> query) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                return query.execute(conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database query failed", e);
                return null;
            }
        });
    }

    /**
     * Closes the database connection.
     * Should be called when plugin is disabled.
     */
    public void shutdown() {
        logger.info("Closing database connection...");
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    /**
     * Checks if database is initialized and ready.
     *
     * @return true if database is ready
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Functional interface for database operations.
     */
    @FunctionalInterface
    public interface DatabaseOperation {
        void execute(Connection connection) throws SQLException;
    }

    /**
     * Functional interface for database queries.
     */
    @FunctionalInterface
    public interface DatabaseQuery<T> {
        T execute(Connection connection) throws SQLException;
    }

    // ==================== STUB METHODS FOR GUI INTEGRATION ====================
    // TODO: Implement these methods with actual SQL queries

    /**
     * Check if player profile exists in database
     */
    public CompletableFuture<Boolean> playerExists(java.util.UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement database query
            logger.warning("playerExists() not yet implemented");
            return false;
        });
    }

    /**
     * Create initial player profile
     */
    public CompletableFuture<Void> createPlayerProfile(java.util.UUID playerUuid, String playerName) {
        return CompletableFuture.runAsync(() -> {
            // TODO: Implement database insert
            logger.warning("createPlayerProfile() not yet implemented");
        });
    }

    /**
     * Update player's last seen timestamp
     */
    public CompletableFuture<Void> updateLastSeen(java.util.UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            // TODO: Implement database update
            logger.warning("updateLastSeen() not yet implemented");
        });
    }

    /**
     * Get total number of player profiles
     */
    public CompletableFuture<Integer> getTotalPlayers() {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement database query
            return 0;
        });
    }

    /**
     * Get number of recruiting factions
     */
    public CompletableFuture<Integer> getRecruitingFactions() {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement database query
            return 0;
        });
    }

    /**
     * Get number of pending applications
     */
    public CompletableFuture<Integer> getPendingApplications() {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement database query
            return 0;
        });
    }

    /**
     * Get number of pending invitations
     */
    public CompletableFuture<Integer> getPendingInvitations() {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement database query
            return 0;
        });
    }

    /**
     * Cleanup expired applications
     */
    public CompletableFuture<Integer> cleanupExpiredApplications() {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement database cleanup
            return 0;
        });
    }

    /**
     * Cleanup expired invitations
     */
    public CompletableFuture<Integer> cleanupExpiredInvitations() {
        return CompletableFuture.supplyAsync(() -> {
            // TODO: Implement database cleanup
            return 0;
        });
    }
}