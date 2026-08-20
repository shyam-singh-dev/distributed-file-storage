package com.filestore.security;


import com.filestore.entity.User;
import com.filestore.exception.UserNotFoundException;
import com.filestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {
        log.info("Loading user by email : {}",email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> {
                    log.error("User not found : {}",email);
                    return new UserNotFoundException(
                            "User not found: "+ email
                    );
                });
        return org.springframework.security.core.userdetails
                .User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_"+ user.getRole())))
                .build();
    }
}
