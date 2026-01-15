package com.yourcompany.schoolasset.domain.model.loan;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.prefs.BackingStoreException;

@Entity
@Table(name = "loan_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanRecord {

    // --- ここを追加してください ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ---------------------------

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "clerk_id", nullable = false)
    private Clerk clerk;

    private LocalDateTime loanedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;

    public void markAsReturned() {
        if (this.returnedAt != null) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED); // "既に返却済みです"
        }
        this.returnedAt = LocalDateTime.now();
    }

    // ファクトリーメソッド（作成ロジック）
    public static LoanRecord create(Reservation reservation, Asset asset, Clerk clerk) {
        LoanRecord record = new LoanRecord();
        record.reservation = reservation;
        record.asset = asset;
        record.clerk = clerk;
        record.loanedAt = LocalDateTime.now();
        record.dueDate = reservation.getEndAt();
        return record;
    }
}