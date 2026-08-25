package com.filestore.security;

import com.filestore.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Read Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: No token -> continue normally
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract JWT
        final String jwt = authHeader.substring(7);

        // Step 4: Check blacklist BEFORE using the token
        if (jwtService.isTokenBlacklisted(jwt)) {
            log.warn("Blacklisted token used");
            filterChain.doFilter(request, response);
            return;
        }

        // Step 5: Extract email from token
        final String userEmail = jwtService.extractUsername(jwt);

        // Step 6: User not already authenticated
        if (userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 7: Load user from database
            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(userEmail);

            // Step 8: Validate JWT
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 9: Create Authentication
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // Step 10: Set SecurityContext
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

                log.info(
                        "User {} authenticated via JWT",
                        userEmail
                );
            }
        }

        // Step 11: Continue filter chain ONCE
        filterChain.doFilter(request, response);
    }
}