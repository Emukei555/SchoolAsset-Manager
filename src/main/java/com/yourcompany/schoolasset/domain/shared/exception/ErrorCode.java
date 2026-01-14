package com.yourcompany.schoolasset.domain.shared.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 1. 定数リストを一番上に書く。各要素はカンマ「,」で区切り、最後だけセミコロン「;」
    ACCOUNT_DISABLED("STU-001", HttpStatus.FORBIDDEN, "アカウントが無効です。"),
    SUSPENDED("STU-002", HttpStatus.FORBIDDEN, "貸出停止中です。"),
    OVERDUE_RESTRICTION("STU-003", HttpStatus.FORBIDDEN, "延滞機材があります。"),
    OUT_OF_STOCK("AST-001", HttpStatus.CONFLICT, "在庫がありません。"),
    UNDER_REPAIR("AST-002", HttpStatus.CONFLICT, "修理中です。"),
    RESERVATION_CONFLICT("AST-003", HttpStatus.CONFLICT, "予約が重複しています。"),
    NOT_FOUND("AST-004", HttpStatus.NOT_FOUND, "機材が存在しません"),

    INVALID_RESERVATION_STATUS("RES-001", HttpStatus.CONFLICT, "この予約は既に処理済みのため、操作を継続できません。");

    // 2. フィールド定義
    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    // 3. コンストラクタ（クラス名と同じ名前である必要がある）
    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}