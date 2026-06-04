package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatRequest;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatResponse;

import java.util.List;

/**
 * DeepSeek AI 大模型服务接口。
 * <p>封装与 DeepSeek API 的交互，提供简洁的对话调用方式。</p>
 */
public interface DeepSeekService {

    /**
     * 发送单轮对话请求，直接获取模型回复文本。
     *
     * @param userMessage 用户输入内容
     * @return 模型生成的回复文本
     */
    String chat(String userMessage);

    /**
     * 发送带系统提示的对话请求。
     *
     * @param systemMessage 系统提示词，用于设定模型角色或行为
     * @param userMessage   用户输入内容
     * @return 模型生成的回复文本
     */
    String chat(String systemMessage, String userMessage);

    /**
     * 发送多轮对话请求。
     *
     * @param messages 消息列表，可包含 system、user、assistant 多轮消息
     * @return 模型生成的回复文本
     */
    String chat(List<DeepSeekChatMessage> messages);

    /**
     * 发送自定义对话请求并返回回复文本。
     * <p>未设置的字段（如 model、temperature、maxTokens）将自动使用服务端默认值。</p>
     *
     * @param request 自定义对话请求参数
     * @return 模型生成的回复文本
     */
    String chat(DeepSeekChatRequest request);

    /**
     * 发送完整的对话请求并返回原始响应对象。
     * <p>适用于需要获取 token 用量、finish_reason 等详细信息的场景。</p>
     *
     * @param request 自定义对话请求参数
     * @return DeepSeek 原始响应对象
     */
    DeepSeekChatResponse chatComplete(DeepSeekChatRequest request);

    /**
     * 流式对话请求。
     * <p>通过 SSE 协议逐字符获取模型回复，适用于需要实时显示打字效果的场景。</p>
     *
     * @param messages 消息列表
     * @param onChunk  每个文本片段的回调（会在调用线程中同步执行）
     */
    void chatStream(List<DeepSeekChatMessage> messages, java.util.function.Consumer<String> onChunk);

    /**
     * 流式对话请求（自定义请求参数）。
     * <p>未设置的字段将自动使用服务端默认值。</p>
     *
     * @param request 自定义对话请求参数
     * @param onChunk 每个文本片段的回调
     */
    void chatStream(DeepSeekChatRequest request, java.util.function.Consumer<String> onChunk);
}
