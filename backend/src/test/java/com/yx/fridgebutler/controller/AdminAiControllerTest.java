package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.config.SecurityConfig;
import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.repository.SysOperLogRepository;
import com.yx.fridgebutler.security.JwtAuthenticationFilter;
import com.yx.fridgebutler.service.AiConfigService;
import com.yx.fridgebutler.service.AiRequestStatsService;
import com.yx.fridgebutler.util.JwtUtil;
import com.yx.fridgebutler.vo.admin.AiConfigVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAiController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiConfigService aiConfigService;

    @MockitoBean
    private AiRequestStatsService requestStatsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private SysOperLogRepository sysOperLogRepository;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanReadMaskedConfig() throws Exception {
        when(aiConfigService.getConfig()).thenReturn(AiConfigVO.builder()
                .provider(AiProvider.DEEPSEEK)
                .providerName("DeepSeek")
                .model("deepseek-v4-pro")
                .baseUrl("https://api.deepseek.com")
                .hasApiKey(true)
                .apiKeyMasked("****1234")
                .build());

        mockMvc.perform(get("/admin/ai/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("DEEPSEEK"))
                .andExpect(jsonPath("$.data.hasApiKey").value(true))
                .andExpect(jsonPath("$.data.apiKeyMasked").value("****1234"))
                .andExpect(jsonPath("$.data.apiKey").doesNotExist())
                .andExpect(jsonPath("$.data.apiKeyCiphertext").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    void ordinaryUserCannotReadAiConfig() throws Exception {
        mockMvc.perform(get("/admin/ai/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserCannotReadAiUsage() throws Exception {
        mockMvc.perform(get("/admin/ai/usage"))
                .andExpect(status().isUnauthorized());
    }
}
