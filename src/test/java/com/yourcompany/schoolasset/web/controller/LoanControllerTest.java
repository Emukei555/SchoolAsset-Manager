package com.yourcompany.schoolasset.web.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.schoolasset.application.service.LoanService;
import com.yourcompany.schoolasset.infrastructure.security.JwtTokenProvider;
import com.yourcompany.schoolasset.web.dto.LoanExecutionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // ⭕ 新しいインポート
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    // ★追加: セキュリティフィルターが要求する Bean をモックで注入する
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "CLERK")
    @DisplayName("貸出実行：POSTリクエストが成功し、201を返すこと")
    void executeLoan_success() throws Exception {
        LoanExecutionRequest request = new LoanExecutionRequest(100L, 200L);
        Long clerkId = 1L;

        mockMvc.perform(post("/api/v1/loans")
                        .with(csrf()) // ★重要：CSRFトークンを付与して保護をパスする
                        .header("X-Clerk-User-Id", clerkId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(loanService).executeLoan(any(LoanExecutionRequest.class), eq(clerkId));
    }

    @Test
    @WithMockUser(roles = "CLERK")
    @DisplayName("返却実行：PATCHリクエストが成功し、200を返すこと")
    void returnLoan_success() throws Exception {
        Long loanId = 100L;

        mockMvc.perform(patch("/api/v1/loans/{id}/return", loanId)
                        .with(csrf())) // ★重要：ここも同様に追加
                .andExpect(status().isOk());

        verify(loanService).returnLoan(loanId);
    }
}