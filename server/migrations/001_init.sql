CREATE TABLE users (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    points INT NOT NULL DEFAULT 0,
    xp INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tokens (
    token_hash CHAR(64) NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    created_at BIGINT NOT NULL,
    last_used BIGINT NOT NULL,
    PRIMARY KEY (token_hash),
    KEY idx_tokens_user (user_id),
    CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE profiles (
    user_id INT UNSIGNED NOT NULL,
    name VARCHAR(80) NOT NULL DEFAULT '',
    age INT NOT NULL DEFAULT 0,
    goal VARCHAR(40) NOT NULL DEFAULT '',
    team VARCHAR(40) NOT NULL DEFAULT '',
    updated_at BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE journal (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    client_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL DEFAULT '',
    body TEXT NOT NULL,
    occurred_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_journal_client (user_id, client_id),
    CONSTRAINT fk_journal_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ledger (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    client_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL DEFAULT '',
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    income TINYINT(1) NOT NULL DEFAULT 1,
    category VARCHAR(40) NOT NULL DEFAULT 'other',
    occurred_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ledger_client (user_id, client_id),
    CONSTRAINT fk_ledger_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE books (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    client_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL DEFAULT '',
    page INT NOT NULL DEFAULT 0,
    pages INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_books_client (user_id, client_id),
    CONSTRAINT fk_books_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wallets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    client_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL DEFAULT '',
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_wallets_client (user_id, client_id),
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE threads (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_threads_created (created_at),
    CONSTRAINT fk_threads_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE posts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    thread_id BIGINT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    body TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_posts_thread (thread_id),
    CONSTRAINT fk_posts_thread FOREIGN KEY (thread_id) REFERENCES threads (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(400) NOT NULL DEFAULT '',
    price INT NOT NULL DEFAULT 0,
    kind VARCHAR(40) NOT NULL DEFAULT 'cosmetic',
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchases (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    item_id BIGINT UNSIGNED NOT NULL,
    price INT NOT NULL,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_purchases_user (user_id),
    CONSTRAINT fk_purchases_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchases_item FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE raid_seasons (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    goal INT NOT NULL DEFAULT 0,
    starts_at BIGINT NOT NULL,
    ends_at BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE raid_members (
    season_id BIGINT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    last_checkin BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (season_id, user_id),
    CONSTRAINT fk_raid_members_season FOREIGN KEY (season_id) REFERENCES raid_seasons (id) ON DELETE CASCADE,
    CONSTRAINT fk_raid_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE vault_items (
    user_id INT UNSIGNED NOT NULL,
    client_id BIGINT NOT NULL,
    payload LONGTEXT NOT NULL,
    updated_at BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, client_id),
    CONSTRAINT fk_vault_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
