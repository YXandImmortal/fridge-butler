package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.ChangePasswordRequest;
import com.yx.fridgebutler.dto.UpdateAvatarRequest;
import com.yx.fridgebutler.dto.UpdateRequest;
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
    public Result<Void> updateUser(@Valid @RequestBody UpdateRequest request) {
        userService.updateUser(request);
        return Result.success(null);
    }

    @PatchMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success(null);
    }

    @PatchMapping("/update-avatar")
    public Result<Void> updateAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        userService.updateAvatar(request);
        return Result.success(null);
    }
}
