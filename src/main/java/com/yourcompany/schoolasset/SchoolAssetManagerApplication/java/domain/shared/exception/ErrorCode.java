package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.shared.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 学生関連 (STU)
    ACCOUNT_DISABLED("STU-001", HttpStatus.FORBIDDEN, "アカウントが無効です。"),
    SUSPENDED("STU-002", HttpStatus.FORBIDDEN, "貸出停止中です。"),
    OVERDUE_RESTRICTION("STU-003", HttpStatus.FORBIDDEN, "延滞機材があります。"),

    // 機材関連 (AST)
    OUT_OF_STOCK("AST-001", HttpStatus.CONFLICT, "在庫がありません。"),
    UNDER_REPAIR("AST-002", HttpStatus.CONFLICT, "修理中です。"),
    RESERVATION_CONFLICT("AST-003", HttpStatus.CONFLICT, "予約が重複しています。");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
