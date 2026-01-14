package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.asset.ModelRepository;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation.ReservationStatus;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationRepository;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentRepository;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationRepository;
import com.yourcompany.schoolasset.web.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final StudentRepository studentRepository;
    private final ModelRepository modelRepository;
    private final ReservationRepository reservationRepository;
    private final LoanRecordRepository loanRecordRepository;

    @Transactional
    public void createReservation(Long studentId, ReservationRequest request) {

        // ==========================================
        // 1. 学生の資格チェック (Domain Logic)
        // ==========================================
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_DISABLED)); // または適切なNOT_FOUNDエラー

        // Repositoryから判断材料を集める
        int activeLoans = loanRecordRepository.countActiveLoansByStudentId(studentId);
        boolean hasOverdue = loanRecordRepository.existsOverdueByStudentId(studentId, LocalDateTime.now());

        // Domainに判断させる
        if (!student.canBorrow(activeLoans, hasOverdue)) {
            // 細かい理由は省き、代表して延滞等のエラーを返す（必要ならErrorCodeを分岐させる）
            throw new BusinessException(ErrorCode.SUSPENDED);
        }

        // ==========================================
        // 2. 機材モデルの存在確認
        // ==========================================
        Model model = modelRepository.findById(request.modelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OUT_OF_STOCK)); // モデル自体がない場合も在庫なし扱い等

        // ==========================================
        // 3. 有効在庫(X)の計算
        // ==========================================
        int totalQuantity = model.getTotalQuantity();
        int currentLoans = loanRecordRepository.countActiveLoansByModelId(model.getId());
        int overlappingReservations = reservationRepository.countOverlappingReservations(
                model.getId(), request.startAt(), request.endAt());

        // ★ 有効在庫 = 総数 - (貸出中 + 予約済み)
        int effectiveStock = totalQuantity - (currentLoans + overlappingReservations);

        if (effectiveStock <= 0) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        // ==========================================
        // 4. 予約の確定と保存
        // ==========================================
        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setModel(model);
        reservation.setStartAt(request.startAt());
        reservation.setEndAt(request.endAt());
        reservation.setStatus(ReservationStatus.PENDING); // 承認待ち

        reservationRepository.save(reservation);
    }
}