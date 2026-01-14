-- 1. テスト用ユーザー（パスワードは 'password' を BCrypt でハッシュ化したもの）
INSERT INTO users (email, password_hash, role) VALUES
                                                   ('student@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8q6uy596HW99O3.8P7h/C.Ccl/6S0KqR4K.', 'STUDENT'),
                                                   ('faculty@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8q6uy596HW99O3.8P7h/C.Ccl/6S0KqR4K.', 'FACULTY'),
                                                   ('clerk@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8q6uy596HW99O3.8P7h/C.Ccl/6S0KqR4K.', 'CLERK');

-- 2. 学生・教員・事務員詳細
INSERT INTO students (user_id, student_number, grade, department) VALUES
    ((SELECT id FROM users WHERE email = 'student@example.com'), 'S2024001', 2, '情報工学科');

INSERT INTO faculties (user_id, faculty_code) VALUES
    ((SELECT id FROM users WHERE email = 'faculty@example.com'), 'F1001');

INSERT INTO clerks (user_id, clerk_code) VALUES
    ((SELECT id FROM users WHERE email = 'clerk@example.com'), 'C9001');

-- 3. カテゴリとモデル
INSERT INTO categories (name, description) VALUES
    ('ノートPC', '貸出用の標準的なラップトップ');

INSERT INTO models (category_id, name, description, total_quantity) VALUES
    ((SELECT id FROM categories WHERE name = 'ノートPC'), 'MacBook Pro 14', 'M3 Proモデル', 5);

-- 4. 個体（Asset）
INSERT INTO assets (model_id, serial_number, status, location) VALUES
                                                                   ((SELECT id FROM models WHERE name = 'MacBook Pro 14'), 'MBP-001', 'AVAILABLE', '機材庫A'),
                                                                   ((SELECT id FROM models WHERE name = 'MacBook Pro 14'), 'MBP-002', 'AVAILABLE', '機材庫A'),
                                                                   ((SELECT id FROM models WHERE name = 'MacBook Pro 14'), 'MBP-003', 'LENT', '学生貸出中');

-- 5. すでに存在する貸出記録（テスト用）
INSERT INTO loan_records (asset_id, model_id, student_id, loaned_at, due_date) VALUES
    (
        (SELECT id FROM assets WHERE serial_number = 'MBP-003'),
        (SELECT id FROM models WHERE name = 'MacBook Pro 14'),
        (SELECT user_id FROM students WHERE student_number = 'S2024001'),
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP + INTERVAL '6 days'
    );