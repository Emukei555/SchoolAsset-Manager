package com.yourcompany.schoolasset.web.controller;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class) // Controller層のみをテスト対象にする
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService; // Serviceはモック（偽物）にする

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider; // セキュリティ用のBeanもモックにする

    @Test
    @WithMockUser(roles = "CLERK") // 認証をパスさせる
    @DisplayName("貸出実行：正しいリクエストで 201 Created を返すこと")
    void executeLoan_success() throws Exception {
        // 準備
        LoanExecutionRequest request = new LoanExecutionRequest(100L, 200L);
        Long userId = 1L;

        // 実行 & 検証
        mockMvc.perform(post("/api/v1/loans")
                        .with(csrf()) // CSRF対策をパス
                        .header("X-Clerk-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Serviceのメソッドが、正しい引数で1回呼ばれたか確認
        verify(loanService).executeLoan(any(LoanExecutionRequest.class), eq(userId));
    }

    @Test
    @WithMockUser(roles = "CLERK")
    @DisplayName("返却実行：ID指定で 200 OK を返すこと")
    void returnLoan_success() throws Exception {
        Long loanId = 500L;

        mockMvc.perform(patch("/api/v1/loans/{id}/return", loanId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(loanService).returnLoan(loanId);
    }
}