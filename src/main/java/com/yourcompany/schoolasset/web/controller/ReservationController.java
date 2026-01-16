package com.yourcompany.schoolasset.web.controller;

import com.yourcompany.schoolasset.application.service.ReservationService;
import com.yourcompany.schoolasset.infrastructure.security.CustomUserDetails;
import com.yourcompany.schoolasset.web.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Void> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Validated ReservationRequest request
    ) {
        // Serviceを呼び出す
        // userDetails.getUser().getId() は UserテーブルのID (認証用)
        // 学生テーブルのIDとUserテーブルのIDは @MapsId で同一なのでそのまま渡してOK
        reservationService.createReservation(userDetails.getUser().getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // ログを出して呼び出しを確認
        log.info("予約承認リクエストを受け付けました。予約ID: {}, 承認者ID: {}", id, userDetails.getUser().getId());

        // サービスの呼び出し
        reservationService.approveReservation(id, userDetails.getUser().getId());

        return ResponseEntity.ok().build();
    }

}