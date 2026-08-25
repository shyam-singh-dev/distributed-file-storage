package com.filestore.controller;
import com.filestore.dto.RefreshTokenRequest;
import com.filestore.dto.RefreshTokenResponse;

import com.filestore.dto.*;
import com.filestore.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    // ─────────────────────────────────────
   // REFRESH TOKEN
  // ─────────────────────────────────────
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response =
                authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed", response)
        );
    }


    // ─────────────────────────────────────
   // LOGOUT
  // ─────────────────────────────────────
    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate tokens")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {

        String token = authHeader.substring(7);
        String userEmail = authentication.getName();

        authService.logout(token, userEmail);

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully")
        );
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>>register(
            @Valid @RequestBody RegisterRequest request
            ) {
        log.info("POST /api/v1/auth/register - Registration request for: {}",
                request.getEmail());
        UserDTO registeredUser = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User register successfully ", registeredUser));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse)
        );
    }


}
