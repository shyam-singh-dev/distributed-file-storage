package com.filestore.service.impl;

import com.filestore.dto.UserDTO;
import com.filestore.entity.User;
import com.filestore.exception.ResourceNotFoundException;
import com.filestore.exception.UserNotFoundException;
import com.filestore.service.UserService;
import com.filestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository ;

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users from database");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        log.info("Fetching user with id: {}",id);
        User user = userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id :" + id));
        return convertToDTO(user);

    }

    public UserDTO getUserByFullName(String fullName) {
        log.warn("No User found : {} ",fullName);
        User user =  userRepository.findByFullName(fullName)
                .orElseThrow(() -> new UserNotFoundException("User not found with fullName : "));
        return convertToDTO(user);
    }

    public Long getUserCount() {
     log.info("Fetching total user count");
     return userRepository.count();
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
