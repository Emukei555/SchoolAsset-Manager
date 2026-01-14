package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.asset.ModelRepository;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentRepository;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationRepository;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
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

        // 1. 学生の存在確認と資格チェック
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("指定された学生が見つかりません"));


        int activeLoans = loanRecordRepository.countActiveLoansByStudentId(studentId); // 未返却数
        boolean overdue = loanRecordRepository.existsOverdueByStudentId(studentId, LocalDateTime.now());     // 延滞有無

        if (!student.canBorrow(activeLoans, overdue)) {
            throw new RuntimeException("貸出条件を満たしていません");
        }

        // 2. 機材モデルの存在確認
        Model model = modelRepository.findById(request.modelId())
                .orElseThrow(() -> new RuntimeException("指定された機材モデルが見つかりません"));

        // 3. 有効在庫(X)の計算 (ここがMVPの肝)
        int overlappingReservations = reservationRepository.countOverlappingReservations(
                model.getId(),
                request.startAt(),
                request.endAt()
        );

        int total = model.getTotalQuantity(); //
        int loans = loanRecordRepository.countActiveLoansByModelId(model.getId());
        int reservations = reservationRepository.countOverlappingReservations(model.getId(), request.startAt(), request.endAt()); //

        int effectiveStock = total - (loans + reservations);

        if (effectiveStock <= 0) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK); //
        }
        // 4. 予約の保存
        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setModel(model);
        reservation.setStartAt(request.startAt());
        reservation.setEndAt(request.endAt());
        reservation.setStatus(Reservation.ReservationStatus.PENDING); // 初期状態は承認待ち

        reservationRepository.save(reservation);
    }
}