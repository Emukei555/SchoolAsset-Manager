package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import com.yourcompany.schoolasset.domain.model.asset.ModelRepository;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentRepository;
import com.yourcompany.schoolasset.web.ReservationRepository;
import com.yourcompany.schoolasset.web.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final StudentRepository studentRepository;
    private final ModelRepository modelRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void createReservation(Long studentId, ReservationRequest request) {

        // 1. 学生の存在確認と資格チェック
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("指定された学生が見つかりません"));

        if (!student.canBorrow()) {
            throw new RuntimeException("貸出停止中のため予約できません");
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

        // シンプルな在庫判定: モデルの総数 - 重複予約数
        int effectiveStock = model.getTotalQuantity() - overlappingReservations;

        if (effectiveStock <= 0) {
            throw new RuntimeException("指定された期間は在庫不足です");
        }

        // 4. 予約の保存
        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setModel(model);
        reservation.setStartAt(request.startAt());
        // TODO: setAtは現在クラスが存在するだけで実装されて居ません
        reservation.setAt(request.endAt());
        reservation.setStatus(Reservation.ReservationStatus.PENDING); // 初期状態は承認待ち

        reservationRepository.save(reservation);
    }
}