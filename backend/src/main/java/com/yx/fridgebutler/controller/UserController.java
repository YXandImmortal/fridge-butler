package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.user.BindEmailRequest;
import com.yx.fridgebutler.dto.user.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.user.UserEmailCaptchaRequest;
import com.yx.fridgebutler.dto.user.UserInitPasswordRequest;
import com.yx.fridgebutler.dto.user.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.user.UserUpdateRequest;
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

    /**
     * 标记当前登录用户的新手指引已完成
     *
     * @return 标记成功的响应结果
     */
    @PatchMapping("/guide-complete")
    public Result<Void> completeGuide() {
        userService.completeGuide();
        log.info("新手指引标记完成");
        return Result.success(null);
    }

    /**
     * 首次登录初始化密码（无需原密码和验证码）
     * <p>仅允许 password_updated_at 为 null 的用户调用，用于首次登录或管理员重置密码后的强制改密。</p>
     *
     * @param request 初始化密码请求参数
     * @return 密码设置成功的响应结果
     */
    @PostMapping("/init-password")
    public Result<Void> initPassword(@Valid @RequestBody UserInitPasswordRequest request) {
        userService.initPassword(request);
        log.info("首次登录密码初始化成功");
        return Result.success(null);
    }

    /**
     * 发送绑定/修改邮箱验证码
     * <p>向目标邮箱发送 6 位数字验证码，用于登录后绑定或修改邮箱。</p>
     *
     * @param request 绑定邮箱验证码请求参数，包含目标邮箱
     * @return 发送成功返回提示信息
     */
    @PostMapping("/email/captcha")
    public Result<Void> sendBindEmailCaptcha(@Valid @RequestBody UserEmailCaptchaRequest request) {
        log.info("发送绑定邮箱验证码请求，邮箱：{}", request.getEmail());
        userService.sendBindEmailCaptcha(request);
        return Result.success("验证码已发送，请查收邮件", null);
    }

    /**
     * 绑定或修改当前登录用户邮箱
     * <p>校验验证码及邮箱是否被其他用户占用，验证通过后更新邮箱。</p>
     *
     * @param request 绑定邮箱请求参数，包含邮箱和验证码
     * @return 绑定成功返回提示信息
     */
    @PostMapping("/email")
    public Result<Void> bindEmail(@Valid @RequestBody BindEmailRequest request) {
        log.info("绑定邮箱请求，邮箱：{}", request.getEmail());
        userService.bindEmail(request);
        return Result.success("邮箱绑定成功", null);
    }
}
