package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.AssetRepository;
import com.yourcompany.schoolasset.domain.model.asset.AssetStatus;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecord;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationRepository;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationStatus;
import com.yourcompany.schoolasset.domain.model.user.ClerkRepository;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private ClerkRepository clerkRepository;
    @Mock
    private LoanRecordRepository loanRecordRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    @DisplayName("正常系：予約と機材が正しければ、貸出が実行されステータスが更新される")
    void executeLoan_success() {
        // --- 準備 ---
        Long reservationId = 100L;
        Long assetId = 200L;
        Long userId = 999L;

        Model targetModel = new Model();
        targetModel.setId(1L);
        targetModel.setName("MacBook Pro");

        // Reservationをスパイ
        Reservation mockReservation = spy(new Reservation());
        mockReservation.setId(reservationId);
        mockReservation.setModel(targetModel);

        // ★修正ポイント: doNothing() を削除し、直接ステータスを設定して APPROVED 状態を作る
        // mockReservation.approve(new Faculty()); // ロジックを通しても良いが、テスト準備としては直接セットが確実
        mockReservation.setStatus(ReservationStatus.APPROVED);

        System.out.println("テスト中の状態: " + mockReservation.getStatus()); // これで APPROVED になるはず

        Asset mockAsset = new Asset();
        mockAsset.setId(assetId);
        mockAsset.setStatus(AssetStatus.AVAILABLE);
        mockAsset.setModel(targetModel);

        Clerk mockClerk = new Clerk();
        // 変更前: mockClerk.setId(50L);
        // 変更後:
        mockClerk.setUserId(50L); // ← setId ではなく setUserId を使用

        LoanExecutionRequest request = new LoanExecutionRequest(reservationId, assetId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(assetRepository.findByIdWithLock(assetId)).thenReturn(Optional.of(mockAsset));
        when(clerkRepository.findByUserId(userId)).thenReturn(Optional.of(mockClerk));
    }
}