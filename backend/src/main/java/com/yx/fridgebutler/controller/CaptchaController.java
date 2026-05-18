package com.yx.fridgebutler.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.yx.fridgebutler.util.CaptchaManager;
import com.yx.fridgebutler.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 验证码控制器
 * <p>
 * 处理验证码的生成和校验请求，支持无session方式的验证码管理。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaManager captchaManager;

    /**
     * 生成验证码图片
     * <p>
     * 生成一个4位数字验证码图片，并通过响应头返回验证码ID供后续校验使用。
     * </p>
     *
     * @param response HTTP响应对象，用于输出验证码图片
     * @throws IOException 当输出验证码图片流时发生IO异常
     */
    @GetMapping("/generate")
    public void generateCaptcha(HttpServletResponse response) throws IOException {
        // 创建数字验证码，4位数字
        Captcha captcha = new SpecCaptcha(130, 44, 4);
        captcha.setCharType(SpecCaptcha.TYPE_DEFAULT);
        
        // 生成验证码
        String code = captcha.text();
        
        // 使用无session方式存储验证码
        String captchaId = captchaManager.generateCaptcha(code);
        
        // 设置响应头
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 将captchaId返回给前端（通过响应头）
        response.setHeader("X-Captcha-Id", captchaId);
        
        // 输出验证码图片
        captcha.out(response.getOutputStream());

        log.info("验证码生成成功，验证码ID：{}", captchaId);
    }

    /**
     * 校验验证码
     *
     * @param captchaId 验证码ID，通过 generateCaptcha 接口获取
     * @param captcha   用户输入的验证码内容
     * @return 验证结果，true表示验证成功，false表示验证失败
     */
    @GetMapping("/verify")
    public Result<Boolean> verifyCaptcha(String captchaId, String captcha) {
        // 使用无session方式验证验证码
        boolean result = captchaManager.verifyCaptcha(captchaId, captcha);
        if (result) {
            log.info("验证码验证成功，验证码ID：{}", captchaId);
        } else {
            log.warn("验证码验证失败，验证码ID：{}", captchaId);
        }
        return Result.success(result);
    }
}
