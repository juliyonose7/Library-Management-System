package com.sgilib.backend.service;

import com.sgilib.backend.api.dto.AuthResponse;
import com.sgilib.backend.api.dto.LoginRequest;
import com.sgilib.backend.api.dto.RefreshRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);
}
