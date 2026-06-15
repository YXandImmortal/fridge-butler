<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="快速入门指引"
        description="感谢您选择智鲜·引擎，下面请允许我给您简单介绍一下各个功能。"
    />
    <el-tour-step
        target=".app-sidebar"
        title="导航菜单"
        description="这里是功能导航栏，可以切换冰箱管理、数据中心、物品分类等模块。有子菜单的选项点击即可展开。"
        placement="right"
        :show-arrow="false"
    />
    <el-tour-step
        target=".theme-toggle"
        title="主题切换"
        description="支持浅色 / 深色模式切换，系统会记住你的偏好。"
        :show-arrow="false"
    />
    <el-tour-step
        target=".notification-icon"
        title="消息通知"
        description="系统消息、临期提醒等通知会在这里显示，红点代表有未读消息。"
        :show-arrow="false"
    />
    <el-tour-step
        target=".user-info"
        title="个人中心"
        description="悬停此处可进入个人中心修改信息，或退出登录。"
        :show-arrow="false"
    />
    <el-tour-step
        target=".ai-chat-section"
        title="AI聊天区"
        description="可以与智能冰箱管家畅所欲言，更多功能等待您的探索！"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".quick-guide-btn"
        title="查看帮助"
        description="点击书本图标就是快速上手指南，随时可以重新打开。"
        :show-arrow="false"/>
    <el-tour-step
        title="开始使用"
        description="点击Finish开启您的“智鲜”之旅吧！"
    />
  </el-tour>
</template>

<script setup>
import {ref} from 'vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'

const visible = ref(false)
const tourStore = useTourStore()

function start() {
  visible.value = true
}

function onFinish() {
  // 只有点击最后一步的 Finish 才标记完成，发放徽章与经验
  tourStore.completeScene(TOUR_SCENES.GLOBAL_LAYOUT)
}

function onClose() {
  // 用户点击“x”或遮罩关闭时不算完成，不发放徽章与经验
  // 引导状态保持未完成，下次仍会自动播放或提醒
}

defineExpose({
  start
})
</script>
