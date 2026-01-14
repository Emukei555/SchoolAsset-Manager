package com.yourcompany.schoolasset.web.dto;

import jakarta.validation.constraints.NotNull;

public record LoanExecutionRequest(
        @NotNull(message = "予約IDは必須です")
        Long reservationId,

        @NotNull(message = "貸し出す機材個体のIDは必須です")
        Long assetId // バーコードやQRコードから読み取った個体ID
) {}