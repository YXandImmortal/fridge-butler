import {defineStore} from 'pinia'
import {ref} from 'vue'
import {useUserStore} from './user'

export const TOUR_SCENES = {
    GLOBAL_LAYOUT: 'global_layout',
    ABOUT: 'about',
    USER_INDEX: 'user_index',
    USER_CENTER: 'user_center',
    NOTIFICATION: 'notification',
    FRIDGE_LIST: 'fridge_list',
    FRIDGE_DETAIL: 'fridge_detail',
    ITEM_MANAGE: 'item_manage',
    ITEM_CATEGORY: 'item_category',
    ITEM_UNIT_TYPE: 'item_unit_type',
    DATA_CENTER: 'data_center',
}

function getSceneStorageKey(userId) {
    return userId ? `tour_scene_completed_${userId}` : 'tour_scene_completed'
}

export const useTourStore = defineStore('tour', () => {
    const userStore = useUserStore()

    // 本地临时重置标记（用于手动重新播放引导，不持久化）
    const localResetMap = ref(new Set())

    // 各场景完成状态（持久化到 localStorage）
    const sceneCompletedMap = ref(new Map())

    // 待启动的场景（用于手动触发页面引导，不持久化）
    const pendingStartScene = ref(null)

    // 已加载的 storage key，避免重复加载
    let loadedStorageKey = null

    // 从 localStorage 加载场景完成状态
    function loadSceneCompletedMap() {
        const key = getSceneStorageKey(userStore.userId)
        if (loadedStorageKey === key) return

        try {
            const raw = localStorage.getItem(key)
            if (raw) {
                const obj = JSON.parse(raw)
                sceneCompletedMap.value = new Map(Object.entries(obj))
            } else {
                sceneCompletedMap.value = new Map()
            }
            loadedStorageKey = key
        } catch (e) {
            console.error('加载 Tour 场景状态失败', e)
            sceneCompletedMap.value = new Map()
        }
    }

    // 保存场景完成状态到 localStorage
    function saveSceneCompletedMap() {
        try {
            const key = getSceneStorageKey(userStore.userId)
            const obj = Object.fromEntries(sceneCompletedMap.value)
            localStorage.setItem(key, JSON.stringify(obj))
            loadedStorageKey = key
        } catch (e) {
            console.error('保存 Tour 场景状态失败', e)
        }
    }

    function isSceneCompleted(scene) {
        loadSceneCompletedMap() // 确保加载当前用户的数据
        // 如果被临时重置，则视为未完成
        if (localResetMap.value.has(scene)) return false
        // 优先检查 sceneCompletedMap
        if (sceneCompletedMap.value.has(scene)) {
            return !!sceneCompletedMap.value.get(scene)
        }
        // 兼容旧逻辑：GLOBAL_LAYOUT 仍可使用全局 guideCompleted
        if (scene === TOUR_SCENES.GLOBAL_LAYOUT) {
            return !!userStore.guideCompleted
        }
        return false
    }

    function completeScene(scene) {
        loadSceneCompletedMap() // 确保操作的是当前用户的数据
        localResetMap.value.delete(scene)
        sceneCompletedMap.value.set(scene, true)
        saveSceneCompletedMap()
        // 全局引导完成仍向后端同步（兼容旧逻辑）
        if (scene === TOUR_SCENES.GLOBAL_LAYOUT) {
            userStore.markGuideCompleted().catch(err => console.error('标记引导完成失败', err))
        }
    }

    function resetScene(scene) {
        localResetMap.value.add(scene)
    }

    function resetAll() {
        localResetMap.value.clear()
    }

    function startScene(scene) {
        pendingStartScene.value = scene
        setTimeout(() => {
            pendingStartScene.value = null
        }, 100)
    }

    // 初始化加载
    loadSceneCompletedMap()

    return {
        localResetMap,
        sceneCompletedMap,
        pendingStartScene,
        isSceneCompleted,
        completeScene,
        resetScene,
        resetAll,
        startScene,
    }
})
