package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.AssetStatus;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecord;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationStatus;
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceReturnTest {

    @Mock
    private LoanRecordRepository loanRecordRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    @DisplayName("正常系：返却を実行すると、貸出記録・機材・予約の状態が正しく更新されること")
    void returnLoan_success() {
        // --- 1. 準備 (Arrange) ---
        Long loanId = 1L;

        Asset mockAsset = new Asset();
        mockAsset.setStatus(AssetStatus.LENT);

        // 【かな型】予約を「貸出中」として確実に準備する
        Reservation mockReservation = new Reservation();

        // ★修正ポイント: 直接代入で確実に LENT にする（メソッド経由だとバリデーションで弾かれる可能性があるため）
        mockReservation.setStatus(ReservationStatus.LENT);

        // もし setter がない場合、リフレクションを使って強制的に書き換えるか、
        // 初期状態を制御できるファクトリメソッドを確認してください。

        Clerk mockClerk = new Clerk();
        LoanRecord mockLoanRecord = LoanRecord.create(mockReservation, mockAsset, mockClerk);

        when(loanRecordRepository.findById(loanId)).thenReturn(Optional.of(mockLoanRecord));

        // --- 2. 実行 (Act) ---
        loanService.returnLoan(loanId);

        // --- 3. 検証 (Assert) ---
        // ここで期待値 COMPLETED と実際の値が一致するか確認
        assertEquals(ReservationStatus.COMPLETED, mockReservation.getStatus(), "予約がCOMPLETEDになっていること");
        // ... (以下略)
    }
}