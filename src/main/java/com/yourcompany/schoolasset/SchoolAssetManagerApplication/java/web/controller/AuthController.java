package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.web.controller;


import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    // ログインリクエスト用のDTO (record)
    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String token) {}

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. Spring Securityで認証を行う
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email, request.password)
        );

        // 2. 認証OKならJWTを発行
        String token = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    // ログアウトはJWTの場合、クライアント側でトークンを捨てるだけなので
    // サーバー側は何もしないか、ブラックリストに入れる（MVPでは不要）
}