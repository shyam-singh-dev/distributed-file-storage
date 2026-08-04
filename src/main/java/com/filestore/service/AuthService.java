package com.filestore.service;

import com.filestore.dto.RegisterRequest;
import com.filestore.dto.UserDTO;

public interface AuthService {
    UserDTO register(RegisterRequest request);
}
