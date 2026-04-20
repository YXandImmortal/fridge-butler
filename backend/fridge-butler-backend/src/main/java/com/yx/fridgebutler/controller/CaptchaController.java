package com.yx.fridgebutler.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.yx.fridgebutler.util.CaptchaManager;
import com.yx.fridgebutler.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaManager captchaManager;

    @GetMapping("/generate")
    public void generateCaptcha(HttpServletResponse response) throws IOException {
        // 创建数字验证码，4位数字
        Captcha captcha = new SpecCaptcha(130, 50, 4);
        captcha.setCharType(SpecCaptcha.TYPE_ONLY_NUMBER);
        
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
    }

    @GetMapping("/verify")
    public Result<Boolean> verifyCaptcha(String captchaId, String captcha) {
        // 使用无session方式验证验证码
        boolean result = captchaManager.verifyCaptcha(captchaId, captcha);
        return Result.success(result);
    }

}