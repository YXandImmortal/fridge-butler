package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.UserUpdateRequest;
import com.yx.fridgebutler.dto.UserInfoDTO;
import com.yx.fridgebutler.service.UserService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<UserInfoDTO> getUserInfo() {
        return Result.success(userService.getUserInfo());
    }

    @PatchMapping("/update-info")
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        return Result.success(null);
    }

    @PatchMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success(null);
    }

    @PatchMapping("/update-avatar")
    public Result<Void> updateAvatar(@Valid @RequestBody UserUpdateAvatarRequest request) {
        userService.updateAvatar(request);
        return Result.success(null);
    }
}
