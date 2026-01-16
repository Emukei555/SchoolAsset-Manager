package com.yourcompany.schoolasset.domain.model.reservation;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 無闇な生成を防ぐ
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

    // ★ startAt, endAt フィールドを削除し、period に集約
    @Embedded
    private ReservationPeriod period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Faculty approvedBy;

    private LocalDateTime approvedAt;

    /**
     * 新規作成用コンストラクタ
     */
    public Reservation(Student student, Model model, ReservationPeriod period) {
        this.student = student;
        this.model = model;
        this.period = period;
        this.status = ReservationStatus.PENDING;
    }

    /**
     * 予約を承認状態にする
     */
    public void approve(Faculty faculty) {
        // ビジネスルール: 承認できるのはPENDING状態のみ
        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.APPROVED;
        this.approvedBy = faculty;
        this.approvedAt = LocalDateTime.now();
    }

    public void markAsLent() {
        if (this.status != ReservationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.LENT;
    }

    public void complete() {
        if (this.status != ReservationStatus.LENT) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.COMPLETED;
    }

    // 既存コードとの互換性のための委譲メソッド
    public LocalDateTime getStartAt() {
        return period.getStartAt();
    }

    public LocalDateTime getEndAt() {
        return period.getEndAt();
    }
}