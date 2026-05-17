CREATE DATABASE IF NOT EXISTS codeforces_analyzer
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE codeforces_analyzer;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    handle VARCHAR(100) NOT NULL,
    platform VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_handle_platform (handle, platform),
    INDEX idx_users_handle (handle),
    INDEX idx_users_platform (platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    submission_id VARCHAR(80) NOT NULL,
    problem_name VARCHAR(255),
    verdict VARCHAR(100),
    language VARCHAR(120),
    submit_time DATETIME,
    source_code LONGTEXT,
    source_url VARCHAR(500),
    source_crawl_status VARCHAR(50) NOT NULL DEFAULT 'Đang chờ',
    source_crawl_message TEXT,
    crawl_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_submissions_user_submission (user_id, submission_id),
    INDEX idx_submissions_user_id (user_id),
    INDEX idx_submissions_submission_id (submission_id),
    INDEX idx_submissions_verdict (verdict),
    INDEX idx_submissions_language (language),
    INDEX idx_submissions_submit_time (submit_time),
    CONSTRAINT fk_submissions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    detected_algorithms TEXT,
    detected_data_structures TEXT,
    difficulty_level VARCHAR(80),
    ai_probability INT NOT NULL DEFAULT 0,
    ai_confidence VARCHAR(80),
    ai_reasons TEXT,
    ai_comment TEXT,
    data_structure_score INT NOT NULL DEFAULT 0,
    algorithm_score INT NOT NULL DEFAULT 0,
    analysis_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_analysis_submission (submission_id),
    INDEX idx_analysis_ai_probability (ai_probability),
    INDEX idx_analysis_difficulty (difficulty_level),
    CONSTRAINT fk_analysis_submission
        FOREIGN KEY (submission_id) REFERENCES submissions(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS crawl_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    crawl_status VARCHAR(50) NOT NULL,
    crawl_message TEXT,
    crawl_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_crawl_logs_user_id (user_id),
    INDEX idx_crawl_logs_time (crawl_time),
    CONSTRAINT fk_crawl_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cf_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(150) NOT NULL,
    encrypted_password TEXT NOT NULL,
    last_login DATETIME,
    cookies LONGTEXT,
    status VARCHAR(80) NOT NULL DEFAULT 'Chưa đăng nhập',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cf_accounts_username (username),
    INDEX idx_cf_accounts_status (status),
    INDEX idx_cf_accounts_last_login (last_login)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
