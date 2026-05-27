<template>
  <div class="main-layout">
    <!-- 头部组件 -->
    <Header
        @show-logout-dialog="showLogoutDialog = true"
        @start-quick-tour="handleStartQuickTour"
        @show-guide-dialog="showGuideTypeDialog = true"
    />

    <!-- 主体内容区域 -->
    <div class="main-layout__wrapper">
      <!-- 左侧导航栏 -->
      <Sidebar/>

      <!-- 主内容区域 -->
      <main class="main-layout__content">
        <router-view/>
      </main>
    </div>

    <!-- 底部版权信息 -->
    <CopyrightFooter/>

    <!-- 登出确认对话框 -->
    <ConfirmDialog
        v-model:visible="showLogoutDialog"
        title="退出登录"
        message="您确定要退出登录吗？"
        confirm-text="确定"
        cancel-text="取消"
        @confirm="handleLogout"
    />

    <!-- 全局布局引导 -->
    <GlobalLayoutTour ref="tourRef"/>

    <!-- 指引类型选择对话框 -->
    <GuideTypeDialog
        v-model:visible="showGuideTypeDialog"
        v-model="guideType"
        @confirm="handleGuideTypeConfirm"
    />
  </div>
</template>

<script setup>
import {onMounted, ref, nextTick} from 'vue'
import {useRouter} from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import CopyrightFooter from '@/components/CopyrightFooter.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import GlobalLayoutTour from '@/components/tour/GlobalLayoutTour.vue'
import GuideTypeDialog from '@/components/tour/GuideTypeDialog.vue'
import showMessage from '@/utils/message'
import {useSystemStore} from '@/stores/system'
import {useUserStore} from '@/stores/user'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'

const router = useRouter()
const systemStore = useSystemStore()
const userStore = useUserStore()
const tourStore = useTourStore()
const {getSystemInfo} = systemStore
const {logout} = userStore

// 控制登出确认对话框显示/隐藏
const showLogoutDialog = ref(false)

// 控制指引类型选择对话框显示/隐藏
const showGuideTypeDialog = ref(false)

// 当前选择的指引类型
const guideType = ref('page')

// 全局布局引导组件引用
const tourRef = ref(null)

// 初始化系统信息
onMounted(async () => {
  await getSystemInfo()

  // 首次登录自动播放全局布局引导
  nextTick(() => {
    setTimeout(() => {
      if (!tourStore.isSceneCompleted(TOUR_SCENES.GLOBAL_LAYOUT)) {
        tourRef.value?.start()
      }
    }, 800)
  })
})

// 封装：确保在首页后启动 Tour（AI 聊天区元素仅在首页存在）
const startTourWithRouteCheck = () => {
  const currentPath = router.currentRoute.value.path
  if (currentPath !== '/user/index') {
    showMessage.info('正在跳转至首页开始引导...')
    router.push('/user/index').then(() => {
      nextTick(() => {
        setTimeout(() => {
          tourRef.value?.start()
        }, 300)
      })
    })
  } else {
    nextTick(() => {
      tourRef.value?.start()
    })
  }
}

// 处理直接启动快速指引（未完成时）
const handleStartQuickTour = () => {
  startTourWithRouteCheck()
}

// 路由名称到 Tour 场景的映射
const routeSceneMap = {
  'user-index': TOUR_SCENES.USER_INDEX,
  'user-center': TOUR_SCENES.USER_CENTER,
  'user-about': TOUR_SCENES.ABOUT,
  'fridge-list': TOUR_SCENES.FRIDGE_LIST,
  'fridge-detail': TOUR_SCENES.FRIDGE_DETAIL,
  'fridge-items': TOUR_SCENES.ITEM_MANAGE,
  'data-center': TOUR_SCENES.DATA_CENTER,
  'item-category-list': TOUR_SCENES.ITEM_CATEGORY,
  'item-unit-type-list': TOUR_SCENES.ITEM_UNIT_TYPE,
  'notification-list': TOUR_SCENES.NOTIFICATION,
}

// 处理指引类型选择确认
const handleGuideTypeConfirm = (mode) => {
  if (mode === 'quick') {
    // 快速指引：重置场景，确保在首页后启动 Tour
    tourStore.resetScene(TOUR_SCENES.GLOBAL_LAYOUT)
    startTourWithRouteCheck()
  } else if (mode === 'page') {
    // 页面指引：直接触发当前页面对应的 Tour
    const currentRouteName = router.currentRoute.value.name
    const scene = routeSceneMap[currentRouteName]
    if (scene) {
      tourStore.startScene(scene)
    } else {
      showMessage.info('当前页面暂无页面指引')
    }
  }
}

// 处理退出登录
const handleLogout = () => {
  logout()
  showLogoutDialog.value = false
  router.push('/login')
  showMessage.info('已退出登录')
}
</script>

<style scoped lang="scss">
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-layout__wrapper {
  margin-top: var(--header-height);
  flex: 1;
  display: flex;
}

.main-layout__content {
  margin-left: var(--sidebar-width);
  transition: all 0.3s ease;
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  max-height: calc(100vh - var(--header-height) - var(--footer-height));
  background: var(--main-content-bg);
  padding: var(--space-5);
  overflow-y: auto;
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-layout__content {
    margin-left: var(--sidebar-width-md);
    padding: var(--space-4);
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .main-layout__content {
    margin-left: 0;
    padding: var(--space-3);
  }
}
</style>
