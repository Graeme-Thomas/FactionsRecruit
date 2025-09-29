-- FactionsRecruit Database Schema
-- MySQL 8.0+ Compatible Schema for Latest Minecraft Version
-- Database: factions_recruitment

CREATE DATABASE IF NOT EXISTS factions_recruitment;
USE factions_recruitment;

-- =====================================================
-- PLAYER PROFILES AND RESUMES
-- =====================================================

CREATE TABLE player_resume (
    player_uuid CHAR(36) PRIMARY KEY COMMENT 'Minecraft player UUID',
    player_name VARCHAR(16) NOT NULL COMMENT 'Current Minecraft username (max 16 chars)',
    timezone ENUM('NA_WEST','NA_EAST','EU_WEST','EU_CENTRAL','ASIA','OCEANIA') NOT NULL COMMENT 'Player timezone region',
    discord_tag VARCHAR(32) COMMENT 'Discord username (username or username#discriminator)',
    factions_experience TINYINT UNSIGNED CHECK (factions_experience BETWEEN 1 AND 10) COMMENT 'Factions experience level 1-10',
    raiding_skill TINYINT UNSIGNED CHECK (raiding_skill BETWEEN 1 AND 10) COMMENT 'Raiding skill level 1-10',
    building_skill TINYINT UNSIGNED CHECK (building_skill BETWEEN 1 AND 10) COMMENT 'Building skill level 1-10',
    pvp_skill TINYINT UNSIGNED CHECK (pvp_skill BETWEEN 1 AND 10) COMMENT 'PvP skill level 1-10',
    availability_hours TINYINT UNSIGNED CHECK (availability_hours BETWEEN 1 AND 10) COMMENT 'Weekly hours available (1=<10hrs, 10=>100hrs)',
    previous_factions JSON COMMENT 'Array of previous faction names (max 10 factions)',
    is_looking BOOLEAN DEFAULT TRUE COMMENT 'Currently seeking a faction',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Player account is active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Profile created timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last updated timestamp',

    CONSTRAINT chk_previous_factions_valid
        CHECK (JSON_VALID(previous_factions) AND JSON_LENGTH(previous_factions) <= 10)
) ENGINE=InnoDB COMMENT='Player recruitment profiles and skill information';

-- =====================================================
-- FACTION RECRUITMENT STATUS
-- =====================================================

CREATE TABLE faction_recruitment (
    faction_uuid CHAR(36) PRIMARY KEY COMMENT 'Faction UUID from faction plugin',
    faction_name VARCHAR(32) NOT NULL COMMENT 'Current faction name',
    leader_uuid CHAR(36) NOT NULL COMMENT 'Faction leader player UUID',
    is_recruiting BOOLEAN DEFAULT FALSE COMMENT 'Currently accepting applications',
    max_members INT UNSIGNED COMMENT 'Maximum faction size limit',
    current_members INT UNSIGNED DEFAULT 1 COMMENT 'Current number of members',
    requirements JSON COMMENT 'Recruitment requirements as structured data',
    banner_data JSON COMMENT 'Faction banner pattern data for display',
    description TEXT COMMENT 'Faction description for recruitment',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Recruitment profile created',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last updated timestamp',

    CONSTRAINT chk_requirements_valid
        CHECK (JSON_VALID(requirements)),
    CONSTRAINT chk_banner_data_valid
        CHECK (banner_data IS NULL OR JSON_VALID(banner_data)),
    CONSTRAINT chk_members_logical
        CHECK (current_members <= max_members)
) ENGINE=InnoDB COMMENT='Faction recruitment status and information';

-- =====================================================
-- PLAYER APPLICATIONS TO FACTIONS
-- =====================================================

