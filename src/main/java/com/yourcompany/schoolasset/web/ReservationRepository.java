package com.yourcompany.schoolasset.web;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 指定期間に重複している「承認済み(APPROVED)」予約の数を数える
     * * 重複の定義:
     * 「予約開始 < 申請終了」 かつ 「予約終了 > 申請開始」
     * (これで、一部でも重なっていればヒットします)
     */
    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.model.id = :modelId
          AND r.status = 'APPROVED'
          AND (r.startAt < :endAt AND r.endAt > :startAt)
    """)
    int countOverlappingReservations(
            @Param("modelId") Long modelId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}