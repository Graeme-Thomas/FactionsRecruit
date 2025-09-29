-- FactionsRecruit Database Schema
-- Cleaned for JDBC execution (no DELIMITER, no USE)

-- =====================================================
-- PLAYER PROFILES AND RESUMES
-- =====================================================
CREATE TABLE IF NOT EXISTS player_resume (
    player_uuid CHAR(36) PRIMARY KEY COMMENT 'Minecraft player UUID',
    player_name VARCHAR(16) NOT NULL COMMENT 'Current Minecraft username (max 16 chars)',
    timezone ENUM('NA_WEST','NA_EAST','EU_WEST','EU_CENTRAL','ASIA','OCEANIA') NOT NULL COMMENT 'Player timezone region',
    discord_tag VARCHAR(32) COMMENT 'Discord username',
    factions_experience TINYINT UNSIGNED CHECK (factions_experience BETWEEN 1 AND 10),
    raiding_skill TINYINT UNSIGNED CHECK (raiding_skill BETWEEN 1 AND 10),
    building_skill TINYINT UNSIGNED CHECK (building_skill BETWEEN 1 AND 10),
    pvp_skill TINYINT UNSIGNED CHECK (pvp_skill BETWEEN 1 AND 10),
    availability_hours TINYINT UNSIGNED CHECK (availability_hours BETWEEN 1 AND 10),
    previous_factions JSON,
    is_looking BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_prev_factions CHECK (JSON_VALID(previous_factions) AND JSON_LENGTH(previous_factions) <= 10)
);

-- =====================================================
-- FACTION RECRUITMENT STATUS
-- =====================================================
CREATE TABLE IF NOT EXISTS faction_recruitment (
    faction_uuid CHAR(36) PRIMARY KEY,
    faction_name VARCHAR(32) NOT NULL,
    leader_uuid CHAR(36) NOT NULL,
    is_recruiting BOOLEAN DEFAULT FALSE,
    max_members INT UNSIGNED,
    current_members INT UNSIGNED DEFAULT 1,
    requirements JSON,
    banner_data JSON,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_req_valid CHECK (requirements IS NULL OR JSON_VALID(requirements)),
    CONSTRAINT chk_banner_valid CHECK (banner_data IS NULL OR JSON_VALID(banner_data)),
    CONSTRAINT chk_members CHECK (current_members <= max_members)
);

-- =====================================================
-- PLAYER APPLICATIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS applications (
    application_id CHAR(36) PRIMARY KEY,
    faction_uuid CHAR(36) NOT NULL,
    player_uuid CHAR(36) NOT NULL,
    status ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') DEFAULT 'PENDING',
    message TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by CHAR(36) NULL,
    expires_at TIMESTAMP,
    rejection_reason TEXT,
    CONSTRAINT fk_app_faction FOREIGN KEY (faction_uuid) REFERENCES faction_recruitment(faction_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_app_player FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_app_proc FOREIGN KEY (processed_by) REFERENCES player_resume(player_uuid) ON DELETE SET NULL,
    CONSTRAINT chk_proc_logic CHECK (
        (status = 'PENDING' AND processed_at IS NULL AND processed_by IS NULL) OR
        (status != 'PENDING' AND processed_at IS NOT NULL)
    ),
    CONSTRAINT uk_app UNIQUE (player_uuid, faction_uuid, status)
);

-- =====================================================
-- FACTION INVITATIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS invitations (
    invitation_id CHAR(36) PRIMARY KEY,
    faction_uuid CHAR(36) NOT NULL,
    player_uuid CHAR(36) NOT NULL,
    invited_by CHAR(36) NOT NULL,
    status ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') DEFAULT 'PENDING',
    message TEXT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    expires_at TIMESTAMP,
    CONSTRAINT fk_inv_faction FOREIGN KEY (faction_uuid) REFERENCES faction_recruitment(faction_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_inv_player FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    CONSTRAINT fk_inv_sender FOREIGN KEY (invited_by) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    CONSTRAINT chk_inv_logic CHECK (
        (status = 'PENDING' AND processed_at IS NULL) OR
        (status != 'PENDING' AND processed_at IS NOT NULL)
    ),
    CONSTRAINT uk_inv UNIQUE (faction_uuid, player_uuid, status)
);

-- =====================================================
-- PLAYER SESSIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS player_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_uuid CHAR(36) NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    server_name VARCHAR(64),
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sess_player FOREIGN KEY (player_uuid) REFERENCES player_resume(player_uuid) ON DELETE CASCADE,
    INDEX idx_sess_player (player_uuid, login_time),
    INDEX idx_sess_active (logout_time, last_activity)
);

-- =====================================================
-- INDEXES
-- =====================================================
CREATE INDEX idx_player_looking ON player_resume(is_looking, is_active);
CREATE INDEX idx_player_timezone_skills ON player_resume(timezone, is_looking, factions_experience, pvp_skill);
CREATE INDEX idx_faction_recruiting ON faction_recruitment(is_recruiting, current_members, max_members);
CREATE INDEX idx_faction_leader ON faction_recruitment(leader_uuid, is_recruiting);
CREATE INDEX idx_app_faction_status ON applications(faction_uuid, status, submitted_at);
CREATE INDEX idx_app_player_status ON applications(player_uuid, status, submitted_at);
CREATE INDEX idx_app_expires ON applications(expires_at);
CREATE INDEX idx_inv_faction_status ON invitations(faction_uuid, status, sent_at);
CREATE INDEX idx_inv_player_status ON invitations(player_uuid, status, sent_at);
CREATE INDEX idx_inv_expires ON invitations(expires_at);

-- =====================================================
-- VIEWS
-- =====================================================
CREATE OR REPLACE VIEW active_recruiting_factions AS
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

CREATE OR REPLACE VIEW available_players AS
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

CREATE OR REPLACE VIEW pending_applications_view AS
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
