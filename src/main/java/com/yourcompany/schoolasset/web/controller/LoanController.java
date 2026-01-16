package com.yourcompany.schoolasset.web.controller;

import com.yourcompany.schoolasset.application.service.LoanService;
import com.yourcompany.schoolasset.infrastructure.security.CustomUserDetails;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // ★ここに @PostMapping を追加！
    @PostMapping
    public ResponseEntity<Void> executeLoan(
            @RequestBody @Valid LoanExecutionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // userDetails.getUser().getId() を渡す
        loanService.executeLoan(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}