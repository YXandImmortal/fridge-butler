package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.aichat.AIChatRequest;
import com.yx.fridgebutler.service.AIChatService;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.aichat.AIChatDataVO;
import com.yx.fridgebutler.vo.aichat.AIChatMessageVO;
import com.yx.fridgebutler.vo.aichat.AIChatSessionVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 聊天控制器。
 * <p>提供自然语言交互接口，支持意图识别并返回结构化数据供前端渲染不同UI组件。</p>
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AIChatController {

    @Autowired
    private AIChatService aiChatService;

    /**
     * AI 聊天接口。
     * <p>接收用户自然语言输入，识别意图后返回结构化回复，前端根据 messageType 渲染对应组件。</p>
     *
     * @param request AI 聊天请求参数
     * @return 包含 sessionId、reply 和 suggestions 的响应数据
     */
    @PostMapping("/chat")
    public Result<AIChatDataVO> chat(@Valid @RequestBody AIChatRequest request) {
        log.info("AI 聊天请求，sessionId：{}，message：{}", request.getSessionId(), request.getMessage());
        AIChatDataVO result = aiChatService.chat(request);
        log.info("AI 聊天响应，sessionId：{}，messageType：{}", result.getSessionId(), result.getReply().getMessageType());
        return Result.success(result);
    }

    /**
     * 查询当前用户的 AI 聊天会话列表。
     * <p>按最后活跃时间降序返回，仅包含未删除的会话。</p>
     *
     * @return 会话列表
     */
    @GetMapping("/chat/sessions")
    public Result<List<AIChatSessionVO>> listSessions() {
        List<AIChatSessionVO> result = aiChatService.listSessions();
        log.info("查询 AI 会话列表成功，数量：{}", result.size());
        return Result.success(result);
    }

    /**
     * 软删除指定 AI 聊天会话。
     *
     * @param sessionId 会话 ID
     * @return 空响应
     */
    @DeleteMapping("/chat/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        aiChatService.deleteSession(sessionId);
        log.info("删除 AI 会话成功，sessionId：{}", sessionId);
        return Result.success(null);
    }

    /**
     * 查询指定会话的历史消息列表。
     * <p>用于前端点击历史会话后恢复聊天记录，按时间升序返回。</p>
     *
     * @param sessionId 会话 ID
     * @return 消息列表（包含 role、content、messageType、data、createTime）
     */
    @GetMapping("/chat/session/{sessionId}/messages")
    public Result<List<AIChatMessageVO>> getSessionMessages(@PathVariable String sessionId) {
        log.info("查询会话消息，sessionId：{}", sessionId);
        List<AIChatMessageVO> result = aiChatService.getSessionMessages(sessionId);
        log.info("查询会话消息成功，sessionId：{}，消息数：{}", sessionId, result.size());
        return Result.success(result);
    }

    /**
     * AI 流式聊天接口（SSE）。
     * <p>通过 Server-Sent Events 协议分阶段推送回复：
     * <ul>
     *     <li><b>event: text</b> — 自然语言文本片段（text 意图下为逐字流式，结构化意图下一次性发送）</li>
     *     <li><b>event: card</b> — 结构化卡片数据（到达后前端根据 messageType 渲染对应组件）</li>
     *     <li><b>event: done</b> — 结束事件，携带 sessionId 和 suggestions</li>
     *     <li><b>event: error</b> — 异常事件</li>
     * </ul>
     *
     * @param request AI 聊天请求参数
     * @return SSE 发射器
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody AIChatRequest request) {
        log.info("AI 流式聊天请求，sessionId：{}，message：{}", request.getSessionId(), request.getMessage());
        SseEmitter emitter = new SseEmitter(180_000L); // 3 分钟超时
        aiChatService.streamChat(request, emitter);
        return emitter;
    }
}
