package com.yourcompany.schoolasset.web.controller;

import com.yourcompany.schoolasset.application.service.ReservationService;
import com.yourcompany.schoolasset.infrastructure.security.CustomUserDetails;
import com.yourcompany.schoolasset.web.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    // TODO: @PreAuthorize("hasRole('FACULTY')") で教員のみに制限
    public ResponseEntity<Void> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails // 教員情報
    ) {
        // TODO: reservationService.approveReservation(id, userDetails.getUser().getId())
        return ResponseEntity.ok().build();
    }

}