CREATE TABLE applications (
    application_id CHAR(36) PRIMARY KEY COMMENT 'Unique application identifier',
    faction_uuid CHAR(36) NOT NULL COMMENT 'Target faction UUID',
    player_uuid CHAR(36) NOT NULL COMMENT 'Applicant player UUID',
    status ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') DEFAULT 'PENDING' COMMENT 'Application status',
    message TEXT COMMENT 'Application message from player',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When application was submitted',
    processed_at TIMESTAMP NULL COMMENT 'When application was processed',
    processed_by CHAR(36) NULL COMMENT 'UUID of faction member who processed application',
    expires_at TIMESTAMP COMMENT 'Automatic expiration timestamp',
    rejection_reason TEXT COMMENT 'Reason for rejection if applicable',

    CONSTRAINT fk_application_faction
        FOREIGN KEY (faction_uuid) REFERENCES faction_recruitment(faction_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_application_player
        FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_application_processor
        FOREIGN KEY (processed_by) REFERENCES player_resume(player_uuid) ON DELETE SET NULL,
    CONSTRAINT chk_processed_logic
        CHECK ((status = 'PENDING' AND processed_at IS NULL AND processed_by IS NULL) OR
               (status != 'PENDING' AND processed_at IS NOT NULL)),
    CONSTRAINT uk_player_faction_application
        UNIQUE (player_uuid, faction_uuid, status) -- Prevent duplicate pending applications
) ENGINE=InnoDB COMMENT='Player applications to join factions';

-- =====================================================
-- FACTION INVITATIONS TO PLAYERS
-- =====================================================

CREATE TABLE invitations (
    invitation_id CHAR(36) PRIMARY KEY COMMENT 'Unique invitation identifier',
    faction_uuid CHAR(36) NOT NULL COMMENT 'Inviting faction UUID',
    player_uuid CHAR(36) NOT NULL COMMENT 'Invited player UUID',
    invited_by CHAR(36) NOT NULL COMMENT 'Faction member who sent invitation',
    status ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') DEFAULT 'PENDING' COMMENT 'Invitation status',
    message TEXT COMMENT 'Custom invitation message from faction',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When invitation was sent',
    processed_at TIMESTAMP NULL COMMENT 'When invitation was responded to',
    expires_at TIMESTAMP COMMENT 'Automatic expiration timestamp',

    CONSTRAINT fk_invitation_faction
        FOREIGN KEY (faction_uuid) REFERENCES faction_recruitment(faction_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_player
        FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_sender
        FOREIGN KEY (invited_by) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    CONSTRAINT chk_invitation_processed_logic
        CHECK ((status = 'PENDING' AND processed_at IS NULL) OR
               (status != 'PENDING' AND processed_at IS NOT NULL)),
    CONSTRAINT uk_faction_player_invitation
        UNIQUE (faction_uuid, player_uuid, status) -- Prevent duplicate pending invitations
) ENGINE=InnoDB COMMENT='Faction invitations sent to players';

-- =====================================================
-- PLAYER SESSION TRACKING
-- =====================================================

CREATE TABLE player_sessions (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique session identifier',
    player_uuid CHAR(36) NOT NULL COMMENT 'Player UUID',
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Session start time',
    logout_time TIMESTAMP NULL COMMENT 'Session end time (NULL if still online)',
    server_name VARCHAR(64) COMMENT 'Server name for multi-server networks',
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last activity timestamp',

    CONSTRAINT fk_session_player
        FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    INDEX idx_player_sessions (player_uuid, login_time),
    INDEX idx_active_sessions (logout_time, last_activity) -- For finding active sessions
) ENGINE=InnoDB COMMENT='Player login/logout session tracking';

-- =====================================================
-- PERFORMANCE INDEXES
-- =====================================================

-- Query optimization indexes
CREATE INDEX idx_player_looking ON player_resume(is_looking, is_active);
CREATE INDEX idx_player_timezone_skills ON player_resume(timezone, is_looking, factions_experience, pvp_skill);
CREATE INDEX idx_faction_recruiting ON faction_recruitment(is_recruiting, current_members, max_members);
CREATE INDEX idx_faction_leader ON faction_recruitment(leader_uuid, is_recruiting);

-- Application and invitation indexes
CREATE INDEX idx_applications_faction_status ON applications(faction_uuid, status, submitted_at);
CREATE INDEX idx_applications_player_status ON applications(player_uuid, status, submitted_at);
CREATE INDEX idx_applications_expires ON applications(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_invitations_faction_status ON invitations(faction_uuid, status, sent_at);
CREATE INDEX idx_invitations_player_status ON invitations(player_uuid, status, sent_at);
CREATE INDEX idx_invitations_expires ON invitations(expires_at) WHERE expires_at IS NOT NULL;

-- =====================================================
-- AUTOMATED CLEANUP PROCEDURES
-- =====================================================

DELIMITER $$

-- Procedure to expire old applications and invitations
CREATE PROCEDURE CleanupExpiredItems()
BEGIN
    -- Expire old applications
    UPDATE applications
    SET status = 'EXPIRED', processed_at = NOW()
    WHERE status = 'PENDING'
    AND expires_at < NOW();

    -- Expire old invitations
    UPDATE invitations
    SET status = 'EXPIRED', processed_at = NOW()
    WHERE status = 'PENDING'
    AND expires_at < NOW();

    -- Close old sessions (players who didn't log out properly)
    UPDATE player_sessions
    SET logout_time = last_activity
    WHERE logout_time IS NULL
    AND last_activity < DATE_SUB(NOW(), INTERVAL 1 HOUR);
END$$

DELIMITER ;

-- =====================================================
-- SAMPLE DATA VIEWS FOR COMMON QUERIES
-- =====================================================

-- View for active recruiting factions with leader info
CREATE VIEW active_recruiting_factions AS
SELECT
    fr.faction_uuid,
    fr.faction_name,
    pr.player_name AS leader_name,
    fr.current_members,
    fr.max_members,
    fr.description,
    fr.updated_at
FROM faction_recruitment fr
JOIN player_resume pr ON fr.leader_uuid = pr.player_uuid
WHERE fr.is_recruiting = TRUE
AND pr.is_active = TRUE;

-- View for players looking for factions
CREATE VIEW available_players AS
SELECT
    pr.player_uuid,
    pr.player_name,
    pr.timezone,
    pr.factions_experience,
    pr.raiding_skill,
    pr.building_skill,
    pr.pvp_skill,
    pr.availability_hours,
    pr.discord_tag,
    pr.updated_at
FROM player_resume pr
WHERE pr.is_looking = TRUE
AND pr.is_active = TRUE;

-- View for pending applications with faction and player info
CREATE VIEW pending_applications_view AS
SELECT
    a.application_id,
    a.faction_uuid,
    fr.faction_name,
    a.player_uuid,
    pr.player_name,
    a.message,
    a.submitted_at,
    a.expires_at
FROM applications a
JOIN faction_recruitment fr ON a.faction_uuid = fr.faction_uuid
JOIN player_resume pr ON a.player_uuid = pr.player_uuid
WHERE a.status = 'PENDING';