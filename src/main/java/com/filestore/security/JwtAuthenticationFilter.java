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
            )throws ServletException, IOException {

        // Step one : Read Authorization header

        final String authHeader = request.getHeader("Authorization");

        // Step two : No token ? skip this filter

        if (authHeader==null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request");
            filterChain.doFilter(request,response);
            return;
        }

        // Step three : Extract JWT (remove "Bearer" prefix)
        final String jwt = authHeader.substring(7);

        // Step 4 : Extract email from token
        final String userEmail = jwtService.extractUsername(jwt);

        // Step 5 : Validation if user not already authenticated

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication()==null) {

            // Step 6 : Load user from database

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            // Step 7 : Validate token

            if(jwtService.isTokenValid(jwt,userDetails)) {

                // Step 8 : Create auth token
                UsernamePasswordAuthenticationToken authToken  =  new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 9 : Set in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("User {} authenticated via JWT", userEmail);
            }

            // Step ten : Continue filter chain
            filterChain.doFilter(request,response);

        }
    }


}
