package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.aichat.AIChatRequest;
import com.yx.fridgebutler.vo.aichat.AIChatDataVO;
import com.yx.fridgebutler.vo.aichat.AIChatMessageVO;
import com.yx.fridgebutler.vo.aichat.AIChatSessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 聊天服务接口。
 * <p>封装自然语言理解、意图识别、业务数据查询与结构化响应组装的核心逻辑。</p>
 */
public interface AIChatService {

    /**
     * 处理用户 AI 聊天请求。
     * <p>流程：意图识别 → 业务数据获取 → 响应组装。</p>
     *
     * @param request AI 聊天请求参数
     * @return 包含结构化 reply 和 suggestions 的响应数据
     */
    AIChatDataVO chat(AIChatRequest request);

    /**
     * 查询当前用户的最近 AI 聊天会话列表（按最后活跃时间降序）。
     *
     * @return 会话列表
     */
    List<AIChatSessionVO> listSessions();

    /**
     * 软删除指定会话（仅允许删除当前用户自己的会话）。
     *
     * @param sessionId 会话 ID
     */
    void deleteSession(String sessionId);

    /**
     * 查询指定会话的历史消息列表（按时间升序）。
     * <p>用于前端点击历史会话后恢复聊天记录。</p>
     *
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    List<AIChatMessageVO> getSessionMessages(String sessionId);

    /**
     * 流式处理 AI 聊天请求。
     * <p>通过 SSE 协议分阶段推送：text（自然语言）→ card（结构化数据）→ done（结束）。</p>
     *
     * @param request AI 聊天请求参数
     * @param emitter SSE 发射器，用于向前端推送事件
     */
    void streamChat(AIChatRequest request, SseEmitter emitter);
}
