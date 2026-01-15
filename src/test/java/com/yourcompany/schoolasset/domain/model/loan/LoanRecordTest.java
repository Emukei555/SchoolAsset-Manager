package com.yourcompany.schoolasset.domain.model.loan;

import static org.junit.jupiter.api.Assertions.*;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoanRecordTest {

    @Test
    @DisplayName("正常系: 未返却なら現在日時が入る")
    void markAsReturned_success() {
        // Arrange (準備)
        // ※createメソッドがあるならそれを使う、なければ new してリフレクション等でセット
        // ここでは new できる前提で書きます
        LoanRecord record = new LoanRecord();

        // Act (実行)
        record.markAsReturned();

        // Assert (検証)
        assertNotNull(record.getReturnedAt(), "返却日時がセットされていること");
    }

    @Test
    @DisplayName("異常系: 既に返却済みならエラーになる")
    void markAsReturned_fail_ifAlreadyReturned() {
        // Arrange
        LoanRecord record = new LoanRecord();
        record.markAsReturned(); // 一度返却済みにする

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            record.markAsReturned(); // 二度目の返却
        });

        // エラーコードが正しいか確認
        assertEquals(ErrorCode.ALREADY_RETURNED, ex.getErrorCode());
    }
}