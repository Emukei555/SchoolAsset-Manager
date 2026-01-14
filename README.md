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
ER図: [Mermaidコードか画像リンク]
