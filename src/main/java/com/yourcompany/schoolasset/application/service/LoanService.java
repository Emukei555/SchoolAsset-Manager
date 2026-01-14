package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.application.service.ReservationService;
import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecord;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationRepository;
import com.yourcompany.schoolasset.domain.model.asset.AssetRepository; // 仮
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import com.yourcompany.schoolasset.domain.model.user.ClerkRepository;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class LoanService {

    private final ReservationRepository reservationRepository;
    private final LoanRecordRepository loanRecordRepository;
    private final AssetRepository assetRepository;// TODO: まだ作ってなければ作成が必要
    private final ClerkRepository clerkRepository;

    /**
     * 貸出実行（事務員用）
     * 予約を消化し、物理的な機材を貸出中に変更する
     */
    @Transactional
    public void executeLoan(LoanExecutionRequest request, Long userId) {
        // 1. 【予約確認】
        LoanRecord loanRecord = loanRecordRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS));

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 【機材ロック】物理的なモノの確保
        // TODO [LOAN-102] 悲観的ロック (Pessimistic Lock) で Asset を取得
        // TODO findByIdWithLockの中身は実装された居ません
        // 3. 【整合性チェック】(予約したモデルと一致するか？)
        Asset asset = assetRepository.findByIdWithLock(request.assetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!reservation.getModel().equals(asset.getModel())) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        // 4. 【ドメイン操作】貸出記録の作成とステータス更新
        // TODO [LOAN-104] LoanRecord エンティティの生成 (new LoanRecord(...))
        // TODO [LOAN-105] Assetステータスを LENT に変更
        // TODO [LOAN-106] Reservationステータスを LENT (または COMPLETED) に変更
        asset.rentOut();
        reservation.markAsLent();

        Clerk clerk = clerkRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_DISABLED));


        LoanRecord loanRecord2 = LoanRecord.create(reservation, asset, clerk);
        // 5. 【永続化】
        loanRecordRepository.save(loanRecord);
    }
}
