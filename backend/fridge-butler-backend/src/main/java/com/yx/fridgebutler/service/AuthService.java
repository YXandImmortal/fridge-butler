package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
