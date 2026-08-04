package com.filestore.controller;

import com.filestore.dto.ApiResponse;
import com.filestore.dto.UserDTO;
import com.filestore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        log.info("GET /api/v1/users - Fetching all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{} - Fetching user by id", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", user)
        );
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getUserCount() {
        log.info("GET /api/v1/users/count - Fetching total user count");
        Long count = userService.getUserCount();
        return ResponseEntity.ok(
                ApiResponse.success("User count fetched successfully", count)
        );
    }
}