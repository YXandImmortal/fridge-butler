package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.dto.LoginResponse;
import com.yx.fridgebutler.dto.RegisterRequest;
import com.yx.fridgebutler.enums.ResultCode;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(ResultCode.LOGIN_SUCCESS, response);
    }

    @PostMapping("/register/user")
    public Result<Void> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return Result.success(ResultCode.REGISTER_SUCCESS, null);
    }
}
