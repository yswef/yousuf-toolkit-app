-- Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
-- جداول جديدة غير مكررة؛ users موحّد ولا يتكرر. كل استعلام مقيّد بـ app_id.
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    user_id INT UNSIGNED NOT NULL,
    token_hash CHAR(64) NOT NULL,
    label VARCHAR(120) NOT NULL DEFAULT '',
    model VARCHAR(120) NOT NULL DEFAULT '',
    last_seen BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    revoked TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_devices_token (app_id, token_hash),
    KEY idx_devices_user (app_id, user_id),
    CONSTRAINT fk_devices_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_packs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    version VARCHAR(40) NOT NULL,
    file_path VARCHAR(190) NOT NULL,
    sha256 CHAR(64) NOT NULL,
    size_bytes BIGINT UNSIGNED NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_content_pack (app_id, version, file_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS releases (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    version VARCHAR(40) NOT NULL,
    version_code INT UNSIGNED NOT NULL DEFAULT 0,
    apk_path VARCHAR(255) NOT NULL DEFAULT '',
    notes TEXT NOT NULL,
    min_supported VARCHAR(40) NOT NULL DEFAULT '',
    active TINYINT(1) NOT NULL DEFAULT 1,
    published_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_release (app_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_requirements (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    category VARCHAR(80) NOT NULL,
    item VARCHAR(190) NOT NULL,
    source VARCHAR(190) NOT NULL DEFAULT '',
    provided TINYINT(1) NOT NULL DEFAULT 0,
    priority INT NOT NULL DEFAULT 5,
    note VARCHAR(400) NOT NULL DEFAULT '',
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_req_sort (app_id, provided, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS asset_sources (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    kind VARCHAR(80) NOT NULL,
    name VARCHAR(190) NOT NULL,
    path_or_url VARCHAR(255) NOT NULL DEFAULT '',
    license VARCHAR(120) NOT NULL DEFAULT '',
    status VARCHAR(60) NOT NULL DEFAULT '',
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_asset_kind (app_id, kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
