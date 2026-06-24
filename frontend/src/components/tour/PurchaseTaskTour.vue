<template>
  <el-tour
      v-model="visible"
      :mask="true"
      :scroll-into-view-options="true"
      @finish="onFinish"
      @close="onClose"
  >
    <el-tour-step
        title="采购任务"
        description="这里展示所有待执行的采购计划，您可以随时查看、修改或标记完成。"
    />
    <el-tour-step
        target=".task-view"
        title="任务看板"
        description="左侧纸张卡片展示当前选中的采购计划，包含物品清单和操作入口。"
        placement="right"
        :show-arrow="false"
    />
    <el-tour-step
        target=".bookmark-list"
        title="切换计划"
        description="右侧书签栏列出所有待采购方案，点击即可切换查看不同计划。"
        placement="left"
        :show-arrow="false"
    />
    <el-tour-step
        target=".paper-card"
        title="计划详情"
        description="卡片中展示计划名称、创建时间、目标冰箱，以及需要采购的物品清单。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        target=".paper-actions"
        title="任务操作"
        description="支持「取消计划」放弃任务、「修改计划」调整清单，或「采购完成」进入核对入库。"
        placement="top"
        :show-arrow="false"
    />
    <el-tour-step
        title="高效采购"
        description="完成任务后前往「核对入库」确认实际采购结果，系统将自动更新冰箱库存。"
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
  tourStore.completeScene(TOUR_SCENES.PURCHASE_TASK)
}

function onClose() {
  tourStore.completeScene(TOUR_SCENES.PURCHASE_TASK)
}

defineExpose({
  start
})
</script>
