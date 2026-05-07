package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.vo.LoginVO;
import com.yx.fridgebutler.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginVO login(LoginRequest request, HttpServletRequest httpRequest);

    void registerUser(RegisterRequest request, HttpServletRequest httpRequest);
}
