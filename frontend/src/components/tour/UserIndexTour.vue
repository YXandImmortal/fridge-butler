<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="欢迎来到智鲜·引擎"
        description="这是您的个人工作台，AI 冰箱管家、数据统计和快捷入口一应俱全。"
    />
    <el-tour-step
        target=".welcome-section"
        title="欢迎问候"
        description="根据时间展示不同的问候语，让您每次进入都感受到专属的贴心体验。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".stats-row"
        title="数据一览"
        description="冰箱数量、物品总数、临期预警等核心数据一目了然，帮您随时掌握食材动态。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".chat-header-actions"
        title="会话管理"
        description="点击图标即可新建对话，或查看、切换历史会话，方便管理不同的咨询主题。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".chat-session-name"
        title="当前会话"
        description="这里显示您正在使用的 AI 会话名称，方便识别对话上下文。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".chat-input-area"
        title="和 AI 管家对话"
        description="在输入框中描述您的需求，按回车或点击发送，AI 管家会即时为您解答。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".chat-quick-actions"
        title="快捷指令"
        description="预设了常用问题的快捷按钮，一键即可询问；AI 还会根据上下文推荐「猜你想问」。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".attach-btn"
        title="附件上下文"
        description="点击回形针图标，可以附加冰箱或物品信息，让 AI 的回答更精准、更贴合实际。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".quick-actions-row"
        title="快捷导航"
        description="点击卡片即可一键跳转到冰箱管理、数据中心等常用页面。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".trend-section"
        title="近 7 天动态"
        description="图表直观展示您最近一周的出入库趋势，帮您了解自己的使用习惯。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="开始探索吧"
        description="以上就是首页的主要功能，随时点击右上角的书本图标可以重温引导。祝您使用愉快！"
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
  tourStore.completeScene(TOUR_SCENES.USER_INDEX)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.USER_INDEX)
}

defineExpose({
  start
})
</script>
