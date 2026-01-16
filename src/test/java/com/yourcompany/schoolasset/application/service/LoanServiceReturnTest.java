package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.AssetStatus;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecord;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationPeriod;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationStatus;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentNumber;
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

        // 必要な依存データの準備
        Asset mockAsset = new Asset();
        mockAsset.setStatus(AssetStatus.LENT);

        // Value Object の作成
        StudentNumber sn = new StudentNumber("20230001"); // 8桁ルール
        ReservationPeriod period = new ReservationPeriod(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        // 予約の作成（最初は PENDING 状態）
        Reservation mockReservation = new Reservation(new Student(), new Model(), period);

        // 【重要】ドメインルールに従って LENT 状態まで遷移させる
        // PENDING -> APPROVED
        mockReservation.approve(new Faculty());
        // APPROVED -> LENT
        mockReservation.markAsLent();

        Clerk mockClerk = new Clerk();
        // LoanRecordの作成
        LoanRecord mockLoanRecord = LoanRecord.create(mockReservation, mockAsset, mockClerk);

        when(loanRecordRepository.findById(loanId)).thenReturn(Optional.of(mockLoanRecord));

        // --- 2. 実行 (Act) ---
        loanService.returnLoan(loanId);

        // --- 3. 検証 (Assert) ---
        assertEquals(ReservationStatus.COMPLETED, mockReservation.getStatus(),
                "予約がCOMPLETEDになっていること");
        assertEquals(AssetStatus.AVAILABLE, mockAsset.getStatus(),
                "機材がAVAILABLEに戻っていること");
        assertNotNull(mockLoanRecord.getReturnedAt(),
                "返却日時が記録されていること");
    }
}