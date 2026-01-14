package com.yourcompany.schoolasset.domain.model.loan;

import com.yourcompany.schoolasset.application.service.ReservationService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long> {

    /**
     * 特定の学生が現在借りている（未返却の）機材数をカウントする
     */
    @Query("SELECT COUNT(lr) FROM LoanRecord lr WHERE lr.student.userId = :studentId AND lr.returnedAt IS NULL")
    int countActiveLoansByStudentId(@Param("studentId") Long studentId);

    /**
     * 特定の学生に延滞（期限を過ぎて未返却）の機材があるか確認する
     */
    @Query("""
       SELECT COUNT(lr) > 0 FROM LoanRecord lr 
       WHERE lr.student.userId = :studentId 
         AND lr.returnedAt IS NULL 
         AND lr.dueDate < :now
   """)
    boolean existsOverdueByStudentId(@Param("studentId") Long studentId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(lr) FROM LoanRecord lr WHERE lr.model.id = :modelId AND lr.returnedAt IS NULL")
    int countActiveLoansByModelId(@Param("modelId") Long modelId);
}