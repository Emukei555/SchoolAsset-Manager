package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.AssetRepository;
import com.yourcompany.schoolasset.domain.model.asset.AssetStatus;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationPeriod;
import com.yourcompany.schoolasset.domain.model.student.Student;
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

import java.time.LocalDateTime;
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
        // --- 1. 準備 (Arrange) ---
        Long reservationId = 100L;
        Long assetId = 200L;
        Long userId = 999L;

        // Modelの準備
        Model targetModel = new Model();
        targetModel.setId(1L);
        targetModel.setName("MacBook Pro");

        // ★修正ポイント1: ReservationPeriod(VO)を作成
        ReservationPeriod period = new ReservationPeriod(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        // ★修正ポイント2: 正しいコンストラクタでReservationを作成
        // (引数: Student, Model, Period)
        Reservation mockReservation = new Reservation(new Student(), targetModel, period);
        mockReservation.setId(reservationId);

        // ★修正ポイント3: ドメインメソッドを呼んで APPROVED 状態にする
        // これにより内部のバリデーションをパスしつつ、正しい状態遷移をシミュレートできる
        mockReservation.approve(new Faculty());

        // Assetの準備
        Asset mockAsset = new Asset();
        mockAsset.setId(assetId);
        mockAsset.setStatus(AssetStatus.AVAILABLE);
        mockAsset.setModel(targetModel);

        // Clerkの準備
        Clerk mockClerk = new Clerk();
        mockClerk.setUserId(50L);

        LoanExecutionRequest request = new LoanExecutionRequest(reservationId, assetId);

        // モックの設定
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(assetRepository.findByIdWithLock(assetId)).thenReturn(Optional.of(mockAsset));
        when(clerkRepository.findByUserId(userId)).thenReturn(Optional.of(mockClerk));

        // --- 2. 実行 (Act) ---
        loanService.executeLoan(request, userId);

        // --- 3. 検証 (Assert) ---
        assertEquals(ReservationStatus.LENT, mockReservation.getStatus());
        assertEquals(AssetStatus.LENT, mockAsset.getStatus());
        verify(loanRecordRepository, times(1)).save(any());
    }
}