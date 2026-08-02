package com.filestore.service;

import com.filestore.dto.UserDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> getAllUsers() ;
    UserDTO getUserById(Long id);

}
