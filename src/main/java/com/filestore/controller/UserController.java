package com.filestore.controller;


import com.filestore.dto.UserDTO;
import com.filestore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j

public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("GET / api/v1/users - fetching all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);

    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{} - Fetching user by id", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

}
