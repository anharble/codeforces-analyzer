USE codeforces_analyzer;

INSERT INTO users (handle, platform)
VALUES
    ('tourist', 'CODEFORCES'),
    ('jiangly', 'CODEFORCES')
ON DUPLICATE KEY UPDATE handle = VALUES(handle);

INSERT INTO crawl_logs (user_id, crawl_status, crawl_message)
SELECT id, 'Thành công', 'Dữ liệu mẫu để kiểm tra giao diện.'
FROM users
WHERE handle IN ('tourist', 'jiangly');
