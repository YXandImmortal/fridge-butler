<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="物品管理"
        description="这里是冰箱的「内部仓库」，您可以查看、添加、编辑和取出食材。"
    />
    <el-tour-step
        target=".capacity-card"
        title="容量监控"
        description="顶部进度条实时展示冰箱已用容量，帮助您合理规划存储空间。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".header-actions"
        title="添加物品"
        description="点击「添加物品」手动录入，或使用「AI 帮我添加」让 AI 管家协助完成。"
        placement="left"
        :show-arrow="false"
    />
    <el-tour-step
        target=".search-section"
        title="搜索与筛选"
        description="通过名称、分类、单位类型等多维度筛选，快速找到您想要的食材。"
        placement="bottom"
        :show-arrow="false"
    />
    <el-tour-step
        target=".item-table"
        title="物品列表"
        description="表格展示了物品名称、数量、保质期、新鲜度等信息，数据清晰一目了然。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".action-btns"
        title="取出与编辑"
        description="支持取出全部、取出一半或自定义数量，也能随时编辑或删除物品信息。"
        placement="left"
        :show-arrow="false"
    />
    <el-tour-step
        title="管理有序"
        description="合理记录生产日期和保质期，系统会自动计算新鲜度，让食材管理更科学。"
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
  tourStore.completeScene(TOUR_SCENES.ITEM_MANAGE)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.ITEM_MANAGE)
}

defineExpose({
  start
})
</script>
