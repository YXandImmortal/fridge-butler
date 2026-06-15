import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {
    sendChatMessage,
    sendChatMessageStream,
    getChatSessions,
    deleteChatSession,
    getChatSessionMessages
} from '@/api/ai'

const SESSION_STORAGE_KEY = 'ai_chat_session_id'

function generateMsgId() {
    return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

function formatTime(date) {
    const h = String(date.getHours()).padStart(2, '0')
    const m = String(date.getMinutes()).padStart(2, '0')
    return `${h}:${m}`
}

function buildWelcomeMessage() {
    return {
        id: generateMsgId(),
        role: 'assistant',
        content: '你好！我是你的 AI 冰箱管家 🎉\n我可以帮你：\n• 查询冰箱库存\n• 查看临期提醒\n• 推荐菜谱\n• 回答食材相关问题\n\n试试点击下方快捷按钮，或直接输入你想问的问题~',
        time: formatTime(new Date())
    }
}

const DEFAULT_QUICK_ACTIONS = ['查看冰箱', '有什么食材', '临期提醒', '推荐菜谱']

export const useAiChatStore = defineStore('aiChat', () => {
    // ==================== State ====================
    const messages = ref([buildWelcomeMessage()])
    const sessionId = ref(localStorage.getItem(SESSION_STORAGE_KEY) || null)
    const sessions = ref([])
    const sessionLoading = ref(false)
    const aiTyping = ref(false)
    const suggestions = ref([])
    const abortController = ref(null)
    const activeWizard = ref(null)
    const pendingWizardData = ref(null)
    const wizardCompleted = ref(false)

    // ==================== Getters ====================
    const currentSessionName = computed(() => {
        if (!sessionId.value) return '新对话'
        const session = sessions.value.find(s => s.sessionId === sessionId.value)
        return session?.title || '新对话'
    })

    const activeWizardData = computed(() => {
        if (!activeWizard.value) return null
        const {type, ...data} = activeWizard.value
        return data
    })

    const isWizardConfirmStep = computed(() => {
        if (!activeWizard.value || activeWizard.value.type !== 'fridge_creation') return false
        const total = activeWizard.value.totalSteps || 3
        return activeWizard.value.currentStep >= total - 1
    })

    // ==================== Message helpers ====================
    function resetWelcome() {
        messages.value = [buildWelcomeMessage()]
    }

    function addUserMessage(text, attachments = []) {
        messages.value.push({
            id: generateMsgId(),
            role: 'user',
            content: text,
            attachments,
            time: formatTime(new Date())
        })
    }

    function addAssistantMessage(content = '', messageType = 'text', data = null) {
        messages.value.push({
            id: generateMsgId(),
            role: 'assistant',
            content,
            messageType,
            data,
            time: formatTime(new Date())
        })
    }

    function ensureAssistantPlaceholder() {
        messages.value.push({
            id: generateMsgId(),
            role: 'assistant',
            content: '',
            messageType: 'text',
            data: null,
            time: formatTime(new Date())
        })
    }

    function getLastAssistantMessage() {
        for (let i = messages.value.length - 1; i >= 0; i--) {
            if (messages.value[i].role === 'assistant') {
                return messages.value[i]
            }
        }
        return null
    }

    function updateMessageById(id, patch) {
        const idx = messages.value.findIndex(m => m.id === id)
        if (idx !== -1) {
            messages.value[idx] = {...messages.value[idx], ...patch}
        }
    }

    function setSuggestions(newSuggestions) {
        suggestions.value = (newSuggestions || []).filter(
            item => !DEFAULT_QUICK_ACTIONS.includes(item)
        )
    }

    // ==================== Stream control ====================
    function abortStream() {
        if (abortController.value) {
            abortController.value.abort()
            abortController.value = null
        }
    }

    // ==================== Wizard helpers ====================
    function activateWizard(messageType, data) {
        if (messageType === 'fridge_creation_wizard') {
            wizardCompleted.value = false
            activeWizard.value = {
                type: 'fridge_creation',
                currentStep: data.currentStep,
                totalSteps: data.totalSteps,
                steps: data.steps || [],
                formData: data.formData || {},
                currentInput: data.currentInput || null
            }
        } else if (messageType === 'item_creation_wizard') {
            wizardCompleted.value = false
            activeWizard.value = {
                type: 'item_creation',
                currentStep: data.currentStep,
                totalSteps: data.totalSteps,
                steps: data.steps || [],
                formData: data.formData || {},
                currentInput: data.currentInput || null
            }
        } else {
            activeWizard.value = null
        }
    }

    function setPendingWizardData(data) {
        pendingWizardData.value = data
    }

    function clearPendingWizardData() {
        pendingWizardData.value = null
    }

    function startItemWizardFromPending() {
        if (!pendingWizardData.value) return
        activeWizard.value = {
            type: 'item_creation',
            currentStep: pendingWizardData.value.currentStep,
            totalSteps: pendingWizardData.value.totalSteps,
            steps: pendingWizardData.value.steps || [],
            formData: pendingWizardData.value.formData || {},
            currentInput: pendingWizardData.value.currentInput || null
        }
        pendingWizardData.value = null
    }

    function clearWizard() {
        activeWizard.value = null
        pendingWizardData.value = null
    }

    // ==================== Session management ====================
    async function loadSessions() {
        try {
            sessionLoading.value = true
            const res = await getChatSessions()
            if (res.code === 200 && Array.isArray(res.data)) {
                sessions.value = res.data
            } else {
                sessions.value = []
            }
        } catch (err) {
            console.error('加载会话列表失败:', err)
            sessions.value = []
        } finally {
            sessionLoading.value = false
        }
    }

    async function loadSessionMessages(sid) {
        const res = await getChatSessionMessages(sid)
        if (res.code === 200 && Array.isArray(res.data)) {
            messages.value = res.data.map(m => ({
                id: m.id || generateMsgId(),
                role: m.role,
                content: m.content || '',
                messageType: m.messageType || 'text',
                data: m.data || null,
                time: m.createTime
                    ? formatTime(new Date(m.createTime.replace(' ', 'T')))
                    : formatTime(new Date())
            }))
        }
    }

    async function switchSession(sid) {
        if (sid === sessionId.value) return

        abortStream()
        aiTyping.value = false
        activeWizard.value = null
        pendingWizardData.value = null
        sessionId.value = sid
        localStorage.setItem(SESSION_STORAGE_KEY, sid)
        messages.value = []
        suggestions.value = []

        await loadSessionMessages(sid)
    }

    function createNewSession() {
        abortStream()
        aiTyping.value = false
        sessionId.value = null
        activeWizard.value = null
        pendingWizardData.value = null
        resetWelcome()
        suggestions.value = []
        localStorage.removeItem(SESSION_STORAGE_KEY)
    }

    async function deleteSession(sid) {
        const res = await deleteChatSession(sid)
        if (res.code === 200) {
            sessions.value = sessions.value.filter(s => s.sessionId !== sid)
            if (sessionId.value === sid) {
                createNewSession()
            }
        }
        return res
    }

    // ==================== Core: send message ====================
    async function sendMessage({text, attachments = [], fridgeId, wizardContext}) {
        addUserMessage(text, attachments)
        ensureAssistantPlaceholder()
        const assistantMsg = getLastAssistantMessage()
        aiTyping.value = true

        abortStream()
        abortController.value = new AbortController()

        const payload = {message: text, attachments}
        if (sessionId.value) {
            payload.sessionId = sessionId.value
        }
        if (fridgeId) {
            payload.fridgeId = Number(fridgeId)
        }
        if (wizardContext) {
            payload.wizardContext = wizardContext
        }

        let useFallback = false
        let fallbackResponse = null
        let streamReward = null

        try {
            await sendChatMessageStream({
                ...payload,
                signal: abortController.value.signal,
                onText: (chunk) => {
                    if (assistantMsg) assistantMsg.content += chunk
                },
                onCard: (messageType, data) => {
                    if (!assistantMsg) return
                    assistantMsg.messageType = messageType
                    assistantMsg.data = data
                    if (messageType === 'item_creation_wizard' && !fridgeId) {
                        setPendingWizardData(data)
                        activeWizard.value = null
                    } else {
                        activateWizard(messageType, data)
                    }
                },
                onDone: (newSid, newSuggestions) => {
                    sessionId.value = newSid || sessionId.value
                    if (sessionId.value) {
                        localStorage.setItem(SESSION_STORAGE_KEY, sessionId.value)
                    }
                    setSuggestions(newSuggestions)
                    aiTyping.value = false
                    abortController.value = null
                    loadSessions()
                },
                onReward: (reward) => {
                    streamReward = reward
                },
                onError: (msg) => {
                    useFallback = true
                    if (assistantMsg) {
                        const idx = messages.value.indexOf(assistantMsg)
                        if (idx !== -1) {
                            messages.value[idx] = {
                                ...assistantMsg,
                                content: msg || 'AI 服务繁忙，请稍后重试',
                                messageType: 'text'
                            }
                        }
                    }
                }
            })
        } catch (err) {
            if (err.name === 'AbortError') {
                aiTyping.value = false
                abortController.value = null
                return null
            }
            console.error('SSE 请求失败:', err)
            if (err.message === 'AI_SERVICE_UNAVAILABLE') {
                aiTyping.value = false
                abortController.value = null
                return null
            }
            useFallback = true
            if (assistantMsg) {
                const idx = messages.value.indexOf(assistantMsg)
                if (idx !== -1) {
                    messages.value.splice(idx, 1)
                }
            }
        }

        // 兜底：流正常结束但 onDone 未被触发时强制重置
        if (!useFallback && aiTyping.value) {
            aiTyping.value = false
            abortController.value = null
        }

        if (useFallback) {
            aiTyping.value = true
            try {
                const res = await sendChatMessage(payload)
                fallbackResponse = res

                if (res.code === 200 && res.data) {
                    const {sessionId: newSid, reply, suggestions: newSuggestions} = res.data
                    sessionId.value = newSid || sessionId.value
                    if (sessionId.value) {
                        localStorage.setItem(SESSION_STORAGE_KEY, sessionId.value)
                    }
                    setSuggestions(newSuggestions)

                    addAssistantMessage(
                        reply.text || '',
                        reply.messageType || 'text',
                        reply.data || null
                    )

                    if (reply.messageType === 'item_creation_wizard' && !fridgeId) {
                        setPendingWizardData(reply.data)
                        activeWizard.value = null
                    } else {
                        activateWizard(reply.messageType, reply.data)
                    }

                    loadSessions()
                } else {
                    addAssistantMessage('服务暂时不可用，请稍后再试。', 'text', null)
                    suggestions.value = []
                    activeWizard.value = null
                }
            } catch (err) {
                console.error('AI 聊天请求失败:', err)
                addAssistantMessage('网络连接异常，请检查网络后重试。', 'text', null)
                suggestions.value = []
                activeWizard.value = null
            } finally {
                aiTyping.value = false
            }
        }

        const reward = streamReward || extractRewardFromFallback(fallbackResponse)
        return {fallbackResponse, reward}
    }

    /**
     * 从同步兜底响应中提取奖励数据
     */
    function extractRewardFromFallback(res) {
        if (!res || res.code !== 200 || !res.data) return null
        const {expGained, dailyExpToday, dailyExpLimit, leveledUp, currentLevel, badgesUnlocked} = res.data
        if (
            expGained === undefined &&
            !badgesUnlocked?.length &&
            leveledUp === undefined
        ) {
            return null
        }
        return {
            sessionId: res.data.sessionId,
            expGained: expGained ?? 0,
            dailyExpToday: dailyExpToday ?? 0,
            dailyExpLimit: dailyExpLimit ?? 0,
            leveledUp: leveledUp ?? false,
            currentLevel: currentLevel ?? null,
            badgesUnlocked: badgesUnlocked || []
        }
    }

    return {
        // state
        messages,
        sessionId,
        sessions,
        sessionLoading,
        aiTyping,
        suggestions,
        abortController,
        activeWizard,
        pendingWizardData,
        wizardCompleted,
        // getters
        currentSessionName,
        activeWizardData,
        isWizardConfirmStep,
        // message helpers
        resetWelcome,
        addUserMessage,
        addAssistantMessage,
        ensureAssistantPlaceholder,
        getLastAssistantMessage,
        updateMessageById,
        setSuggestions,
        // stream / wizard
        abortStream,
        activateWizard,
        setPendingWizardData,
        clearPendingWizardData,
        startItemWizardFromPending,
        clearWizard,
        // session
        loadSessions,
        loadSessionMessages,
        switchSession,
        createNewSession,
        deleteSession,
        // core
        sendMessage
    }
})
