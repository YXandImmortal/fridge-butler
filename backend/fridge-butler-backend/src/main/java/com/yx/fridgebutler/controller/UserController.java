package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.UserUpdateRequest;
import com.yx.fridgebutler.vo.UserInfoVO;
import com.yx.fridgebutler.service.UserService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * <p>
 * 处理用户信息查询、更新、密码修改、头像更新等操作。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户信息
     *
     * @return 包含用户详细信息的响应结果
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        UserInfoVO result = userService.getUserInfo();
        log.info("查询用户信息成功，用户名：{}", result.getUsername());
        return Result.success(result);
    }

    /**
     * 更新当前登录用户信息
     *
     * @param request 用户信息更新请求参数，包含用户名、手机号等可修改字段
     * @return 更新成功的响应结果
     */
    @PatchMapping("/update-info")
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        log.info("更新用户信息成功，用户名：{}", request.getUsername());
        return Result.success(null);
    }

    /**
     * 修改当前登录用户密码
     *
     * @param request 密码修改请求参数，包含原密码和新密码
     * @return 密码修改成功的响应结果
     */
    @PatchMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest request) {
        userService.changePassword(request);
        log.info("修改密码成功");
        return Result.success(null);
    }

    /**
     * 更新当前登录用户头像
     *
     * @param request 头像更新请求参数，包含头像图片的URL或标识
     * @return 头像更新成功的响应结果
     */
    @PatchMapping("/update-avatar")
    public Result<Void> updateAvatar(@Valid @RequestBody UserUpdateAvatarRequest request) {
        userService.updateAvatar(request);
        log.info("更新头像成功，头像ID：{}", request.getAvatar());
        return Result.success(null);
    }
}
