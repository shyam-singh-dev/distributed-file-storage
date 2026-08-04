package com.filestore.service.impl;

import com.filestore.dto.RegisterRequest;
import com.filestore.dto.UserDTO;
import com.filestore.entity.User;
import com.filestore.exception.EmailAlreadyExistsException;
import com.filestore.repository.UserRepository;
import com.filestore.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}",request.getEmail());

        // Step One : check if mail already exists
        if(userRepository.existsByEmail(request.getEmail())) {
            log.warn("Register failed - Email already exists: {}",request.getEmail());
            throw new EmailAlreadyExistsException("Email already exists: "+ request.getEmail());

        }

        // Step two : Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
                log.info("Password encoded successfully for user: {}",request.getPassword());

        // Step three : Build user entity

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(hashedPassword)
                .role("USER")
                .build();

        // Step 4 : Save to database
        User savedUser = userRepository.save(user);
        log.info("User register successfully with id: {}",savedUser.getId());

        // Step 5 : Return DTO (never return the entity directly)
        return UserDTO.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
}
