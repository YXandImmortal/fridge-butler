package com.yx.fridgebutler.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaManagerTest {

    private CaptchaManager captchaManager;

    @BeforeEach
    void setUp() {
        captchaManager = new CaptchaManager();
    }

    @AfterEach
    void tearDown() {
        ScheduledExecutorService scheduler = (ScheduledExecutorService) ReflectionTestUtils.getField(
                captchaManager, "cleanupScheduler");
        assertNotNull(scheduler);
        scheduler.shutdownNow();
    }

    @Test
    void devCaptchaCanBeDisabledForAutomation() {
        configure(false, "dev");
        String captchaId = captchaManager.generateCaptcha("ABCD");

        assertNotNull(captchaId);
        assertEquals(0, captchaManager.getActiveCaptchaCount());
        assertTrue(captchaManager.verifyCaptcha(captchaId, "TEST"));
        assertFalse(captchaManager.verifyCaptcha(captchaId, ""));
        assertFalse(captchaManager.verifyCaptcha(captchaId, null));
        assertFalse(captchaManager.verifyCaptcha(" ", "TEST"));
    }

    @Test
    void devCaptchaRemainsEnabledByDefault() {
        configure(true, "dev");
        String firstId = captchaManager.generateCaptcha("ABCD");

        assertEquals(1, captchaManager.getActiveCaptchaCount());
        assertFalse(captchaManager.verifyCaptcha(firstId, "WXYZ"));
        assertFalse(captchaManager.verifyCaptcha(firstId, "ABCD"));

        String secondId = captchaManager.generateCaptcha("ABCD");
        assertTrue(captchaManager.verifyCaptcha(secondId, "ABCD"));
        assertFalse(captchaManager.verifyCaptcha(secondId, "ABCD"));
    }

    @Test
    void nonDevCaptchaCannotBeDisabledByConfiguration() {
        configure(false, "prod");
        String firstId = captchaManager.generateCaptcha("ABCD");

        assertEquals(1, captchaManager.getActiveCaptchaCount());
        assertFalse(captchaManager.verifyCaptcha(firstId, "TEST"));

        String secondId = captchaManager.generateCaptcha("ABCD");
        assertTrue(captchaManager.verifyCaptcha(secondId, "ABCD"));
    }

    private void configure(boolean enabled, String profiles) {
        ReflectionTestUtils.setField(captchaManager, "captchaEnabled", enabled);
        ReflectionTestUtils.setField(captchaManager, "activeProfiles", profiles);
    }
}
