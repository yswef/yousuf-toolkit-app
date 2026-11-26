-- Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
-- جداول جديدة غير مكررة. users موحّد ولا يتكرر. كل استعلام مقيّد بـ app_id.

CREATE TABLE IF NOT EXISTS developer_accounts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    user_id INT UNSIGNED NOT NULL,
    role VARCHAR(24) NOT NULL DEFAULT 'developer',
    granted_by VARCHAR(120) NOT NULL DEFAULT 'panel',
    granted_at BIGINT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_developer_account (app_id, user_id),
    KEY idx_developer_role (app_id, role, active),
    CONSTRAINT fk_developer_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS panel_admins (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    label VARCHAR(80) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    role VARCHAR(24) NOT NULL DEFAULT 'admin',
    created_at BIGINT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_panel_admin_token (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS diagnostic_reports (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    issue_signature VARCHAR(120) NOT NULL,
    symptoms JSON NOT NULL,
    device_model VARCHAR(120) NOT NULL DEFAULT '',
    android_version VARCHAR(40) NOT NULL DEFAULT '',
    rom_version VARCHAR(120) NOT NULL DEFAULT '',
    relevant_readings JSON NOT NULL,
    diagnosis_ranked_causes JSON NOT NULL,
    attempted_fix_actions JSON NOT NULL,
    resolved_locally TINYINT(1) NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_diag_signature (app_id, issue_signature)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS diagnostic_solutions (
    issue_signature VARCHAR(120) NOT NULL,
    app_id VARCHAR(64) NOT NULL DEFAULT 'y-dimensions',
    solution_ar TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (app_id, issue_signature)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO diagnostic_solutions (issue_signature, app_id, solution_ar, created_at) VALUES
('touch_lag_during_ac_charging', 'y-dimensions', 'جرّب شاحناً أصلياً ومقبساً مؤرضاً، ثم كابلاً آخر. إن استمرت المشكلة فقط عند الشحن فهي تشويش تموج تيار يصل لخط اللمس؛ افحص واقي الشاشة ودرجة الحرارة.', UNIX_TIMESTAMP()),
('low_internal_storage', 'y-dimensions', 'امسح الكاش المؤقت أولاً، ثم انقل الوسائط لبطاقة SD أو احذف التطبيقات الثقيلة. أبقِ 10٪ على الأقل من المساحة فارغة.', UNIX_TIMESTAMP()),
('weak_wifi_signal', 'y-dimensions', 'اقترب من الراوتر أو بدّل إلى نطاق 5GHz عند القرب، وغيّر قناة الراوتر عند الازدحام.', UNIX_TIMESTAMP()),
('slow_dns', 'y-dimensions', 'جرّب DNS عاماً مثل 1.1.1.1 أو 8.8.8.8 على الراوتر، ثم أعد قياس زمن الاستجابة.', UNIX_TIMESTAMP()),
('high_battery_temp', 'y-dimensions', 'أوقف الشحن السريع مؤقتاً، أزل الغلاف، وابتعد عن الشمس. فوق 42°C توقف عن الشحن حتى تبرد.', UNIX_TIMESTAMP());
-- Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
