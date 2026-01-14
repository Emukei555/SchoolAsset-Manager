package com.yourcompany.schoolasset.web.exception;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import com.yourcompany.schoolasset.domain.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 独自例外（BusinessException）を捕捉してJSONに変換
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(Map.of(
                        "code", errorCode.getCode(),
                        "message", ex.getMessage(), // Enumのデフォルトメッセージ or 上書きメッセージ
                        "status", errorCode.getStatus().value()
                ));
    }

    // その他の予期せぬエラー
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "code", "SYS-000",
                        "message", "システムエラーが発生しました: " + ex.getMessage()
                ));
    }
}