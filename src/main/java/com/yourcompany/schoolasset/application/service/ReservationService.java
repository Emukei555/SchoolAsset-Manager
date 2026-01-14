package com.yourcompany.schoolasset.application.service;

import com.yourcompany.schoolasset.domain.model.asset.AssetRepository;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.asset.ModelRepository;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentRepository;
import com.yourcompany.schoolasset.web.ReservationRepository; // パッケージ注意
import com.yourcompany.schoolasset.web.dto.ReservationRequest;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
// 独自例外クラスがない場合はRuntimeExceptionなどで代用
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    // 必要なリポジトリを注入
    private final StudentRepository studentRepository;
    private final ModelRepository modelRepository;
    private final ReservationRepository reservationRepository; // 型を修正
    private final AssetRepository assetRepository;
    private final LoanRecordRepository loanRecordRepository;

    /**
     * 新規予約を作成する
     * @param studentId 申請者のID
     * @param request リクエストDTO
     */
    @Transactional
    public void createReservation(Long studentId, ReservationRequest request) {

        // ==========================================
        // 1. 学生の資格チェック
        // ==========================================
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生が見つかりません")); // 仮の例外

        // TODO: 本来は !student.canBorrow() でチェックする
        // 一旦パスさせる
        if (false) {
            throw new RuntimeException(ErrorCode.SUSPENDED.getDefaultMessage());
        }

        // ==========================================
        // 2. 機材モデルの存在確認
        // ==========================================
        Model model = modelRepository.findById(request.modelId())
                .orElseThrow(() -> new RuntimeException("機材モデルが見つかりません"));

        // ==========================================
        // 3. 有効在庫(X)の計算 (仮実装)
        // ==========================================

        // TODO: 重複予約数をカウントする
        // int overlappingReservations = reservationRepository.countOverlappingReservations(
        //         request.modelId(),
        //         request.startAt(),
        //         request.endAt()
        // );
        int overlappingReservations = 0; // 仮：重複なしとする

        int totalQuantity = model.getTotalQuantity();

        // A. そのモデルの「貸出可能(AVAILABLE)」な総数
        // TODO: 実装後にコメントアウト解除
        // int totalAssets = assetRepository.countAvailableByModelId(model.getId());
        int totalAssets = 10; // 仮：10台あるとする

        // B. 現在「貸出中(LENT)」でまだ返ってきていない数
        // TODO: 実装後にコメントアウト解除
        // int currentLoans = loanRecordRepository.countActiveLoansByModelId(model.getId());
        int currentLoans = 0; // 仮：貸出中なしとする

        // ★ 計算式: X = 総数 - (貸出中 + 予約済み)
        // 本来は totalAssets を使うが、簡易的に totalQuantity を使う場合もある
        int effectiveStock = totalQuantity - currentLoans - overlappingReservations;

        // ==========================================
        // 4. 在庫判定
        // ==========================================
        if (effectiveStock <= 0) {
            // throw new RuntimeException(ErrorCode.OUT_OF_STOCK.getDefaultMessage());
            // TODO: 在庫不足エラー
            System.out.println("在庫不足ですが、仮実装のためスルーします");
        }

        // ==========================================
        // 5. 予約の確定と保存
        // ==========================================
        // Reservation エンティティがまだ作成されていない、または修正が必要なためコメントアウト
        /*
        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setModel(model);
        reservation.setStartAt(request.startAt());
        reservation.setEndAt(request.endAt());
        reservation.setStatus(ReservationStatus.PENDING);

        reservationRepository.save(reservation);
        */

        // 仮ログ出力
        log.info("予約を受け付けました (仮): " + request);
    }
}