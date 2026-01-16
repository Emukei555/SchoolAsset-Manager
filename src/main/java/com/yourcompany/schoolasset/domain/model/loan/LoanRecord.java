package com.yourcompany.schoolasset.domain.model.loan;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.student.Student; // ★追加
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    // ★★★★★ 【今回追加】これが抜けていました！ ★★★★★
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clerk_id", nullable = false)
    private Clerk clerk;

    private LocalDateTime loanedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;

    public void markAsReturned() {
        if (this.returnedAt != null) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED);
        }
        this.returnedAt = LocalDateTime.now();
    }

    // ファクトリーメソッド
    public static LoanRecord create(Reservation reservation, Asset asset, Clerk clerk) {
        LoanRecord record = new LoanRecord();

        record.reservation = reservation;
        record.asset = asset;
        record.clerk = clerk;
        record.model = asset.getModel();

        // ★★★★★ 【今回追加】予約情報から学生をセット ★★★★★
        record.student = reservation.getStudent();

        record.loanedAt = LocalDateTime.now();
        record.dueDate = reservation.getEndAt();

        return record;
    }
}