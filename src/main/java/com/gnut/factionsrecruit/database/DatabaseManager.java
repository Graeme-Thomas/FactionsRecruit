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
        logger.info("Loading database schema...");

        try (InputStream schemaStream = plugin.getResource("database/schema.sql")) {
            if (schemaStream == null) {
                logger.warning("schema.sql not found in plugin resources. Skipping schema initialization.");
                return;
            }

            // Read schema file
            String schemaSql = new BufferedReader(new InputStreamReader(schemaStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Split by semicolon and execute each statement
            //String[] statements = schemaSql.split("(?<=;)\\s*\\n"); // only split at semicolons followed by newline
            String[] statements = schemaSql.split(";");
            // for (String sql : statements) {
            //     sql = sql.trim();
            //     if (sql.isEmpty() || sql.startsWith("--") || sql.startsWith("/*")) {
            //         continue;
            //     }
            //     try (Statement stmt = connection.createStatement()) {
            //         logger.info("Executing: " + sql.substring(0, Math.min(80, sql.length())) + "...");
            //         stmt.execute(sql);
            //     }
            // }
            for (String sql : statements) {
                sql = sql.trim();
                if (sql.isEmpty() || sql.startsWith("--") || sql.startsWith("/*")) continue;
                try (Statement stmt = connection.createStatement()) {
                    logger.info("Executing: " + sql);
                    stmt.execute(sql);
                }
            }
            logger.info("Database schema initialized successfully");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read schema.sql file", e);
            throw new SQLException("Could not load database schema", e);
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