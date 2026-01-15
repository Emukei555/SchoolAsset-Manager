package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.AssetRepository;
import com.yourcompany.schoolasset.domain.model.asset.AssetStatus;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.user.Clerk;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.reservation.*;
import com.yourcompany.schoolasset.domain.model.asset.AssetStatus; // ついでにこちらも必要になるはずです

import com.yourcompany.schoolasset.domain.model.user.ClerkRepository;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecord;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationStatus;
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

@ExtendWith(MockitoExtension.class) // Mockitoを使ってテストするおまじない

class LoanServiceTest {


    // 実際のDBには繋がず、モック（偽物）のリポジトリを使います
    @Mock private ReservationRepository reservationRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private ClerkRepository clerkRepository;
    @Mock private LoanRecordRepository loanRecordRepository;


    // モックが注入されたテスト対象のサービス
    @InjectMocks private LoanService loanService;

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

        // Reservationをスパイ（実オブジェクトの振る舞いを残しつつ検証可能）
        Reservation mockReservation = spy(new Reservation());
        mockReservation.setId(reservationId);
        mockReservation.setModel(targetModel);

        // approve()を呼び出して状態を変更（スパイなので実際のメソッドが動く）
        doNothing().when(mockReservation).approve(any(Faculty.class)); // 必要ならモック
        mockReservation.approve(new Faculty());  // ← これでstatusがAPPROVEDになるはず

        // デバッグ用：状態を確認
        System.out.println("テスト中の状態: " + mockReservation.getStatus()); // ← ここでAPPROVEDか確認！

        Asset mockAsset = new Asset();
        mockAsset.setId(assetId);
        mockAsset.setStatus(AssetStatus.AVAILABLE);
        mockAsset.setModel(targetModel);

        Clerk mockClerk = new Clerk();
        mockClerk.setId(50L);

        LoanExecutionRequest request = new LoanExecutionRequest(reservationId, assetId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(assetRepository.findByIdWithLock(assetId)).thenReturn(Optional.of(mockAsset));
        when(clerkRepository.findByUserId(userId)).thenReturn(Optional.of(mockClerk));

        // --- 実行 ---
        loanService.executeLoan(request, userId);

        // --- 検証 ---
        assertEquals(ReservationStatus.LENT, mockReservation.getStatus());
        assertEquals(AssetStatus.LENT, mockAsset.getStatus());
        verify(loanRecordRepository, times(1)).save(any(LoanRecord.class));
    }
}