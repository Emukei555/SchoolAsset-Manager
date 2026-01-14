# SchoolAsset-Manager

学校機材貸出管理システム（API）

## 概要
学校内のPC・カメラなどの機材貸出をデジタル化するREST APIです。  
学生の予約申請 → 教員の承認 → 事務員の貸出・返却をサポートします。

## 技術スタック
- Java 21
- Spring Boot 3.4.1
- Spring Security + JWT
- Spring Data JPA + Flyway
- PostgreSQL
- Gradle

## 現在の状態
MVP開発中（認証・機材検索・予約申請を実装中）
ビルドはできるが現在ではまだ使える状態ではない

## ローカル起動方法
1. PostgreSQLを起動（docker-compose up -d）
2. `./gradlew bootRun`
3. http://localhost:8080 にアクセス

## 主な機能（MVP）
- JWT認証（学籍番号ベース）
- 機材検索（GET /api/v1/models）
- 予約申請（POST /api/v1/reservations）
- 在庫排他制御（楽観的ロック予定）

## 今後の予定
- 予約承認・貸出・返却フロー
- 延滞警告・通知
- ブラックリスト機能

## 設計ドキュメント
Notion: [リンクを貼る]

## データベース設計 (ER図)

以下はシステムのER図です（Mermaid記法）。

```mermaid
erDiagram
    %% ユーザー関連（継承構造）
    USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar role "STUDENT / FACULTY / CLERK"
        timestamptz created_at
        timestamptz updated_at
    }

    STUDENTS {
        bigint user_id PK,FK
        varchar student_number UK
        int grade
        varchar department
        boolean is_suspended
        text suspension_reason
        date graduation_date
        timestamptz created_at
        timestamptz updated_at
    }

    FACULTIES {
        bigint user_id PK,FK
        varchar faculty_code UK
        timestamptz created_at
        timestamptz updated_at
    }

    CLERKS {
        bigint user_id PK,FK
        varchar clerk_code UK
        timestamptz created_at
        timestamptz updated_at
    }

    %% 機材関連
    CATEGORIES {
        bigint id PK
        varchar name UK
        text description
        timestamptz created_at
        timestamptz updated_at
    }

    MODELS {
        bigint id PK
        bigint category_id FK
        varchar name
        text description
        int total_quantity
        timestamptz created_at
        timestamptz updated_at
    }

    ASSETS {
        bigint id PK
        bigint model_id FK
        varchar serial_number UK
        varchar status "AVAILABLE / LENT / REPAIR / LOST / MAINTENANCE"
        varchar location
        text note
        timestamptz created_at
        timestamptz updated_at
    }

    %% 予約・貸出関連
    RESERVATIONS {
        bigint id PK
        bigint student_id FK
        bigint model_id FK
        timestamptz start_at
        timestamptz end_at
        varchar status "PENDING / APPROVED / REJECTED / CANCELLED"
        bigint approved_by FK
        timestamptz approved_at
        text reason
        timestamptz created_at
        timestamptz updated_at
    }

    LOAN_RECORDS {
        bigint id PK
        bigint reservation_id FK
        bigint asset_id FK
        bigint model_id FK
        bigint student_id FK
        timestamptz loaned_at
        timestamptz due_date
        timestamptz returned_at
        bigint returned_by FK
        text note
        timestamptz created_at
        timestamptz updated_at
    }

    %% リレーション
    USERS ||--|| STUDENTS     : "1:1 (継承)"
    USERS ||--|| FACULTIES    : "1:1 (継承)"
    USERS ||--|| CLERKS       : "1:1 (継承)"

    CATEGORIES ||--o{ MODELS  : "1:N"
    MODELS     ||--o{ ASSETS  : "1:N"
    MODELS     ||--o{ RESERVATIONS : "1:N"
    STUDENTS   ||--o{ RESERVATIONS : "1:N"
    FACULTIES  ||--o{ RESERVATIONS : "1:N (approved_by)"

    ASSETS     ||--o{ LOAN_RECORDS : "1:N"
    STUDENTS   ||--o{ LOAN_RECORDS : "1:N"
    CLERKS     ||--o{ LOAN_RECORDS : "1:N (returned_by)"
    RESERVATIONS ||--o| LOAN_RECORDS : "1:0..1 (予約→貸出昇格)"
