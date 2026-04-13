package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.dto.LoginResponse;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success("Login Success", response);
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }
}
