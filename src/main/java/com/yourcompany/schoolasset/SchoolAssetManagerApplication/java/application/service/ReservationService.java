package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.application.service;


import com.sun.source.tree.CompilationUnitTree;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset.Model;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset.ModelRepository;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.student.Student;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.web.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    // 必要なリポジトリ（DB操作係）を注入
    // TODO 後に実装
    // private final StudentRepository studentRepository;
    private final ModelRepository modelRepository;
    private final ReservationRequest reservationRepository;
    // TODO 後に実装
    // private final AssetRepository assetRepository;

    /**
     * 新規予約を作成する
     * @param studentId 申請者のID
     * @param request リクエストDTO
     */
    @Transactional // ← 必須！途中でエラーが出たら全てなかったことにする（ロールバック）
    public void createReservation(Long studentId, ReservationRequest request) {

        // ==========================================
        // 1. 学生の資格チェック (Domain Logic)
        // ==========================================
        // ヒント: 学生エンティティを取得し、ビジネスルールを確認する
        // TODO このエラーはStudentクラスのstudentRepository（インターフェース）が実装されて居ないので起きています
        //      canBorrowに関しては現在は仮でreturn false;を返してて居ます
        Student student = studentRepository.findById(studentId);
        if (!student.canBorrow()) {
            throw new SuspendedException("貸出停止中、または延滞中のため予約できません");
        }

        // ==========================================
        // 2. 機材モデルの存在確認
        // ==========================================
        // TODO このエラーは実装されて居ないので起きています
        Model model = modelRepository.findById(request.modelId());
        if (model.getAssets() == null) {
            throw new IllegalStateException("機材が存在しない、または他の生徒がすでに借りており、在庫が0である");
        }

        // ==========================================
        // 3. 【最重要】有効在庫(X)の計算
        // ==========================================
        // TODO このエラーは実装されて居ないので起きています
        int overlappingReservations = reservationRepository.countOverlappingReservations(
                request.modelId(),  // recordのゲッター（メソッド形式）で呼び出し
                request.startAt(),  // 開始時間
                request.endAt()     // 終了時間
        );

        int totalQuantity = modelRepository.findById(request.modelId())
                .map(Model::getTotalQuantity)
                .orElse(0);

        int effectiveStock = totalQuantity - overlappingReservations;
        // A. そのモデルの「貸出可能(AVAILABLE)」な総数
        // TODO このエラーは実装されて居ないので起きています
        int totalAssets = assetRepository.countAvailableByModelId(model.getId());
        // B. 現在「貸出中(LENT)」でまだ返ってきていない数
        // TODO このエラーは実装されて居ないので起きています
         int currentLoans = loanRecordRepository.countActiveLoansByModelId(model.getId());

         int overlappingReservations = reservationRepository.countOverlappingReservations(
                 model.getId(),
                 request.startAt(),
                 request.endAt()
            );

        // ★ 計算式: X = 総数 - (貸出中 + 予約済み)
        // TODO このエラーは実装されて居ないので起きています
         int effectiveStock = totalAssets - currentLoans - overlappingReservations;

        // ==========================================
        // 4. 在庫判定 (Guard Clause)
        // ==========================================
        // TODO このエラーは実装されて居ないので起きています
         if (effectiveStock <= 0) {
             throw new OutOfStockException(model.getId(), effectiveStock);
         }

        // ==========================================
        // 5. 予約の確定と保存
        // ==========================================
        // TODO このエラーは実装されて居ないので起きています
        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setModel(model);
        reservation.setStartAt(request.startAt());
        reservation.setEndAt(request.endAt());
        reservation.setStatus(ReservationStatus.PENDING); // 承認待ちで保存

        reservationRepository.save(reservation);
    }
}