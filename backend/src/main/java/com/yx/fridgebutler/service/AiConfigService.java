package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.admin.AiConfigUpdateRequest;
import com.yx.fridgebutler.dto.ai.AiRuntimeConfig;
import com.yx.fridgebutler.vo.admin.AiConfigVO;
import com.yx.fridgebutler.vo.admin.AiProviderVO;

import java.util.List;

public interface AiConfigService {

    List<AiProviderVO> listProviders();

    AiConfigVO getConfig();

    AiConfigVO updateConfig(AiConfigUpdateRequest request);

    AiRuntimeConfig getRuntimeConfig();
}
