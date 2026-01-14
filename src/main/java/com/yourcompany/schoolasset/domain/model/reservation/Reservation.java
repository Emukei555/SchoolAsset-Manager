package com.yourcompany.schoolasset.domain.model.reservation;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Faculty approvedBy;

    private LocalDateTime approvedAt;

    public enum ReservationStatus {
        PENDING, APPROVED, REJECTED, CANCELLED, LENT;
    }

    public void markAsLent() {
        if (this.status != ReservationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.LENT;
    }

    /**
     * 予約を承認状態にする
     */
    public void approve(Faculty faculty) {
        // ... (以前実装したバリデーション)
        this.status = ReservationStatus.APPROVED;
        this.approvedBy = faculty;
        this.approvedAt = LocalDateTime.now();
    }
}