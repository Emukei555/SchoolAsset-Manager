package com.yourcompany.schoolasset.domain.model.reservation;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.prefs.BackingStoreException;

@Entity
@Table(name = "reservations")
@Getter @Setter @NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    public enum ReservationStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }

    public void approve(Long facultyId) {
        // TODO [STATE-401] 状態遷移のガード
        // 1. 【バリデーション】
        if (this.status != ReservationStatus.PENDING) {
            // TODO [ERR-002] 独自の BusinessException への置き換え検討
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        // 2. 【状態変更】
        // TODO [FIELD-101] approvedBy と approvedAt フィールドが未定義なら追加が必要
        this.status = ReservationStatus.APPROVED;
        this.approvedBy = facultyId;
        this.approvedAt = LocalDateTime.now();

        // TODO [AUDIT-101] 監査ログ用の整合性チェック
        // 日本語ロジック：facultyId が NULL でないこと、approvedAt が現在時刻であることを保証
    }
}