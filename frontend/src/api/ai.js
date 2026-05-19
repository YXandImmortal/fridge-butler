import request from '@/utils/request'

/**
 * AI 聊天接口（二期：history 由后端从数据库读取，前端无需透传）
 * @param {Object} params
 * @param {string} params.message - 用户输入内容
 * @param {string} [params.sessionId] - 会话ID，首次可不传
 */
export function sendChatMessage(params) {
    return request({
        url: '/ai/chat',
        method: 'post',
        data: params
    })
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
