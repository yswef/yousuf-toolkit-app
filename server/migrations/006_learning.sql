-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
CREATE TABLE IF NOT EXISTS learning_plans (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id INT UNSIGNED NOT NULL,
  title VARCHAR(255) NOT NULL DEFAULT '',
  subject VARCHAR(255) NOT NULL DEFAULT '',
  category VARCHAR(100) NOT NULL DEFAULT '',
  course_type VARCHAR(100) NOT NULL DEFAULT '',
  level VARCHAR(100) NOT NULL DEFAULT '',
  mode VARCHAR(20) NOT NULL DEFAULT 'daily',
  minutes INT NOT NULL DEFAULT 0,
  goals_json LONGTEXT NULL,
  plan_json LONGTEXT NOT NULL,
  course_md LONGTEXT NOT NULL,
  roadmap_xml LONGTEXT NOT NULL,
  site_html LONGTEXT NOT NULL,
  status ENUM('pending','approved','rejected','published') NOT NULL DEFAULT 'pending',
  site_url VARCHAR(512) NOT NULL DEFAULT '',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_learning_status (status, updated_at),
  INDEX idx_learning_user (user_id, created_at),
  CONSTRAINT fk_learning_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
