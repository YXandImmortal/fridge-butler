<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="消息中心"
        description="系统通知、临期提醒和重要预警都会汇聚在这里，让您不错过任何关键信息。"
    />
    <el-tour-step
        target=".page-header"
        title="消息总览"
        description="顶部显示未读消息数量，支持一键刷新和全部已读，快速处理海量通知。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".category-panel"
        title="分类筛选"
        description="左侧按消息类型分类，点击即可切换查看系统、临期、过期等不同类别的通知。"
        placement="right"
        :show-arrow="false"
    />
    <el-tour-step
        target=".notification-card"
        title="消息卡片"
        description="每条消息都有优先级色条和状态标识，未读消息高亮显示，点击卡片可快速跳转处理。"
        placement="left"
        :show-arrow="false"
    />
    <el-tour-step
        title="消息管理完毕"
        description="记得定期查看消息中心，及时处理临期提醒，让食材管理更省心。"
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
  tourStore.completeScene(TOUR_SCENES.NOTIFICATION)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.NOTIFICATION)
}

defineExpose({
  start
})
</script>
