import request from '@/utils/request'
import {useUserStore} from '@/stores/user'
import {replaceToLogin} from '@/utils/navigate'
import showMessage from '@/utils/message'

/**
 * AI 聊天接口（二期：history 由后端从数据库读取，前端无需透传）
 * @param {Object} params
 * @param {string} params.message - 用户输入内容
 * @param {string} [params.sessionId] - 会话ID，首次可不传
 * @param {Array} [params.attachments] - 附件列表 [{type, id, name, fridgeId?, fridgeName?}]
 * @param {number} [params.fridgeId] - 当前冰箱ID（物品向导场景）
 */
export function sendChatMessage(params) {
    return request({
        url: '/ai/chat',
        method: 'post',
        data: params
    })
}

/**
 * 解析单条 SSE 事件字符串
 * @param {string} raw
 * @returns {{event:string, data:any}|null}
 */
function parseSSEEvent(raw) {
    const lines = raw.split('\n')
    let event = ''
    let data = ''

    for (const line of lines) {
        if (line.startsWith('event:')) {
            event = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
            data = line.slice(5).trim()
        }
    }

    if (!event || !data) return null

    try {
        return {event, data: JSON.parse(data)}
    } catch {
        return {event, data}
    }
}

/**
 * AI 流式聊天接口（SSE）
 * @param {Object} options
 * @param {string} options.message - 用户输入内容
 * @param {string} [options.sessionId] - 会话ID
 * @param {Array} [options.attachments] - 附件列表 [{type, id, name, fridgeId?, fridgeName?}]
 * @param {Object} [options.wizardContext] - 向导上下文 {type, currentStep, formData, inputField?}
 * @param {number} [options.fridgeId] - 当前冰箱ID（物品向导场景）
 * @param {Function} [options.onText] - 文本片段回调 (chunk: string) => void
 * @param {Function} [options.onCard] - 卡片数据回调 (messageType: string, data: any) => void
 * @param {Function} [options.onDone] - 结束回调 (sessionId: string, suggestions: string[]) => void
 * @param {Function} [options.onError] - 错误回调 (msg: string) => void
 * @param {AbortSignal} [options.signal] - 用于中断请求
 */
export async function sendChatMessageStream({
                                                message,
                                                sessionId,
                                                attachments,
                                                wizardContext,
                                                fridgeId,
                                                onText,
                                                onCard,
                                                onDone,
                                                onError,
                                                signal
                                            }) {
    const userStore = useUserStore()

    // Token 过期检查（与 axios 拦截器保持一致）
    if (userStore.expireTime && Date.now() > userStore.expireTime) {
        showMessage.warning('登录已过期，请重新登录')
        userStore.logout()
        replaceToLogin()
        throw new Error('Token expired')
    }

    const token = userStore.token
    const baseURL = import.meta.env.VITE_API_BASE_URL || ''

    const response = await fetch(`${baseURL}/ai/chat/stream`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({message, sessionId, attachments, wizardContext, fridgeId}),
        signal
    })

    if (!response.ok) {
        if (response.status === 401) {
            showMessage.error('登录已过期，请重新登录')
            userStore.logout()
            replaceToLogin()
            throw new Error('Unauthorized')
        }
        const err = await response.json().catch(() => ({}))
        if (response.status === 503) {
            const msg = err.message || 'AI 服务繁忙，请稍后重试'
            onError?.(msg)
            throw new Error('AI_SERVICE_UNAVAILABLE')
        }
        if (response.status === 403) {
            const msg = err.message || '无权访问该资源'
            onError?.(msg)
            throw new Error(msg)
        }
        throw new Error(err.message || `HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    try {
        while (true) {
            const {done, value} = await reader.read()
            if (done) break

            buffer += decoder.decode(value, {stream: true}).replace(/\r\n/g, '\n')
            const events = buffer.split('\n\n')
            buffer = events.pop() || ''

            let hasEvent = false
            for (const event of events) {
                if (!event.trim()) continue
                hasEvent = true
                const parsed = parseSSEEvent(event)
                if (!parsed) continue

                switch (parsed.event) {
                    case 'text':
                        onText?.(parsed.data.chunk || '')
                        break
                    case 'card':
                        onCard?.(parsed.data.messageType, parsed.data.data)
                        break
                    case 'done':
                        onDone?.(parsed.data.sessionId, parsed.data.suggestions || [])
                        break
                    case 'error':
                        onError?.(parsed.data.message || '流式响应出错')
                        break
                }
            }

            // 让出主线程，让浏览器有机会渲染本轮更新
            if (hasEvent) {
                await new Promise(resolve => requestAnimationFrame(resolve))
            }
        }

        // 处理最后残留的 buffer（可能没有 \n\n 结尾）
        if (buffer.trim()) {
            const parsed = parseSSEEvent(buffer)
            if (parsed) {
                switch (parsed.event) {
                    case 'text':
                        onText?.(parsed.data.chunk || '')
                        break
                    case 'card':
                        onCard?.(parsed.data.messageType, parsed.data.data)
                        break
                    case 'done':
                        onDone?.(parsed.data.sessionId, parsed.data.suggestions || [])
                        break
                    case 'error':
                        onError?.(parsed.data.message || '流式响应出错')
                        break
                }
            }
        }
    } finally {
        reader.releaseLock()
    }
}

/**
 * 查询当前用户的最近会话列表
 */
export function getChatSessions() {
    return request({
        url: '/ai/chat/sessions',
        method: 'get'
    })
}

/**
 * 删除指定会话
 * @param {string} sessionId - 会话ID
 */
export function deleteChatSession(sessionId) {
    return request({
        url: `/ai/chat/session/${sessionId}`,
        method: 'delete'
    })
}

/**
 * 查询指定会话的历史消息列表
 * @param {string} sessionId - 会话ID
 */
export function getChatSessionMessages(sessionId) {
    return request({
        url: `/ai/chat/session/${sessionId}/messages`,
        method: 'get'
    })
}
