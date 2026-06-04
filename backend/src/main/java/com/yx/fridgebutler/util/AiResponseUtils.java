package com.yx.fridgebutler.util;

import lombok.extern.slf4j.Slf4j;

/**
 * AI 响应处理工具类。
 * <p>提供模型输出清洗、JSON 提取等通用能力。</p>
 */
@Slf4j
public final class AiResponseUtils {

    private AiResponseUtils() {
        // 工具类禁止实例化
    }

    /**
     * 清理模型可能返回的 Markdown 代码块包装，并尝试提取 JSON 子串。
     *
     * <p>处理逻辑：</p>
     * <ol>
     *   <li>去除头尾空白</li>
     *   <li>去除头尾的 {@code ```} 或 {@code ```json} 标记</li>
     *   <li>若清理后仍不以 {@code {} 或 {@code [} 开头，尝试提取第一个合法的 JSON 对象/数组</li>
     * </ol>
     *
     * @param response 原始模型响应
     * @return 清洗后的字符串，可能为空
     */
    public static String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return "";
        }

        String cleaned = response.trim();

        // 1. 去除 markdown 代码块标记
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.lastIndexOf("```")).trim();
            }
        }

        // 2. 如果模型在 JSON 前后加了解释文字，尝试提取第一个 {...} 或 [...]
        if (!cleaned.isEmpty() && cleaned.charAt(0) != '{' && cleaned.charAt(0) != '[') {
            String extracted = extractJsonBlock(cleaned);
            if (extracted != null) {
                log.debug("从模型响应中提取了 JSON 子串，原长度={}，提取长度={}",
                        cleaned.length(), extracted.length());
                cleaned = extracted;
            }
        }

        return cleaned;
    }

    /**
     * 从混合文本中提取第一个合法的 JSON 对象或数组。
     *
     * @param text 混合文本
     * @return 提取到的 JSON 字符串；未找到则返回 null
     */
    private static String extractJsonBlock(String text) {
        int firstBrace = text.indexOf('{');
        int firstBracket = text.indexOf('[');

        int start = -1;
        char openChar = 0;

        if (firstBrace != -1 && (firstBracket == -1 || firstBrace < firstBracket)) {
            start = firstBrace;
            openChar = '{';
        } else if (firstBracket != -1) {
            start = firstBracket;
            openChar = '[';
        }

        if (start == -1) {
            return null;
        }

        char closeChar = (openChar == '{') ? '}' : ']';
        int depth = 0;
        int end = -1;
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);

            if (inString) {
                if (escape) {
                    escape = false;
                    continue;
                }
                if (c == '\\') {
                    escape = true;
                    continue;
                }
                if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
                continue;
            }

            if (c == openChar) {
                depth++;
            } else if (c == closeChar) {
                depth--;
                if (depth == 0) {
                    end = i + 1;
                    break;
                }
            }
        }

        return (end != -1) ? text.substring(start, end) : null;
    }
}
