package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.activation.ActivationKeyVerifyRequest;
import com.yx.fridgebutler.service.ActivationKeyService;
import com.yx.fridgebutler.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户激活密钥控制器
 * <p>提供用户激活状态查询和密钥验证接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/activation-key")
public class ActivationKeyController {

    @Autowired
    private ActivationKeyService activationKeyService;

    /**
     * 查询当前用户的激活状态
     *
     * @param request HTTP 请求对象
     * @return 是否已激活
     */
    @GetMapping("/status")
    public Result<Boolean> getActivationStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean activated = activationKeyService.isUserActivated(userId);
        log.debug("查询用户激活状态，用户ID：{}，已激活：{}", userId, activated);
        return Result.success(activated);
    }

    /**
     * 验证并绑定激活密钥
     *
     * @param request     HTTP 请求对象
     * @param verifyRequest 密钥验证请求
     * @return 新的 JWT Token
     */
    @PostMapping("/verify")
    public Result<String> verifyKey(HttpServletRequest request,
                                    @Valid @RequestBody ActivationKeyVerifyRequest verifyRequest) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户提交激活密钥，用户ID：{}，密钥：{}", userId, verifyRequest.getKeyCode());
        String newToken = activationKeyService.verifyKey(verifyRequest.getKeyCode(), userId);
        return Result.success(newToken);
    }
}
