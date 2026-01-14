package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.web.controller;

import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.infrastructure.security.CustomUserDetails;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.web.dto.ReservationRequest;
// import com.sqlcanvas.schoolassetmanager.application.service.ReservationService; // 作成予定
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    // private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Void> createReservation(
            // 1. JWTから復元されたユーザー情報を注入（なりすまし防止の要）
            @AuthenticationPrincipal CustomUserDetails userDetails,

            // 2. バリデーション付きでDTOを受け取る
            @RequestBody @Validated ReservationRequest request
    ) {
        // ヒント: ここでServiceを呼ぶ
        // service.create(userDetails.getUser().getId(), request);

        // 作成成功時は 201 Created を返すのがRESTの作法
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}