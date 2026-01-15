package com.yourcompany.schoolasset.web.controller;

import com.yourcompany.schoolasset.application.service.LoanService;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<Void> executeLoan(
            @RequestBody @Valid LoanExecutionRequest request,
            // ※実際はSecurityContextから取得しますが、まずは「小さく作る」ためヘッダー等で代用
            @RequestHeader("X-Clerk-User-Id") Long clerkUserId
    ) {
        // 【かな型】
        // 1. サービスを呼び出して貸出を実行する
        loanService.executeLoan(request, clerkUserId);

        // 2. 成功（201 Created）を返す
        return ResponseEntity.status(201).build();
    }

    /**
     * 返却を実行する
     * PATCH /api/v1/loans/{id}/return
     */
    @PatchMapping("/{id}/return")
    public ResponseEntity<Void> returnLoan(@PathVariable("id") Long loanId) {
        // 【かな型】
        // 1. サービスを呼び出して返却を実行する
        loanService.returnLoan(loanId);

        // 2. 正常終了（200 OK）を返す
        return ResponseEntity.ok().build();
    }
}