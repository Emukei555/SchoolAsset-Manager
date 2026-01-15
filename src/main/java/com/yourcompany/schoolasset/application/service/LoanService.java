package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.AssetRepository;
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import com.yourcompany.schoolasset.domain.model.user.ClerkRepository;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecord;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationRepository;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final ReservationRepository reservationRepository;
    private final LoanRecordRepository loanRecordRepository;
    private final AssetRepository assetRepository;
    private final ClerkRepository clerkRepository;

    /**
     * 貸出を実行する
     */
    @Transactional
    public void executeLoan(LoanExecutionRequest request, Long userId) {
        // 1. 予約を取得する
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 機材をロックして取得する（悲観的ロック）
        Asset asset = assetRepository.findByIdWithLock(request.assetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 3. 事務員（操作者）を取得する
        Clerk clerk = clerkRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_DISABLED));

        // 4. モデルが一致するか確認する
        if (!reservation.getModel().equals(asset.getModel())) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        // 5. 予約を貸出中にする（ドメインロジック）
        reservation.markAsLent();

        // 6. 機材を貸出中にする（ドメインロジック）
        asset.rentOut();

        // 7. 貸出記録を作成する（ファクトリメソッド）
        LoanRecord loanRecord = LoanRecord.create(reservation, asset, clerk);

        // 8. 貸出記録を保存する
        loanRecordRepository.save(loanRecord);
    }

    /**
     * 返却を実行する
     */
    @Transactional
    public void returnLoan(Long loanId) {
        // 1. 貸出記録を取得する
        LoanRecord loanRecord = loanRecordRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 返却処理を行う（ドメインロジック）
        loanRecord.markAsReturned();

        // 3. 予約を完了にする
        loanRecord.getReservation().complete();

        // 4. 機材を在庫に戻す
        loanRecord.getAsset().returnBack();

        // 5. 変更を保存する（Dirty Checkingにより自動更新されるが明示的にsave）
        loanRecordRepository.save(loanRecord);
    }
}