package com.yourcompany.schoolasset.web.controller;

import com.yourcompany.schoolasset.application.service.LoanService;
import com.yourcompany.schoolasset.domain.model.asset.*;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.*;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentRepository;
import com.yourcompany.schoolasset.domain.model.user.*;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@SpringBootTest
class ConcurrencyLoanTest {

    @Autowired private LoanService loanService;
    @Autowired private LoanRecordRepository loanRecordRepository; // 追加: 削除に必要
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private ClerkRepository clerkRepository;
    @Autowired private ModelRepository modelRepository;

    // 追加: リレーション作成用
    @Autowired private StudentRepository studentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository; // ※なければ作成してください

    private Long savedAssetId;
    private Long savedReservationId1;
    private Long savedReservationId2;
    private Long savedClerkUserId;

    @BeforeEach
    void setUp() {
        // 1. クリーンアップ（子テーブルから順に削除）
        // 参照している側（LoanRecord）を最初に消さないと、AssetやReservationが消せません
        loanRecordRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        assetRepository.deleteAllInBatch();
        modelRepository.deleteAllInBatch();
        clerkRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        // UserやCategoryは他のテストデータ(V2等)で使われている可能性があるため、
        // 関連データのみ削除するか、影響がないなら deleteAllInBatch します。
        // ここでは安全のため、User削除は省略し、必要なUserを新規作成します。

        // 2. 依存データの作成

        // カテゴリ (Modelに必須)
        Category category = new Category();
        category.setName("TestCategory");
        category = categoryRepository.saveAndFlush(category);

        // Model (Asset, Reservationに必須)
        Model model = new Model();
        model.setName("MBP-2024");
        model.setTotalQuantity(10);
        model.setCategory(category); // セット
        Model savedModel = modelRepository.saveAndFlush(model);

        // User (Clerk, Studentに必須)
        // 事務員用ユーザー
        User clerkUser = new User();
        clerkUser.setEmail("clerk_test@example.com");
        clerkUser.setPasswordHash("hash");
        clerkUser.setRole(Role.CLERK);
        clerkUser = userRepository.saveAndFlush(clerkUser);
        this.savedClerkUserId = clerkUser.getId();

        // 学生用ユーザー
        User studentUser = new User();
        studentUser.setEmail("student_test@example.com");
        studentUser.setPasswordHash("hash");
        studentUser.setRole(Role.STUDENT);
        studentUser = userRepository.saveAndFlush(studentUser);

        // 3. 事務員の作成
        Clerk clerk = new Clerk();
        // 修正: userIdを直接セットするのではなく、Userエンティティをセットする
        clerk.setUser(clerkUser);
        // 追加: 必須項目の clerkCode をセットする
        clerk.setClerkCode("C9999");

        clerkRepository.saveAndFlush(clerk);

        // 4. 学生の作成 (Reservationに必須)
        Student student = new Student();
        student.setUser(studentUser); // @MapsId の場合
        // もし @MapsId を使っておらず userId をセットする仕様なら student.setUserId(studentUser.getId());
        student.setStudentNumber("S99999");
        student.setGrade(1);
        student.setDepartment("TestDept");
        studentRepository.saveAndFlush(student);

        // 5. 機材を作成
        Asset asset = new Asset();
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setModel(savedModel);
        asset.setSerialNumber("TEST-001"); // ユニーク制約回避のため
        asset = assetRepository.saveAndFlush(asset);
        this.savedAssetId = asset.getId(); // 自動採番されたIDを保持

        // 6. 予約を2つ作成
        Reservation res1 = new Reservation();
        res1.setStatus(ReservationStatus.APPROVED);
        res1.setModel(savedModel);
        res1.setStudent(student); // 必須
        res1.setStartAt(LocalDateTime.now().plusDays(1));
        res1.setEndAt(LocalDateTime.now().plusDays(2));
        res1 = reservationRepository.saveAndFlush(res1);
        this.savedReservationId1 = res1.getId();

        Reservation res2 = new Reservation();
        res2.setStatus(ReservationStatus.APPROVED);
        res2.setModel(savedModel);
        res2.setStudent(student); // 必須
        res2.setStartAt(LocalDateTime.now().plusDays(3));
        res2.setEndAt(LocalDateTime.now().plusDays(4));
        res2 = reservationRepository.saveAndFlush(res2);
        this.savedReservationId2 = res2.getId();

        System.out.println("DEBUG: テストデータの準備完了. AssetID=" + savedAssetId + ", ResIDs=" + savedReservationId1 + "," + savedReservationId2);
    }

    @Test
    @DisplayName("結合テスト：同時に貸出リクエストが来ても、DBロックにより二重貸出を防げること")
    void concurrencyLoanTest() throws InterruptedException {
        // --- 1. 準備 ---
        // setUpで採番されたIDを使用する
        Long assetId = this.savedAssetId;
        Long clerkUserId = this.savedClerkUserId;
        Long[] reservationIds = {this.savedReservationId1, this.savedReservationId2};

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // --- 2. 実行 ---
        for (int i = 0; i < 2; i++) {
            Long reservationId = reservationIds[i];
            executor.execute(() -> {
                try {
                    startLatch.await(); // 一斉スタート待機
                    // ClerkのIDではなく、認証ユーザーのID(UserテーブルのID)を渡す想定
                    loanService.executeLoan(new LoanExecutionRequest(reservationId, assetId), clerkUserId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("DEBUG: 排他制御によりエラー発生 -> " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 【一斉スタート！】

        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // --- 3. 検証 ---
        assertTrue(completed, "タイムアウトせずに完了すること");
        assertEquals(1, successCount.get(), "成功は必ず1件であること");
        assertEquals(1, failCount.get(), "もう1件は失敗していること(在庫切れ/排他エラー)");

        // 追加検証：Assetの状態がLENTになっているか
        Asset updatedAsset = assetRepository.findById(assetId).orElseThrow();
        assertEquals(AssetStatus.LENT, updatedAsset.getStatus());
    }
}