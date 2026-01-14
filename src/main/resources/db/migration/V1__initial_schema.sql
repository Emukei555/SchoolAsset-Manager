-- =============================================
-- Flyway Migration: V1__initial_schema.sql
-- 学校機材貸出管理システム - 初期スキーマ
-- Version: 1.0.1 (MVP修正)
-- 注意: このスクリプトは idempotent（再実行可能）になっています
-- =============================================

-- 1. 共通関数（最初に定義・再定義可能）
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. ユーザー基底テーブル
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) UNIQUE NOT NULL,
                                     password_hash VARCHAR(255) NOT NULL,
                                     role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'FACULTY', 'CLERK')),
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_users_updated_at'
              AND tgrelid = 'users'::regclass
        ) THEN
            CREATE TRIGGER update_users_updated_at
                BEFORE UPDATE ON users
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 3. 学生テーブル
CREATE TABLE IF NOT EXISTS students (
                                        user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                        student_number VARCHAR(20) UNIQUE NOT NULL,
                                        grade INTEGER NOT NULL CHECK (grade BETWEEN 1 AND 6),
                                        department VARCHAR(50) NOT NULL,
                                        is_suspended BOOLEAN DEFAULT FALSE,
                                        suspension_reason TEXT,
                                        graduation_date DATE,
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_students_updated_at'
              AND tgrelid = 'students'::regclass
        ) THEN
            CREATE TRIGGER update_students_updated_at
                BEFORE UPDATE ON students
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 4. 教員テーブル
CREATE TABLE IF NOT EXISTS faculties (
                                         user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                         faculty_code VARCHAR(20) UNIQUE NOT NULL,
                                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_faculties_updated_at'
              AND tgrelid = 'faculties'::regclass
        ) THEN
            CREATE TRIGGER update_faculties_updated_at
                BEFORE UPDATE ON faculties
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 5. 事務員テーブル
CREATE TABLE IF NOT EXISTS clerks (
                                      user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                      clerk_code VARCHAR(20) UNIQUE NOT NULL,
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_clerks_updated_at'
              AND tgrelid = 'clerks'::regclass
        ) THEN
            CREATE TRIGGER update_clerks_updated_at
                BEFORE UPDATE ON clerks
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 6. 機材カテゴリ
CREATE TABLE IF NOT EXISTS categories (
                                          id BIGSERIAL PRIMARY KEY,
                                          name VARCHAR(100) NOT NULL UNIQUE,
                                          description TEXT,
                                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_categories_updated_at'
              AND tgrelid = 'categories'::regclass
        ) THEN
            CREATE TRIGGER update_categories_updated_at
                BEFORE UPDATE ON categories
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 7. 機材モデル
CREATE TABLE IF NOT EXISTS models (
                                      id BIGSERIAL PRIMARY KEY,
                                      category_id BIGINT NOT NULL REFERENCES categories(id),
                                      name VARCHAR(255) NOT NULL,
                                      description TEXT,
                                      total_quantity INTEGER NOT NULL CHECK (total_quantity >= 0),
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_models_updated_at'
              AND tgrelid = 'models'::regclass
        ) THEN
            CREATE TRIGGER update_models_updated_at
                BEFORE UPDATE ON models
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 8. 個体（Asset）
CREATE TABLE IF NOT EXISTS assets (
                                      id BIGSERIAL PRIMARY KEY,
                                      model_id BIGINT NOT NULL REFERENCES models(id),
                                      serial_number VARCHAR(100) UNIQUE NOT NULL,
                                      status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                                          CHECK (status IN ('AVAILABLE', 'LENT', 'REPAIR', 'LOST', 'MAINTENANCE')),
                                      location VARCHAR(100),
                                      note TEXT,
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_assets_updated_at'
              AND tgrelid = 'assets'::regclass
        ) THEN
            CREATE TRIGGER update_assets_updated_at
                BEFORE UPDATE ON assets
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 9. 予約テーブル
CREATE TABLE IF NOT EXISTS reservations (
                                            id BIGSERIAL PRIMARY KEY,
                                            student_id BIGINT NOT NULL REFERENCES students(user_id),
                                            model_id BIGINT NOT NULL REFERENCES models(id),
                                            start_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            end_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                                CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
                                            approved_by BIGINT REFERENCES faculties(user_id),
                                            approved_at TIMESTAMP WITH TIME ZONE,
                                            reason TEXT,
                                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                            CONSTRAINT chk_reservation_dates CHECK (end_at > start_at)
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_reservations_updated_at'
              AND tgrelid = 'reservations'::regclass
        ) THEN
            CREATE TRIGGER update_reservations_updated_at
                BEFORE UPDATE ON reservations
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- 10. 貸出記録（model_id追加済み）
CREATE TABLE IF NOT EXISTS loan_records (
                                            id BIGSERIAL PRIMARY KEY,
                                            reservation_id BIGINT REFERENCES reservations(id),
                                            asset_id BIGINT NOT NULL REFERENCES assets(id),
                                            model_id BIGINT NOT NULL REFERENCES models(id),  -- 必須: 在庫計算用
                                            student_id BIGINT NOT NULL REFERENCES students(user_id),
                                            loaned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            due_date TIMESTAMP WITH TIME ZONE NOT NULL,
                                            returned_at TIMESTAMP WITH TIME ZONE,
                                            returned_by BIGINT REFERENCES clerks(user_id),
                                            note TEXT,
                                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger
            WHERE tgname = 'update_loan_records_updated_at'
              AND tgrelid = 'loan_records'::regclass
        ) THEN
            CREATE TRIGGER update_loan_records_updated_at
                BEFORE UPDATE ON loan_records
                FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
        END IF;
    END $$;

