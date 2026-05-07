package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.vo.LoginVO;
import com.yx.fridgebutler.dto.RegisterRequest;
import com.yx.fridgebutler.enums.ResultCode;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginVO response = authService.login(request, httpRequest);
        return Result.success(ResultCode.LOGIN_SUCCESS, response);
    }

    @PostMapping("/register/user")
    public Result<Void> registerUser(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        authService.registerUser(request, httpRequest);
        return Result.success(ResultCode.REGISTER_SUCCESS, null);
    }
}