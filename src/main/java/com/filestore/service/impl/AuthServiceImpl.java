package com.filestore.service.impl;

import com.filestore.dto.AuthResponse;
import com.filestore.dto.LoginRequest;
import com.filestore.dto.RegisterRequest;
import com.filestore.dto.UserDTO;
import com.filestore.entity.User;
import com.filestore.exception.EmailAlreadyExistsException;
import com.filestore.repository.UserRepository;
import com.filestore.service.AuthService;
import com.filestore.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt : {}",request.getEmail());
        try{
            // Spring verifies email + BCrypt password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e ){
            log.error("Login failed for: {}", request.getEmail());
            throw new BadCredentialsException(
                    "Invalid email or password");
        }

        // Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid email or password"));

        // Build Spring UserDetails for token generation
        org.springframework.security.core.userdetails.User
                userDetails =
                new org.springframework.security.core.userdetails
                        .User(
                        user.getEmail(),
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole()))
                );

        // Generate JWT
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("JWT generated for: {}", request.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(convertToDTO(user))
                .build();
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
