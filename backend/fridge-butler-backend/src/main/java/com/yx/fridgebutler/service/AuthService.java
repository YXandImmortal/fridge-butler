package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.dto.LoginResponse;
import com.yx.fridgebutler.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void registerUser(RegisterRequest request);
}
