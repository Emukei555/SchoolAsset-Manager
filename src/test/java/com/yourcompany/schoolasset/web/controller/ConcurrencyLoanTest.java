package com.yourcompany.schoolasset.web.controller;

import com.yourcompany.schoolasset.application.service.LoanService;
import com.yourcompany.schoolasset.domain.model.asset.*;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.*;
import com.yourcompany.schoolasset.domain.model.student.Student;
import com.yourcompany.schoolasset.domain.model.student.StudentNumber;
import com.yourcompany.schoolasset.domain.model.student.StudentRepository;
import com.yourcompany.schoolasset.domain.model.user.*;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import com.yourcompany.schoolasset.domain.model.asset.Model;
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

        // 4. 学生の作成
        Student student = new Student();
        student.setUser(studentUser);
        // ★ 修正: StudentNumber VOを使用する
        student.setStudentNumber(new StudentNumber("S0009999")); // 8桁ルールに合わせる
        student.setGrade(1);
        student.setDepartment("TestDept");
        studentRepository.saveAndFlush(student);

        // 5. 機材を作成 (Assetはそのまま)
        Asset asset = new Asset();
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setModel(savedModel);
        asset.setSerialNumber("TEST-" + System.currentTimeMillis()); // ユニーク制約回避
        asset = assetRepository.saveAndFlush(asset);
        this.savedAssetId = asset.getId();

        // 6. 予約を2つ作成
        // ★ 修正: ReservationPeriod VOを生成
        ReservationPeriod period1 = new ReservationPeriod(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        // ★ 修正: コンストラクタで生成（セッターが廃止されている場合）
        Reservation res1 = new Reservation(student, savedModel, period1);
        res1.setStatus(ReservationStatus.APPROVED);
        res1 = reservationRepository.saveAndFlush(res1);
        this.savedReservationId1 = res1.getId();

        ReservationPeriod period2 = new ReservationPeriod(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4)
        );

        Reservation res2 = new Reservation(student, savedModel, period2);
        res2.setStatus(ReservationStatus.APPROVED);
        res2 = reservationRepository.saveAndFlush(res2);
        this.savedReservationId2 = res2.getId();

        System.out.println("DEBUG: テストデータの準備完了. AssetID=" + savedAssetId + ", ResIDs=" + savedReservationId1 + "," + savedReservationId2);
    }
}