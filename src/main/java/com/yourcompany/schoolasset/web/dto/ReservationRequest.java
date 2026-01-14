package com.yourcompany.schoolasset.web.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// Java Recordを使って不変（Immutable）なデータを定義
public record ReservationRequest(

        @NotNull(message = "機材モデルIDは必須です")
        Long modelId,

        @NotNull(message = "開始日時は必須です")
        @Future(message = "開始日時は未来を指定してください")
        LocalDateTime startAt,

        @NotNull(message = "終了日時は必須です")
        @Future(message = "終了日時は未来を指定してください")
        LocalDateTime endAt,

        String reason // 任意項目


) {
    /**
     * 相関チェック：アノテーションだけでは難しい「項目間の整合性」をチェック
     */
    @AssertTrue(message = "終了日時は開始日時より後に設定してください")
    public boolean isValidPeriod() {
        if (startAt == null || endAt == null) return true; // 個別チェックに任せる
        return endAt.isAfter(startAt);
    }
}