package com.yourcompany.schoolasset.domain.model.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationPeriod {
    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    public ReservationPeriod(LocalDateTime startAt, LocalDateTime endAt) {
        LocalDateTime now = LocalDateTime.now();

        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("開始日時と終了日時は必須です");
        }

        if (startAt.isAfter(endAt)) {
            throw new IllegalArgumentException("開始日時は終了日時より前である必要があります");
        }

        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("開始日時は終了日時より前である必要があります");
        }

        // 1. 開始日時が過去でないか
        if (startAt.isBefore(now)) {
            throw new IllegalArgumentException("開始日時に過去の日付を指定することはできません");
        }

        // 2. 開始が終了より前か
        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("開始日時は終了日時より前である必要があります");
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }


    // 期間の重複判定ロジック
    public boolean overlaps(ReservationPeriod other) {
        return this.startAt.isBefore(other.endAt) && other.startAt.isBefore(this.endAt);
    }

    // 現在この期間中かどうか
    public boolean isActiveNow() {
        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(startAt) || now.isAfter(startAt)) && now.isBefore(endAt);
    }
}
