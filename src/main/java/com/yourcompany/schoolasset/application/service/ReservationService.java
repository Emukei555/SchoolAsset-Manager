package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.asset.ModelRepository;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.faculty.FacultyRepository;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationPeriod;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationStatus;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.reservation.ReservationRepository;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentRepository;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.web.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final StudentRepository studentRepository;
    private final ModelRepository modelRepository;
    private final ReservationRepository reservationRepository;
    private final LoanRecordRepository loanRecordRepository;
    private final FacultyRepository facultyRepository;

    @Transactional
    public void createReservation(Long studentId, ReservationRequest request) {

        // 1. 学生の取得と資格チェック
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 予約期間(Value Object)の生成
        // ここで「開始 > 終了」などのバリデーションが自動的に行われる
        ReservationPeriod period = new ReservationPeriod(request.startAt(), request.endAt());

        // 3. 資格チェック (Domain Logic)
        int activeLoans = loanRecordRepository.countActiveLoansByModelId(studentId);
        boolean hasOverdue = loanRecordRepository.existsOverdueByStudentId(studentId, LocalDateTime.now());

        if (!student.canBorrow(activeLoans, hasOverdue)) {
            throw new BusinessException(ErrorCode.SUSPENDED);
        }

        // 4. 機材モデルの存在確認
        Model model = modelRepository.findById(request.modelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OUT_OF_STOCK));

        // 5. 有効在庫の計算
        int totalQuantity = model.getTotalQuantity();
        int currentLoans = loanRecordRepository.countActiveLoansByModelId(model.getId());

        // VOのメソッドを使って重複判定ができるようになります（Repository側もVO対応するとさらに綺麗になります）
        int overlappingReservations = reservationRepository.countOverlappingReservations(
                model.getId(), period.getStartAt(), period.getEndAt());

        int effectiveStock = totalQuantity - (currentLoans + overlappingReservations);

        if (effectiveStock <= 0) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        // 6. 予約エンティティの生成 (コンストラクタを使用)
        // セッターを使わず、完全な状態でオブジェクトを作る
        Reservation reservation = new Reservation(student, model, period);

        reservationRepository.save(reservation);
        log.info("新規予約を作成しました。学生ID: {}, モデルID: {}, 期間: {} ~ {}",
                studentId, model.getId(), period.getStartAt(), period.getEndAt());
    }

    @Transactional
    public void approveReservation(Long reservationId, Long facultyId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_DISABLED));

        // ドメインモデル内の approve メソッドがステータス変更とバリデーションを担う
        reservation.approve(faculty);

        reservationRepository.save(reservation);

        log.info("予約の承認が完了しました。予約ID: {}, 承認者: {}, ステータス: {}",
                reservationId, facultyId, reservation.getStatus());
    }
}