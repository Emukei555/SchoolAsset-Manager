package com.yourcompany.schoolasset;

import com.yourcompany.schoolasset.domain.model.asset.*;
import com.yourcompany.schoolasset.domain.model.faculty.Faculty;
import com.yourcompany.schoolasset.domain.model.faculty.FacultyRepository;
import com.yourcompany.schoolasset.domain.model.loan.LoanRecordRepository;
import com.yourcompany.schoolasset.domain.model.reservation.*;
import com.yourcompany.schoolasset.domain.model.student.*;
import com.yourcompany.schoolasset.domain.model.user.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate; // 追加

import java.time.LocalDateTime;

@SpringBootApplication
public class SchoolAssetManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolAssetManagerApplication.class, args);
    }

    /**
     * アプリ起動時にデータを初期化するメソッド
     */
    @Bean
    public CommandLineRunner initData(

            UserRepository userRepository,
            StudentRepository studentRepository,
            ClerkRepository clerkRepository,
            FacultyRepository facultyRepository,
            ModelRepository modelRepository,
            AssetRepository assetRepository,
            CategoryRepository categoryRepository,
            ReservationRepository reservationRepository,
            LoanRecordRepository loanRecordRepository,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transactionTemplate // トランザクション制御用に追加

    ) {
        return args -> {
            // トランザクション内で実行することで、save後のエンティティが「Managed」状態を維持できる
            transactionTemplate.execute(status -> {
                // 1. 既存データを全削除
                loanRecordRepository.deleteAll();
                reservationRepository.deleteAll();
                assetRepository.deleteAll();
                modelRepository.deleteAll();
                categoryRepository.deleteAll(); // ★追加: カテゴリも削除
                studentRepository.deleteAll();
                clerkRepository.deleteAll();
                facultyRepository.deleteAll();
                userRepository.deleteAll();

                // 2. ユーザー作成
                // Transaction内なので、saveしたUserはそのまま次の処理でも同一インスタンスとして扱われる
                User studentUser = createUser(userRepository, "student@example.com", Role.STUDENT, passwordEncoder);
                User clerkUser = createUser(userRepository, "clerk@example.com", Role.CLERK, passwordEncoder);
                User facultyUser = createUser(userRepository, "faculty@example.com", Role.FACULTY, passwordEncoder);

                // 3. ロールごとの詳細データ作成
                // 学生
                Student student = new Student();
                student.setUser(studentUser);
                student.setStudentNumber(new StudentNumber("20230001"));
                student.setDepartment("情報工学科");
                student.setGrade(3);
                studentRepository.save(student);

                // 事務員
                Clerk clerk = new Clerk();
                clerk.setUser(clerkUser);
                clerk.setClerkCode("C9999");
                clerkRepository.save(clerk);

                // 教員
                Faculty faculty = new Faculty();
                faculty.setUser(facultyUser);
                faculty.setFacultyCode("F9999");
                // 必要なフィールドがあればセット
                facultyRepository.save(faculty);

                // ★追加: カテゴリを作成
                Category pcCategory = new Category();
                pcCategory.setName("ノートPC");
                pcCategory.setDescription("貸出用ラップトップ");
                categoryRepository.save(pcCategory);

                // 4. 機材データ作成
                Model model = new Model();
                model.setName("MacBook Pro M3");
                model.setTotalQuantity(5);
                model.setCategory(pcCategory);
                modelRepository.save(model);

                Asset asset = new Asset();
                asset.setModel(model);
                asset.setSerialNumber("MBP-001");
                asset.setStatus(AssetStatus.AVAILABLE);
                assetRepository.save(asset);

                // 5. 貸出テスト用の「承認済み予約」を作成
                ReservationPeriod period = new ReservationPeriod(
                        LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                        LocalDateTime.now().plusDays(1).withHour(18).withMinute(0)
                );

                Reservation reservation = new Reservation(student, model, period);
                reservation.approve(faculty); // 承認済みにする
                reservationRepository.save(reservation);

                System.out.println("=== データ初期化完了 ===");
                System.out.println("予約ID: " + reservation.getId());
                System.out.println("機材ID: " + asset.getId());
                System.out.println("事務員Email: clerk@example.com / Pass: password");

                return null;
            });
        };
    }

    private User createUser(UserRepository repo, String email, Role role, PasswordEncoder encoder) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode("password")); // BCrypt化
        user.setRole(role);
        return repo.save(user);
    }
}