-- =============================================
-- インデックス（パフォーマンス最重要）
-- =============================================

CREATE INDEX IF NOT EXISTS idx_reservations_model_dates
    ON reservations (model_id, status, start_at, end_at)
    WHERE status = 'APPROVED';

CREATE INDEX IF NOT EXISTS idx_loan_records_model_active
    ON loan_records (model_id)
    WHERE returned_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_assets_serial_number
    ON assets (serial_number);

CREATE INDEX IF NOT EXISTS idx_loan_records_student_active
    ON loan_records (student_id)
    WHERE returned_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_models_category
    ON models (category_id);

CREATE INDEX IF NOT EXISTS idx_loan_records_model_student
    ON loan_records (model_id, student_id);

-- =============================================
-- ビュー：有効在庫計算用
-- =============================================
CREATE OR REPLACE VIEW model_availability AS
SELECT
    m.id AS model_id,
    m.name,
    m.total_quantity,
    COUNT(a.id) FILTER (WHERE a.status = 'AVAILABLE') AS available_assets,
    COUNT(lr.id) FILTER (WHERE lr.returned_at IS NULL) AS currently_lent,
    (
        SELECT COUNT(*)
        FROM reservations r
        WHERE r.model_id = m.id
          AND r.status = 'APPROVED'
          AND r.start_at < CURRENT_TIMESTAMP
          AND r.end_at > CURRENT_TIMESTAMP
    ) AS overlapping_reservations,
    GREATEST(0,
             COUNT(a.id) FILTER (WHERE a.status = 'AVAILABLE')
                 - COUNT(lr.id) FILTER (WHERE lr.returned_at IS NULL)
                 - (SELECT COUNT(*) FROM reservations r
                    WHERE r.model_id = m.id AND r.status = 'APPROVED'
                      AND r.start_at < CURRENT_TIMESTAMP AND r.end_at > CURRENT_TIMESTAMP)
    ) AS effective_available
FROM models m
         LEFT JOIN assets a ON a.model_id = m.id
         LEFT JOIN loan_records lr ON lr.model_id = m.id
GROUP BY m.id, m.name, m.total_quantity;