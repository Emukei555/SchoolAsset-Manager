package com.yourcompany.schoolasset.domain.model.reservation;

import com.yourcompany.schoolasset.domain.model.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 指定された期間に重なる「承認済み」の予約数をカウントする
     * 重複条件: (既存予約.開始 < 申請終了) AND (既存予約.終了 > 申請開始)
     */
    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.model.id = :modelId
          AND r.status = 'APPROVED'
          AND r.startAt < :endAt
          AND r.endAt > :startAt
    """)
    int countOverlappingReservations(
            @Param("modelId") Long modelId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}